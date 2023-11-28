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

/*
import cats.effect._
import cats.effect.std.Random
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.instances._
import org.typelevel.otel4s.sdk.trace.SdkTracerProvider
import org.typelevel.otel4s.sdk.trace.exporter.BatchSpanProcessor
import org.typelevel.otel4s.sdk.trace.exporter.InMemorySpanExporter

object TraceSdkExample extends IOApp.Simple {

  def run: IO[Unit] = {
    IOLocal(Context.root).flatMap { implicit local =>
      for {
        exporter <- InMemorySpanExporter.create[IO]
        random <- Random.scalaUtilRandom[IO]
        _ <- BatchSpanProcessor.builder[IO](exporter).build.use { processor =>
          implicit val rnd: Random[IO] = random

          val traceProvider = SdkTracerProvider
            .builder[IO]
            .addSpanProcessor(processor)
            .build

          for {
            tracer <- traceProvider.get("my-tracer")
            _ <- tracer
              .span("test", Attribute("test", "test123"))
              .use(sd => IO.println(sd.context))
          } yield ()
        }
        spans <- exporter.finishedSpans
        _ <- IO.println("Spans: " + spans.toList.mkString("\n"))
      } yield ()
    }
  }

}
 */
