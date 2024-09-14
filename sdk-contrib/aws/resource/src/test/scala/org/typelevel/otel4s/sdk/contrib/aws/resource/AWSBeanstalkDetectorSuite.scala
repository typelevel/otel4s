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
import fs2.Stream
import fs2.io.file.Files
import fs2.io.file.Path
import io.circe.Json
import io.circe.syntax._
import munit.CatsEffectSuite
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.semconv.SchemaUrls
import org.typelevel.otel4s.semconv.attributes.ServiceAttributes
import org.typelevel.otel4s.semconv.experimental.attributes.CloudExperimentalAttributes._
import org.typelevel.otel4s.semconv.experimental.attributes.ServiceExperimentalAttributes._

class AWSBeanstalkDetectorSuite extends CatsEffectSuite {

  test("parse config file and add attributes") {
    Files[IO].tempFile.use { path =>
      val id = 11
      val versionLabel = "1"
      val envName = "production-env"

      val content = Json.obj(
        "deployment_id" := id,
        "version_label" := versionLabel,
        "environment_name" := envName
      )

      val expected = TelemetryResource(
        Attributes(
          CloudProvider(CloudProviderValue.Aws.value),
          CloudPlatform(CloudPlatformValue.AwsElasticBeanstalk.value),
          ServiceInstanceId(id.toString),
          ServiceNamespace(envName),
          ServiceAttributes.ServiceVersion(versionLabel)
        ),
        Some(SchemaUrls.Current)
      )

      for {
        _ <- write(content.noSpaces, path)
        r <- AWSBeanstalkDetector[IO](path).detect
      } yield assertEquals(r, Some(expected))
    }
  }

  test("add only provider and platform when file is empty") {
    Files[IO].tempFile.use { path =>
      val expected = TelemetryResource(
        Attributes(
          CloudProvider(CloudProviderValue.Aws.value),
          CloudPlatform(CloudPlatformValue.AwsElasticBeanstalk.value)
        ),
        Some(SchemaUrls.Current)
      )

      for {
        _ <- write("{}", path)
        r <- AWSBeanstalkDetector[IO](path).detect
      } yield assertEquals(r, Some(expected))
    }
  }

  test("return None when config file is unparsable") {
    Files[IO].tempFile.use { path =>
      AWSBeanstalkDetector[IO](path).detect.assertEquals(None)
    }
  }

  test("return None when the config file doesn't exist") {
    AWSBeanstalkDetector[IO].detect.assertEquals(None)
  }

  private def write(content: String, path: Path): IO[Unit] =
    Stream(content).through(fs2.text.utf8.encode).through(Files[IO].writeAll(path)).compile.drain

}
