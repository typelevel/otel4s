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

import cats.effect.IO
import cats.effect.std.Env
import io.circe.Json
import io.circe.syntax._
import munit.CatsEffectSuite
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.semconv.SchemaUrls
import org.typelevel.otel4s.semconv.experimental.attributes.AwsExperimentalAttributes._
import org.typelevel.otel4s.semconv.experimental.attributes.CloudExperimentalAttributes._
import org.typelevel.otel4s.semconv.experimental.attributes.ContainerExperimentalAttributes._

import scala.collection.immutable

class AwsEcsDetectorSuite extends CatsEffectSuite {

  private val containerMetadataUrl = "http://169.254.170.2/v4/5fb8fcdd-29f2-490f-8229-c1269d11a9d9"

  test("parse metadata response and add attributes") {
    implicit val env: Env[IO] = constEnv("ECS_CONTAINER_METADATA_URI_V4" -> containerMetadataUrl)

    val accountId = "1234567890"
    val availabilityZone = "eu-west-1a"
    val region = "eu-west-1"
    val taskId = "5e1b48d43b264ff9b0765d6b99886980"
    val launchType = "EC2"
    val family = "service-production"
    val cluster = "production"
    val revision = "11"
    val logGroup = s"/ecs/$cluster/service"
    val logGroupArn = s"arn:aws:logs:$region:$accountId:log-group:$logGroup"
    val logStream = s"ecs/server/$taskId"
    val logStreamArn = s"$logGroupArn:log-stream:$logStream"
    val taskArn = s"arn:aws:ecs:$region:$accountId:task/$cluster/$taskId"
    val dockerImageRepository = s"$accountId.dkr.ecr.$region.amazonaws.com/internal/repository"
    val dockerImageTag = "8abab2a5"
    val dockerImageSha = "sha256:7382b7779e6038c591e0b483768a637e0b9831d46faf10749e95311f2d7d522d"
    val containerId = "83b2af5973dcd7d5e3769bc36402edc9ac26abbfdc3f185c68da9e30377ee1e1"
    val containerName = s"ecs-$family-$revision-server-e4e7efbceda7b7c68601"
    val containerArn = s"$taskArn/1a1c23fe-1718-4eed-9833-c3dc2dad712c"
    val clusterArn = s"arn:aws:ecs:$region:$accountId:cluster/$cluster"

    val container = Json.obj(
      "DockerId" := containerId,
      "Name" := "server",
      "DockerName" := containerName,
      "Image" := s"$dockerImageRepository:$dockerImageTag",
      "ImageID" := dockerImageSha,
      "Ports" := Json.arr(),
      "Labels" := Json.obj(),
      "DesiredStatus" := "RUNNING",
      "KnownStatus" := "RUNNING",
      "Limits" := Json.obj(),
      "CreatedAt" := "2024-09-12T18:08:55.593944224Z",
      "StartedAt" := "2024-09-12T18:08:56.524454503Z",
      "Type" := "NORMAL",
      "Health" := Json.obj("status" := "HEALTHY"),
      "Volumes" := Json.arr(),
      "LogDriver" := "awslogs",
      "LogOptions" := Json.obj(
        "awslogs-group" := logGroup,
        "awslogs-region" := region,
        "awslogs-stream" := logStream
      ),
      "ContainerARN" := containerArn,
      "Networks" := Json.arr()
    )

    val task = Json.obj(
      "Cluster" := cluster,
      "TaskARN" := taskArn,
      "Family" := family,
      "Revision" := revision,
      "DesiredStatus" := "RUNNING",
      "KnownStatus" := "RUNNING",
      "PullStartedAt" := "2024-09-12T18:08:55.307387715Z",
      "PullStoppedAt" := "2024-09-12T18:08:55.564707417Z",
      "AvailabilityZone" := availabilityZone,
      "LaunchType" := launchType,
      "Containers" := Json.arr(container),
      "VPCID" := "vpc-123",
      "ServiceName" := "service"
    )

    val client = Client.fromHttpApp(mockServer(container, task))

    val expected = TelemetryResource(
      Attributes(
        CloudProvider(CloudProviderValue.Aws.value),
        CloudPlatform(CloudPlatformValue.AwsEcs.value),
        CloudAccountId(accountId),
        CloudRegion(region),
        CloudAvailabilityZone(availabilityZone),
        CloudResourceId(containerArn),
        ContainerId(containerId),
        ContainerName(containerName),
        ContainerImageName(dockerImageRepository),
        ContainerImageTags(Seq(dockerImageTag)),
        Attribute("aws.ecs.container.image.id", dockerImageSha),
        AwsEcsClusterArn(clusterArn),
        AwsEcsContainerArn(containerArn),
        AwsEcsLaunchtype(launchType),
        AwsEcsTaskArn(taskArn),
        AwsEcsTaskFamily(family),
        AwsEcsTaskRevision(revision),
        AwsLogGroupNames(Seq(logGroup)),
        AwsLogStreamNames(Seq(logStream)),
        AwsLogGroupArns(Seq(logGroupArn)),
        AwsLogStreamArns(Seq(logStreamArn)),
      ),
      Some(SchemaUrls.Current)
    )

    AwsEcsDetector[IO](client).detect.assertEquals(Some(expected))
  }

  test("return None when metadata response is unparsable") {
    implicit val env: Env[IO] = constEnv("ECS_CONTAINER_METADATA_URI_V4" -> containerMetadataUrl)
    val client = Client.fromHttpApp(mockServer(Json.obj(), Json.obj()))
    AwsEcsDetector[IO](client).detect.assertEquals(None)
  }

  test("return None when the endpoint is unavailable exist") {
    implicit val env: Env[IO] = constEnv("ECS_CONTAINER_METADATA_URI_V4" -> containerMetadataUrl)
    val client = Client.fromHttpApp(HttpApp.notFound[IO])
    AwsEcsDetector[IO](client).detect.assertEquals(None)
  }

  test("return None when both ECS_CONTAINER_METADATA_URI and ECS_CONTAINER_METADATA_URI_V4 are undefined") {
    implicit val env: Env[IO] = constEnv()
    AwsEcsDetector[IO].detect.assertEquals(None)
  }

  private def constEnv(pairs: (String, String)*): Env[IO] =
    new Env[IO] {
      private val params = pairs.toMap

      def get(name: String): IO[Option[String]] =
        IO.pure(params.get(name))

      def entries: IO[immutable.Iterable[(String, String)]] =
        IO.pure(params)
    }

  private def mockServer(container: Json, task: Json): HttpApp[IO] = {
    import org.http4s.circe.jsonEncoder

    HttpRoutes
      .of[IO] {
        case GET -> Root / "v4" / "5fb8fcdd-29f2-490f-8229-c1269d11a9d9"          => Ok(container)
        case GET -> Root / "v4" / "5fb8fcdd-29f2-490f-8229-c1269d11a9d9" / "task" => Ok(task)
      }
      .orNotFound
  }

}
