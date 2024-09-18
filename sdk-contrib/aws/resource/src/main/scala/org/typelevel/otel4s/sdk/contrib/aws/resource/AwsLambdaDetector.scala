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

import cats.Monad
import cats.effect.std.Env
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.resource.TelemetryResourceDetector
import org.typelevel.otel4s.semconv.SchemaUrls

private class AwsLambdaDetector[F[_]: Env: Monad] extends TelemetryResourceDetector[F] {

  import AwsLambdaDetector.Const
  import AwsLambdaDetector.Keys

  def name: String = Const.Name

  def detect: F[Option[TelemetryResource]] =
    for {
      region <- Env[F].get("AWS_REGION")
      functionName <- Env[F].get("AWS_LAMBDA_FUNCTION_NAME")
      functionVersion <- Env[F].get("AWS_LAMBDA_FUNCTION_VERSION")
    } yield build(region, functionName, functionVersion)

  private def build(
      region: Option[String],
      functionName: Option[String],
      functionVersion: Option[String]
  ): Option[TelemetryResource] =
    Option.when(functionName.nonEmpty || functionVersion.nonEmpty) {
      val builder = Attributes.newBuilder

      builder.addOne(Keys.CloudProvider, Const.CloudProvider)
      builder.addOne(Keys.CloudPlatform, Const.CloudPlatform)

      region.foreach(r => builder.addOne(Keys.CloudRegion, r))
      functionName.foreach(name => builder.addOne(Keys.FaasName, name))
      functionVersion.foreach(v => builder.addOne(Keys.FaasVersion, v))

      TelemetryResource(builder.result(), Some(SchemaUrls.Current))
    }
}

object AwsLambdaDetector {

  private object Const {
    val Name = "aws-lambda"
    val CloudProvider = "aws"
    val CloudPlatform = "aws_lambda"
  }

  private object Keys {
    val CloudProvider: AttributeKey[String] = AttributeKey("cloud.provider")
    val CloudPlatform: AttributeKey[String] = AttributeKey("cloud.platform")
    val CloudRegion: AttributeKey[String] = AttributeKey("cloud.region")
    val FaasName: AttributeKey[String] = AttributeKey("faas.name")
    val FaasVersion: AttributeKey[String] = AttributeKey("faas.version")
  }

  /** The detector relies on the `AWS_REGION`, `AWS_LAMBDA_FUNCTION_NAME`, and `AWS_LAMBDA_FUNCTION_VERSION` environment
    * variables to configure the telemetry resource.
    *
    * Either `AWS_LAMBDA_FUNCTION_NAME` or `AWS_LAMBDA_FUNCTION_VERSION` must be present.
    *
    * @example
    *   {{{
    * OpenTelemetrySdk
    *   .autoConfigured[IO](
    *     // register OTLP exporters configurer
    *     _.addExportersConfigurer(OtlpExportersAutoConfigure[IO])
    *     // register AWS Lambda detector
    *      .addResourceDetector(AwsLambdaDetector[IO])
    *   )
    *   .use { autoConfigured =>
    *     val sdk = autoConfigured.sdk
    *     ???
    *   }
    *   }}}
    */
  def apply[F[_]: Env: Monad]: TelemetryResourceDetector[F] =
    new AwsLambdaDetector[F]

}
