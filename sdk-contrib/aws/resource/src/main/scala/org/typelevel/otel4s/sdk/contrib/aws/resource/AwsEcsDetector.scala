/*
 * Copyright 2024 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.otel4s.sdk.contrib.aws.resource

import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Env
import cats.syntax.applicativeError._
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.io.net.Network
import io.circe.Decoder
import org.http4s.EntityDecoder
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.internal.Diagnostic
import org.typelevel.otel4s.sdk.resource.TelemetryResourceDetector
import org.typelevel.otel4s.semconv.SchemaUrls

import scala.concurrent.duration._

private class AwsEcsDetector[F[_]: Async: Network: Env: Diagnostic] private (
    customClient: Option[Client[F]]
) extends TelemetryResourceDetector.Unsealed[F] {

  import AwsEcsDetector.Const
  import AwsEcsDetector.Keys
  import AwsEcsDetector.ContainerMetadata
  import AwsEcsDetector.TaskMetadata

  private implicit val containerDecoder: EntityDecoder[F, ContainerMetadata] = accumulatingJsonOf
  private implicit val taskDecoder: EntityDecoder[F, TaskMetadata] = accumulatingJsonOf

  def name: String = Const.Name

  def detect: F[Option[TelemetryResource]] =
    (Env[F].get(Const.MetadataV4Key), Env[F].get(Const.MetadataV3Key)).flatMapN { (v4, v3) =>
      v4.orElse(v3) match {
        case Some(uri) =>
          mkClient.use { client =>
            retrieve(client, uri).handleErrorWith { e =>
              Diagnostic[F]
                .error(s"AwsEcsDetector: cannot retrieve metadata from $uri due to ${e.getMessage}")
                .as(None)
            }
          }

        case None =>
          Async[F].pure(None)
      }
    }

  private def retrieve(client: Client[F], uri: String): F[Option[TelemetryResource]] =
    for {
      container <- client.expect[ContainerMetadata](uri)
      task <- client.expect[TaskMetadata](uri + "/task")
    } yield Some(build(container, task))

  private def build(container: ContainerMetadata, task: TaskMetadata): TelemetryResource = {
    val builder = Attributes.newBuilder

    val (regionOpt, accountIdOpt) = parseAccountAndRegion(task.taskArn)
    val (imageNameOpt, imageTagOpt) = parseDockerImage(container.image)

    // cloud
    builder.addOne(Keys.CloudProvider, Const.CloudProvider)
    builder.addOne(Keys.CloudPlatform, Const.CloudPlatform)
    builder.addOne(Keys.CloudResourceId, container.containerArn)
    builder.addOne(Keys.CloudAvailabilityZones, task.availabilityZone)
    builder.addAll(Keys.CloudRegion.maybe(regionOpt))
    builder.addAll(Keys.CloudAccountId.maybe(accountIdOpt))

    // container
    builder.addOne(Keys.ContainerId, container.dockerId)
    builder.addOne(Keys.ContainerName, container.dockerName)
    builder.addAll(Keys.ContainerImageName.maybe(imageNameOpt))
    builder.addAll(Keys.ContainerImageTags.maybe(imageTagOpt.map(Seq(_))))

    // aws
    builder.addOne(Keys.AwsLogGroupNames, Seq(container.logOptions.group))
    builder.addOne(Keys.AwsLogStreamNames, Seq(container.logOptions.stream))

    accountIdOpt.foreach { accountId =>
      builder.addOne(Keys.AwsLogGroupArns, Seq(container.logOptions.logGroupArn(accountId)))
      builder.addOne(Keys.AwsLogStreamArns, Seq(container.logOptions.logStreamArn(accountId)))
    }

    builder.addOne(Keys.AwsEcsContainerArn, container.containerArn)
    builder.addOne(Keys.AwsEcsContainerImageId, container.imageId)
    builder.addOne(Keys.AwsEcsTaskArn, task.taskArn)
    builder.addOne(Keys.AwsEcsLaunchType, task.launchType)
    builder.addOne(Keys.AwsEcsTaskFamily, task.family)
    builder.addOne(Keys.AwsEcsTaskRevision, task.revision)

    if (task.cluster.contains(":")) {
      builder.addOne(Keys.AwsEcsClusterArn, task.cluster)
    } else {
      regionOpt.zip(accountIdOpt).foreach { case (region, account) =>
        builder.addOne(Keys.AwsEcsClusterArn, s"arn:aws:ecs:$region:$account:cluster/${task.cluster}")
      }
    }

    TelemetryResource(builder.result(), Some(SchemaUrls.Current))
  }

  private def mkClient: Resource[F, Client[F]] =
    customClient match {
      case Some(client) => Resource.pure(client)
      case None         => EmberClientBuilder.default[F].withTimeout(Const.Timeout).build
    }

  // the format is: arn:aws:ecs:eu-west-1:12345678901:task/production/abc123
  private def parseAccountAndRegion(taskArn: String): (Option[String], Option[String]) =
    taskArn.split(":").toList match {
      case _ :: _ :: _ :: region :: account :: _ =>
        (Some(region), Some(account))

      case _ =>
        (None, None)
    }

  private def parseDockerImage(fqn: String): (Option[String], Option[String]) = {
    fqn.split(":", 2).toList match {
      case name :: tag :: Nil =>
        (Some(name), tag.split("@").headOption)

      case _ =>
        (None, None)
    }
  }
}

object AwsEcsDetector {

  private object Const {
    val Name = "aws-ecs"
    val CloudProvider = "aws"
    val CloudPlatform = "aws_ecs"
    val MetadataV3Key = "ECS_CONTAINER_METADATA_URI"
    val MetadataV4Key = "ECS_CONTAINER_METADATA_URI_V4"
    val Timeout: FiniteDuration = 2.seconds
  }

  private object Keys {
    val CloudProvider: AttributeKey[String] = AttributeKey("cloud.provider")
    val CloudPlatform: AttributeKey[String] = AttributeKey("cloud.platform")
    val CloudAccountId: AttributeKey[String] = AttributeKey("cloud.account.id")
    val CloudAvailabilityZones: AttributeKey[String] = AttributeKey("cloud.availability_zone")
    val CloudRegion: AttributeKey[String] = AttributeKey("cloud.region")
    val CloudResourceId: AttributeKey[String] = AttributeKey("cloud.resource_id")
    val ContainerId: AttributeKey[String] = AttributeKey("container.id")
    val ContainerName: AttributeKey[String] = AttributeKey("container.name")
    val ContainerImageName: AttributeKey[String] = AttributeKey("container.image.name")
    val ContainerImageTags: AttributeKey[Seq[String]] = AttributeKey("container.image.tags")
    val AwsEcsClusterArn: AttributeKey[String] = AttributeKey("aws.ecs.cluster.arn")
    val AwsEcsContainerImageId: AttributeKey[String] = AttributeKey("aws.ecs.container.image.id")
    val AwsEcsContainerArn: AttributeKey[String] = AttributeKey("aws.ecs.container.arn")
    val AwsEcsLaunchType: AttributeKey[String] = AttributeKey("aws.ecs.launchtype")
    val AwsEcsTaskArn: AttributeKey[String] = AttributeKey("aws.ecs.task.arn")
    val AwsEcsTaskFamily: AttributeKey[String] = AttributeKey("aws.ecs.task.family")
    val AwsEcsTaskRevision: AttributeKey[String] = AttributeKey("aws.ecs.task.revision")
    val AwsLogGroupNames: AttributeKey[Seq[String]] = AttributeKey("aws.log.group.names")
    val AwsLogGroupArns: AttributeKey[Seq[String]] = AttributeKey("aws.log.group.arns")
    val AwsLogStreamNames: AttributeKey[Seq[String]] = AttributeKey("aws.log.stream.names")
    val AwsLogStreamArns: AttributeKey[Seq[String]] = AttributeKey("aws.log.stream.arns")
  }

  /** The detector fetches ECS container and task metadata.
    *
    * The base uri is obtained from `ECS_CONTAINER_METADATA_URI_V4` or `ECS_CONTAINER_METADATA_URI` env variable.
    *
    * @example
    *   {{{
    * OpenTelemetrySdk
    *   .autoConfigured[IO](
    *     // register OTLP exporters configurer
    *     _.addExportersConfigurer(OtlpExportersAutoConfigure[IO])
    *     // register AWS ECS detector
    *      .addResourceDetector(AwsEcsDetector[IO])
    *   )
    *   .use { autoConfigured =>
    *     val sdk = autoConfigured.sdk
    *     ???
    *   }
    *   }}}
    *
    * @see
    *   [[https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task-metadata-endpoint-v3.html]]
    *
    * @see
    *   [[https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task-metadata-endpoint-v4.html]]
    */
  def apply[F[_]: Async: Network: Env: Diagnostic]: TelemetryResourceDetector[F] =
    new AwsEcsDetector[F](None)

  /** The detector fetches ECS container and task metadata using the given `client`.
    *
    * The base uri is obtained from `ECS_CONTAINER_METADATA_URI_V4` or `ECS_CONTAINER_METADATA_URI` env variable.
    *
    * @example
    *   {{{
    * OpenTelemetrySdk
    *   .autoConfigured[IO](
    *     // register OTLP exporters configurer
    *     _.addExportersConfigurer(OtlpExportersAutoConfigure[IO])
    *     // register AWS ECS detector
    *      .addResourceDetector(AwsEcsDetector[IO])
    *   )
    *   .use { autoConfigured =>
    *     val sdk = autoConfigured.sdk
    *     ???
    *   }
    *   }}}
    *
    * @see
    *   [[https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task-metadata-endpoint-v3.html]]
    *
    * @see
    *   [[https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task-metadata-endpoint-v4.html]]
    */
  def apply[F[_]: Async: Network: Env: Diagnostic](client: Client[F]): TelemetryResourceDetector[F] =
    new AwsEcsDetector[F](Some(client))

  private final case class ContainerMetadata(
      dockerId: String,
      dockerName: String,
      image: String,
      imageId: String,
      containerArn: String,
      logOptions: ContainerMetadata.LogOptions
  )

  private object ContainerMetadata {
    final case class LogOptions(
        group: String,
        region: String,
        stream: String
    ) {
      def logGroupArn(accountId: String): String =
        s"arn:aws:logs:$region:$accountId:log-group:$group"

      def logStreamArn(accountId: String): String =
        s"${logGroupArn(accountId)}:log-stream:$stream"
    }

    implicit val logOptionsDecoder: Decoder[LogOptions] =
      Decoder.forProduct3(
        "awslogs-group",
        "awslogs-region",
        "awslogs-stream"
      )(LogOptions.apply)

    implicit val containerMetadataDecoder: Decoder[ContainerMetadata] =
      Decoder.forProduct6(
        "DockerId",
        "DockerName",
        "Image",
        "ImageID",
        "ContainerARN",
        "LogOptions"
      )(ContainerMetadata.apply)
  }

  private final case class TaskMetadata(
      availabilityZone: String,
      cluster: String,
      taskArn: String,
      launchType: String,
      family: String,
      revision: String,
  )

  private object TaskMetadata {
    implicit val taskMetadataDecoder: Decoder[TaskMetadata] =
      Decoder.forProduct6(
        "AvailabilityZone",
        "Cluster",
        "TaskARN",
        "LaunchType",
        "Family",
        "Revision"
      )(TaskMetadata.apply)
  }
}
