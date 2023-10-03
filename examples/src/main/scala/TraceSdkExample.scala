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

import cats.Monad
import cats.Semigroup
import cats.effect._
import cats.effect.std.Console
import cats.effect.std.Random
import cats.syntax.apply._
import cats.syntax.flatMap._
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.exporter.otlp.OtlpHttpSpanExporter
import org.typelevel.otel4s.sdk.instances._
import org.typelevel.otel4s.sdk.trace.SdkTracerProvider
import org.typelevel.otel4s.sdk.trace.exporter.BatchSpanProcessor
import org.typelevel.otel4s.sdk.trace.exporter.InMemorySpanExporter
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter
import org.typelevel.otel4s.trace.Tracer

import scala.concurrent.duration._

object TraceSdkExample extends IOApp.Simple {

  trait Work[F[_]] {
    def request(headers: Map[String, String]): F[Unit]
  }

  object Work {
    def apply[F[_]: Monad: Tracer: Console]: Work[F] =
      new Work[F] {
        def request(headers: Map[String, String]): F[Unit] = {
          Tracer[F].currentSpanContext.flatMap { current =>
            Tracer[F].joinOrRoot(headers) {
              val builder = Tracer[F].spanBuilder("Work.DoWork")
              current.fold(builder)(builder.addLink(_)).build.use { span =>
                Tracer[F].currentSpanContext
                  .flatMap(ctx => Console[F].println("Context is " + ctx)) *>
                  span.addEvent("Starting the work.") *>
                  doWorkInternal *>
                  span.addEvent("Finished working.")
              }
            }
          }
        }

        def doWorkInternal() =
          Tracer[F]
            .span("Work.InternalWork")
            .surround(
              Console[F].println("Doin' work")
            )
      }
  }

  def run: IO[Unit] = {
    IOLocal(Context.root).flatMap { implicit local =>
      OtlpHttpSpanExporter.builder[IO].build.use { otlpExporter =>
        for {
          inMemory <- InMemorySpanExporter.create[IO]
          random <- Random.scalaUtilRandom[IO]
          exporter = Semigroup[SpanExporter[IO]].combine(inMemory, otlpExporter)
          _ <- BatchSpanProcessor.builder[IO](exporter).build.use { processor =>
            implicit val rnd: Random[IO] = random

            val traceProvider = SdkTracerProvider
              .builder[IO]
              .addSpanProcessor(processor)
              .build

            for {
              tracer <- traceProvider.get("my-tracer")
              _ <- tracer
                .span("resource")
                .resource
                .use { res =>
                  implicit val t: Tracer[IO] = tracer

                  res.trace {
                    for {
                      _ <- tracer.span("acquire").surround(IO.sleep(50.millis))
                      _ <- tracer.span("use").surround {
                        Work[IO].request(
                          Map(
                            "X-B3-TraceId" -> "80f198ee56343ba864fe8b2a57d3eff7",
                            "X-B3-ParentSpanId" -> "05e3ac9a4f6e3b90",
                            "X-B3-SpanId" -> "e457b5a2e4d86bd1",
                            "X-B3-Sampled" -> "1"
                          )
                        )
                      }
                      _ <- res.span.addEvent("event")
                      _ <- tracer.span("release").surround(IO.sleep(100.millis))
                    } yield ()
                  }
                }
            } yield ()
          }
          spans <- inMemory.finishedSpans
          _ <- IO.println("Spans: " + spans.toList.mkString("\n"))
        } yield ()
      }
    }
  }

}
