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
import cats.syntax.functor._
import io.circe.Json
import io.circe.syntax._
import munit.CatsEffectSuite
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.Request
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.syntax.literals._
import org.typelevel.ci._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.semconv.SchemaUrls
import org.typelevel.otel4s.semconv.experimental.attributes.CloudExperimentalAttributes._
import org.typelevel.otel4s.semconv.experimental.attributes.HostExperimentalAttributes._

class AwsEc2DetectorSuite extends CatsEffectSuite {

  test("parse metadata response and add attributes") {
    val accountId = "1234567890"
    val availabilityZone = "eu-west-1a"
    val region = "eu-west-1"
    val instanceId = "i-abc321de"
    val instanceType = "t3.small"
    val imageId = "ami-abc123de"
    val hostname = "ip-10-0-0-1.eu-west-1.compute.internal"

    val metadata = Json.obj(
      "accountId" := accountId,
      "architecture" := "x86_64",
      "availabilityZone" := availabilityZone,
      "imageId" := imageId,
      "instanceId" := instanceId,
      "instanceType" := instanceType,
      "privateIp" := "10.0.0.1",
      "region" := region,
      "version" := "2017-09-30"
    )

    val client = Client.fromHttpApp(mockServer(metadata, hostname, "token"))

    val expected = TelemetryResource(
      Attributes(
        CloudProvider(CloudProviderValue.Aws.value),
        CloudPlatform(CloudPlatformValue.AwsEc2.value),
        CloudAccountId(accountId),
        CloudRegion(region),
        CloudAvailabilityZone(availabilityZone),
        HostId(instanceId),
        HostType(instanceType),
        HostImageId(imageId),
        HostName(hostname)
      ),
      Some(SchemaUrls.Current)
    )

    AwsEc2Detector[IO](uri"", client).detect.assertEquals(Some(expected))
  }

  test("return None when metadata response is unparsable") {
    val client = Client.fromHttpApp(mockServer(Json.obj(), "", ""))
    AwsEc2Detector[IO](uri"", client).detect.assertEquals(None)
  }

  test("return None when the endpoint is unavailable exist") {
    val client = Client.fromHttpApp(HttpApp.notFound[IO])
    AwsEc2Detector[IO](uri"", client).detect.assertEquals(None)
  }

  private def mockServer(metadata: Json, hostname: String, token: String): HttpApp[IO] = {
    def checkHeader(req: Request[IO]): Either[Throwable, Unit] =
      req.headers
        .get(ci"X-aws-ec2-metadata-token")
        .toRight(new RuntimeException("Token header is missing"))
        .flatMap { header =>
          Either.cond(header.head.value == token, (), new RuntimeException("Invalid token header value"))
        }
        .void

    HttpRoutes
      .of[IO] {
        case req @ GET -> Root / "latest" / "meta-data" / "hostname" =>
          IO.fromEither(checkHeader(req)) >> Ok(hostname)

        case req @ GET -> Root / "latest" / "dynamic" / "instance-identity" / "document" =>
          import org.http4s.circe.jsonEncoder
          IO.fromEither(checkHeader(req)) >> Ok(metadata)

        case PUT -> Root / "latest" / "api" / "token" =>
          Ok(token)
      }
      .orNotFound
  }

}
