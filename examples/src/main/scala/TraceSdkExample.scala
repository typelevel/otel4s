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

import cats.effect._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.sdk.OpenTelemetrySdk
import org.typelevel.otel4s.sdk.exporter.otlp.trace.autoconfigure.OtlpSpanExporterAutoConfigure

object TraceSdkExample extends IOApp.Simple {

  def run: IO[Unit] =
    OpenTelemetrySdk
      .autoConfigured[IO](
        _.addExporterConfigurer(OtlpSpanExporterAutoConfigure[IO])
      )
      .use { autoConfigured =>
        val sdk = autoConfigured.sdk

        for {
          tracer <- sdk.tracerProvider.get("my-tracer")
          _ <- tracer
            .span("test", Attribute("test", "test123"))
            .use(sd => IO.println(sd.context))
        } yield ()
      }

}
