/*
 * Copyright 2022 Typelevel
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

package com.example.tracing

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.{Resource => OtelResource}
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.BatchSpanProcessor
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.Otel4s
import org.typelevel.otel4s.java.OtelJava
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.Tracer

import scala.concurrent.duration._

/** Run jaeger-all-in-one image:
  * {{{
  *  docker run -d --name jaeger-132 \
  *    -e COLLECTOR_ZIPKIN_HOST_PORT=:9411 \
  *    -p 5775:5775/udp \
  *    -p 6831:6831/udp \
  *    -p 6832:6832/udp \
  *    -p 5778:5778 \
  *    -p 16686:16686 \
  *    -p 14250:14250 \
  *    -p 14268:14268 \
  *    -p 14269:14269 \
  *    -p 9411:9411 \
  *    jaegertracing/all-in-one:1.32
  * }}}
  */
object JaegerTracing extends IOApp.Simple {

  def run: IO[Unit] =
    makeOtel4s.use { otel =>
      for {
        tracer <- otel.tracerProvider
          .tracer("my-tracer")
          .get
        _ <- tracer
          .resourceSpan("resource-span")(makeImageLookup(tracer))
          .use { case Span.Res(lookup) =>
            lookup.exists("my-image")
          }
      } yield ()
    }

  private def makeImageLookup(tracer: Tracer[IO]): Resource[IO, ImageLookup] = {
    val acquire = IO.delay {
      new ImageLookup {
        def exists(imageId: String): IO[Boolean] =
          tracer
            .span(
              "image-exists",
              Attribute(AttributeKey.string("image-id"), imageId)
            )
            .surround(IO.pure(false))
      }
    }

    Resource.make(acquire.delayBy(200.millis))(_ => IO.sleep(300.millis))
  }

  trait ImageLookup {
    def exists(imageId: String): IO[Boolean]
  }

  private def makeOtel4s: Resource[IO, Otel4s[IO]] = {
    val mkTracerProvider = IO.delay {
      val jaegerExporter = JaegerGrpcSpanExporter
        .builder()
        .setEndpoint("http://localhost:14250")
        .build()

      SdkTracerProvider
        .builder()
        .addSpanProcessor(BatchSpanProcessor.builder(jaegerExporter).build())
        .setResource(
          OtelResource.create(
            Attributes
              .builder()
              .put(ResourceAttributes.SERVICE_NAME, "my-service")
              .build()
          )
        )
        .build()
    }

    for {
      tracerProvider <- Resource.fromAutoCloseable(mkTracerProvider)
      sdk <- Resource.pure {
        OpenTelemetrySdk
          .builder()
          .setTracerProvider(tracerProvider)
          .build()
      }
      otel <- Resource.eval(OtelJava.forSync(sdk))
    } yield otel
  }
}
