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

import cats.effect.Concurrent
import cats.effect.std.Console
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.show._
import fs2.io.file.Files
import fs2.io.file.Path
import fs2.text
import io.circe.Decoder
import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.resource.TelemetryResourceDetector
import org.typelevel.otel4s.semconv.SchemaUrls
import org.typelevel.otel4s.semconv.attributes.ServiceAttributes

private class AwsBeanstalkDetector[F[_]: Concurrent: Files: Console] private (
    path: Path
) extends TelemetryResourceDetector[F] {

  import AwsBeanstalkDetector.Const
  import AwsBeanstalkDetector.Keys
  import AwsBeanstalkDetector.Metadata

  def name: String = Const.Name

  def detect: F[Option[TelemetryResource]] =
    Files[F]
      .exists(path)
      .ifM(
        parseFile,
        Console[F].errorln(s"AwsBeanstalkDetector: config file doesn't exist at path $path").as(None)
      )

  private def parseFile: F[Option[TelemetryResource]] =
    for {
      content <- Files[F].readAll(path).through(text.utf8.decode).compile.foldMonoid
      resource <- io.circe.parser
        .decode[Metadata](content)
        .fold(
          e => Console[F].errorln(show"AwsBeanstalkDetector: cannot parse metadata from $path. $e").as(None),
          m => Concurrent[F].pure(Some(build(m)))
        )
    } yield resource

  private def build(metadata: Metadata): TelemetryResource = {
    val builder = Attributes.newBuilder

    builder.addOne(Keys.CloudProvider, Const.CloudProvider)
    builder.addOne(Keys.CloudPlatform, Const.CloudPlatform)

    builder.addAll(Keys.ServiceInstanceId.maybe(metadata.deploymentId.map(_.toString)))
    builder.addAll(Keys.ServiceVersion.maybe(metadata.versionLabel))
    builder.addAll(Keys.ServiceNamespace.maybe(metadata.environmentName))

    TelemetryResource(builder.result(), Some(SchemaUrls.Current))
  }

}

object AwsBeanstalkDetector {

  private object Const {
    val Name = "aws-beanstalk"
    val CloudProvider = "aws"
    val CloudPlatform = "aws_elastic_beanstalk"
    val ConfigFilePath = "/var/elasticbeanstalk/xray/environment.conf"
  }

  private object Keys {
    val CloudProvider: AttributeKey[String] = AttributeKey("cloud.provider")
    val CloudPlatform: AttributeKey[String] = AttributeKey("cloud.platform")
    val ServiceInstanceId: AttributeKey[String] = AttributeKey("service.instance.id")
    val ServiceNamespace: AttributeKey[String] = AttributeKey("service.namespace")
    val ServiceVersion: AttributeKey[String] = ServiceAttributes.ServiceVersion
  }

  private final case class Metadata(
      deploymentId: Option[Int],
      versionLabel: Option[String],
      environmentName: Option[String]
  )

  private object Metadata {
    implicit val metadataDecoder: Decoder[Metadata] =
      Decoder.forProduct3(
        "deployment_id",
        "version_label",
        "environment_name"
      )(Metadata.apply)
  }

  /** The detector parses environment details from the `/var/elasticbeanstalk/xray/environment.conf` file.
    *
    * Expected configuration attributes:
    *   - `deployment_id`
    *   - `version_label`
    *   - `environment_name`
    *
    * @example
    *   {{{
    * OpenTelemetrySdk
    *   .autoConfigured[IO](
    *     // register OTLP exporters configurer
    *     _.addExportersConfigurer(OtlpExportersAutoConfigure[IO])
    *     // register AWS Beanstalk detector
    *      .addResourceDetector(AwsBeanstalkDetector[IO])
    *   )
    *   .use { autoConfigured =>
    *     val sdk = autoConfigured.sdk
    *     ???
    *   }
    *   }}}
    */
  def apply[F[_]: Concurrent: Files: Console]: TelemetryResourceDetector[F] =
    new AwsBeanstalkDetector[F](Path(Const.ConfigFilePath))

  /** The detector parses environment details from the file at the given `path`.
    *
    * Expected configuration attributes:
    *   - `deployment_id`
    *   - `version_label`
    *   - `environment_name`
    *
    * @example
    *   {{{
    * OpenTelemetrySdk
    *   .autoConfigured[IO](
    *     // register OTLP exporters configurer
    *     _.addExportersConfigurer(OtlpExportersAutoConfigure[IO])
    *     // register AWS Beanstalk detector
    *      .addResourceDetector(AwsBeanstalkDetector[IO])
    *   )
    *   .use { autoConfigured =>
    *     val sdk = autoConfigured.sdk
    *     ???
    *   }
    *   }}}
    */
  def apply[F[_]: Concurrent: Files: Console](path: Path): TelemetryResourceDetector[F] =
    new AwsBeanstalkDetector[F](path)

}
