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

import cats.effect.IO
import cats.effect.IOApp
import io.opentelemetry.api.trace.{Span => JSpan}
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.oteljava.IOLocalContextStorage
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.oteljava.context.Context

object ContextStorageExample extends IOApp.Simple {

  def run: IO[Unit] = {
    implicit val provider: LocalProvider[IO, Context] = IOLocalContextStorage.localProvider[IO]
    OtelJava.autoConfigured[IO]().use { otelJava =>
      otelJava.tracerProvider.tracer("").get.flatMap { tracer =>
        tracer.span("test").use { span => // start 'test' span using otel4s
          val jSpanContext = JSpan.current().getSpanContext // get a span from a ThreadLocal var
          IO.println(s"jCtx: ${jSpanContext}, Otel4s ctx: ${span.context}")
        }
      }
    }
  }

}
