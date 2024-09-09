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
import munit.CatsEffectSuite
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.semconv.SchemaUrls
import org.typelevel.otel4s.semconv.experimental.attributes.CloudExperimentalAttributes._
import org.typelevel.otel4s.semconv.experimental.attributes.FaasExperimentalAttributes._

import scala.collection.immutable

class AWSLambdaDetectorSuite extends CatsEffectSuite {

  test("add only defined attributes") {
    implicit val env: Env[IO] =
      constEnv("AWS_LAMBDA_FUNCTION_NAME" -> "function")

    val expected = TelemetryResource(
      Attributes(
        CloudProvider(CloudProviderValue.Aws.value),
        CloudPlatform(CloudPlatformValue.AwsLambda.value),
        FaasName("function")
      ),
      Some(SchemaUrls.Current)
    )

    AWSLambdaDetector[IO].detect.assertEquals(Some(expected))
  }

  test("add all attributes") {
    implicit val env: Env[IO] = constEnv(
      "AWS_REGION" -> "eu-west-1",
      "AWS_LAMBDA_FUNCTION_NAME" -> "function",
      "AWS_LAMBDA_FUNCTION_VERSION" -> "0.0.1"
    )

    val expected = TelemetryResource(
      Attributes(
        CloudProvider(CloudProviderValue.Aws.value),
        CloudPlatform(CloudPlatformValue.AwsLambda.value),
        CloudRegion("eu-west-1"),
        FaasName("function"),
        FaasVersion("0.0.1")
      ),
      Some(SchemaUrls.Current)
    )

    AWSLambdaDetector[IO].detect.assertEquals(Some(expected))
  }

  test("return None when both NAME and VERSION are undefined") {
    implicit val env: Env[IO] = constEnv("AWS_REGION" -> "eu-west-1")

    AWSLambdaDetector[IO].detect.assertEquals(None)
  }

  private def constEnv(pairs: (String, String)*): Env[IO] =
    new Env[IO] {
      private val params = pairs.toMap

      def get(name: String): IO[Option[String]] =
        IO.pure(params.get(name))

      def entries: IO[immutable.Iterable[(String, String)]] =
        IO.pure(params)
    }
}
