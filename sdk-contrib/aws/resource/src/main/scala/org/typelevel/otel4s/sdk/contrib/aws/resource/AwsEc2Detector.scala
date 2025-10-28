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
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.io.net.Network
import io.circe.Decoder
import org.http4s.EntityDecoder
import org.http4s.Header
import org.http4s.Headers
import org.http4s.Method
import org.http4s.Request
import org.http4s.Uri
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.syntax.literals._
import org.typelevel.ci._
import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.Diagnostic
import org.typelevel.otel4s.sdk.resource.TelemetryResourceDetector
import org.typelevel.otel4s.semconv.SchemaUrls

import scala.concurrent.duration._

private class AwsEc2Detector[F[_]: Async: Network: Diagnostic] private (
    baseUri: Uri,
    customClient: Option[Client[F]]
) extends TelemetryResourceDetector.Unsealed[F] {

  import AwsEc2Detector.Const
  import AwsEc2Detector.Keys
  import AwsEc2Detector.IdentityMetadata

  def name: String = Const.Name

  def detect: F[Option[TelemetryResource]] =
    mkClient.use { client =>
      retrieve(client).handleErrorWith { e =>
        Diagnostic[F]
          .error(s"AwsEc2Detector: cannot retrieve metadata from $baseUri due to ${e.getMessage}")
          .as(None)
      }
    }

  private def retrieve(client: Client[F]): F[Option[TelemetryResource]] =
    for {
      token <- retrieveToken(client)
      metadata <- retrieveIdentityMetadata(client, token)
      hostname <- retrieveHostname(client, token)
    } yield Some(build(metadata, hostname))

  private def retrieveIdentityMetadata(client: Client[F], token: String): F[IdentityMetadata] = {
    implicit val decoder: EntityDecoder[F, IdentityMetadata] = accumulatingJsonOf[F, IdentityMetadata]

    val request = Request[F](
      Method.GET,
      uri = baseUri / "latest" / "dynamic" / "instance-identity" / "document",
      headers = Headers(Header.Raw(Const.TokenHeader, token))
    )

    client.expect[IdentityMetadata](request)
  }

  private def retrieveHostname(client: Client[F], token: String): F[String] = {
    val request = Request[F](
      Method.GET,
      uri = baseUri / "latest" / "meta-data" / "hostname",
      headers = Headers(Header.Raw(Const.TokenHeader, token))
    )

    client.expect[String](request)
  }

  private def retrieveToken(client: Client[F]): F[String] = {
    val request = Request[F](
      Method.PUT,
      uri = baseUri / "latest" / "api" / "token",
      headers = Headers(Header.Raw(Const.TokenTTLHeader, "60"))
    )

    client.expect[String](request)
  }

  private def build(metadata: IdentityMetadata, hostname: String): TelemetryResource = {
    val builder = Attributes.newBuilder

    builder.addOne(Keys.CloudProvider, Const.CloudProvider)
    builder.addOne(Keys.CloudPlatform, Const.CloudPlatform)
    builder.addOne(Keys.CloudAccountId, metadata.accountId)
    builder.addOne(Keys.CloudRegion, metadata.region)
    builder.addOne(Keys.CloudAvailabilityZones, metadata.availabilityZone)
    builder.addOne(Keys.HostId, metadata.instanceId)
    builder.addOne(Keys.HostType, metadata.instanceType)
    builder.addOne(Keys.HostImageId, metadata.imageId)
    builder.addOne(Keys.HostName, hostname)

    TelemetryResource(builder.result(), Some(SchemaUrls.Current))
  }

  private def mkClient: Resource[F, Client[F]] =
    customClient match {
      case Some(client) => Resource.pure(client)
      case None         => EmberClientBuilder.default[F].withTimeout(Const.Timeout).build
    }

}

object AwsEc2Detector {

  private object Const {
    val Name = "aws-ec2"
    val CloudProvider = "aws"
    val CloudPlatform = "aws_ec2"
    val MetadataEndpoint = uri"http://169.254.169.254"
    val Timeout: FiniteDuration = 2.seconds
    val TokenHeader = ci"X-aws-ec2-metadata-token"
    val TokenTTLHeader = ci"X-aws-ec2-metadata-token-ttl-seconds"
  }

  private object Keys {
    val CloudProvider: AttributeKey[String] = AttributeKey("cloud.provider")
    val CloudPlatform: AttributeKey[String] = AttributeKey("cloud.platform")
    val CloudAccountId: AttributeKey[String] = AttributeKey("cloud.account.id")
    val CloudAvailabilityZones: AttributeKey[String] = AttributeKey("cloud.availability_zone")
    val CloudRegion: AttributeKey[String] = AttributeKey("cloud.region")
    val HostId: AttributeKey[String] = AttributeKey("host.id")
    val HostType: AttributeKey[String] = AttributeKey("host.type")
    val HostImageId: AttributeKey[String] = AttributeKey("host.image.id")
    val HostName: AttributeKey[String] = AttributeKey("host.name")
  }

  private final case class IdentityMetadata(
      accountId: String,
      region: String,
      availabilityZone: String,
      instanceId: String,
      instanceType: String,
      imageId: String
  )

  private object IdentityMetadata {
    implicit val metadataDecoder: Decoder[IdentityMetadata] =
      Decoder.forProduct6(
        "accountId",
        "region",
        "availabilityZone",
        "instanceId",
        "instanceType",
        "imageId"
      )(IdentityMetadata.apply)
  }

  /** The detector fetches instance metadata from the `http://169.254.169.254` endpoint.
    *
    * @example
    *   {{{
    * OpenTelemetrySdk
    *   .autoConfigured[IO](
    *     // register OTLP exporters configurer
    *     _.addExportersConfigurer(OtlpExportersAutoConfigure[IO])
    *     // register AWS EC2 detector
    *      .addResourceDetector(AwsEc2Detector[IO])
    *   )
    *   .use { autoConfigured =>
    *     val sdk = autoConfigured.sdk
    *     ???
    *   }
    *   }}}
    *
    * @see
    *   [[https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instancedata-data-retrieval.html]]
    */
  def apply[F[_]: Async: Network: Diagnostic]: TelemetryResourceDetector[F] =
    new AwsEc2Detector[F](Const.MetadataEndpoint, None)

  /** The detector fetches instance metadata from the given `baseUri` using the given `client`.
    *
    * @example
    *   {{{
    * OpenTelemetrySdk
    *   .autoConfigured[IO](
    *     // register OTLP exporters configurer
    *     _.addExportersConfigurer(OtlpExportersAutoConfigure[IO])
    *     // register AWS EC2 detector
    *      .addResourceDetector(AwsEc2Detector[IO])
    *   )
    *   .use { autoConfigured =>
    *     val sdk = autoConfigured.sdk
    *     ???
    *   }
    *   }}}
    *
    * @see
    *   [[https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instancedata-data-retrieval.html]]
    */
  def apply[F[_]: Async: Network: Diagnostic](baseUri: Uri, client: Client[F]): TelemetryResourceDetector[F] =
    new AwsEc2Detector[F](baseUri, Some(client))

}
