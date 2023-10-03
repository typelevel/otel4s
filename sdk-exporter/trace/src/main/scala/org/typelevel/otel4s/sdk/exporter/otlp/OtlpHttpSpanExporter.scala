/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk.exporter.otlp

import cats.effect.Async
import cats.effect.Resource
import cats.effect.Temporal
import cats.effect.std.Console
import cats.effect.syntax.temporal._
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.io.net.Network
import io.circe.syntax._
import org.http4s.Headers
import org.http4s.Method
import org.http4s.Request
import org.http4s.Response
import org.http4s.Uri
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.syntax.literals._
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter

import scala.concurrent.duration._

final class OtlpHttpSpanExporter[F[_]: Temporal: Console] private (
    client: Client[F],
    config: OtlpHttpSpanExporter.Config
) extends SpanExporter[F] {
  import JsonCodecs._

  def exportSpans(spans: List[SpanData]): F[Unit] = {
    val request = Request[F](
      Method.POST,
      config.endpoint,
      headers = config.headers
    ).withEntity(spans.asJson)

    client
      .run(request)
      .use(response => logBody(response).unlessA(response.status.isSuccess))
      .timeout(config.timeout)
      .handleErrorWith { e =>
        Console[F].error("Cannot export spans") >> Console[F].printStackTrace(e)
      }
  }

  private def logBody(response: Response[F]) =
    for {
      body <- response.bodyText.compile.string
      _ <- Console[F].println(
        s"[OtlpHttpSpanExporter] the request failed with [${response.status}]. Body: $body"
      )
    } yield ()

}

object OtlpHttpSpanExporter {

  private object Defaults {
    val Endpoint: Uri = uri"http://localhost:4318/v1/traces"
    val Timeout: FiniteDuration = 10.seconds
  }

  private final case class Config(
      endpoint: Uri,
      timeout: FiniteDuration,
      headers: Headers
  )

  /** A builder of [[OtlpHttpSpanExporter]] */
  sealed trait Builder[F[_]] {

    /** Sets the OTLP endpoint to connect to.
      *
      * The endpoint must start with either `http://` or `https://`, and include
      * the full HTTP path.
      *
      * Default value is `http://localhost:4318/v1/traces`.
      */
    def setEndpoint(endpoint: Uri): Builder[F]

    /** Sets the maximum time to wait for the collector to process an exported
      * batch of spans.
      *
      * Default value is `10 seconds`.
      */
    def setTimeout(timeout: FiniteDuration): Builder[F]

    /** Add headers to requests. */
    def addHeaders(headers: Headers): Builder[F]

    /** Creates a [[OtlpHttpSpanExporter]] using the configuration of this
      * builder.
      */
    def build: Resource[F, OtlpHttpSpanExporter[F]]
  }

  /** Creates a [[Builder]] of [[OtlpHttpSpanExporter]] with the default
    * configuration.
    */
  def builder[F[_]: Async: Network: Console]: Builder[F] =
    BuilderImpl(
      Defaults.Endpoint,
      Defaults.Timeout,
      Headers.empty
    )

  private final case class BuilderImpl[F[_]: Async: Network: Console](
      endpoint: Uri,
      timeout: FiniteDuration,
      headers: Headers
  ) extends Builder[F] {

    def setTimeout(timeout: FiniteDuration): Builder[F] =
      copy(timeout = timeout)

    def setEndpoint(endpoint: Uri): Builder[F] =
      copy(endpoint = endpoint)

    def addHeaders(headers: Headers): Builder[F] =
      copy(headers = this.headers ++ headers)

    def build: Resource[F, OtlpHttpSpanExporter[F]] = {
      val config = Config(endpoint, timeout, headers)

      for {
        client <- EmberClientBuilder.default[F].build
      } yield new OtlpHttpSpanExporter[F](client, config)
    }
  }

}
