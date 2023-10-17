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

package org.typelevel.otel4s.java

import cats.effect.IO
import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.context.{Context => JContext}
import io.opentelemetry.sdk.{OpenTelemetrySdk => JOpenTelemetrySdk}
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import munit.CatsEffectSuite

class OtelJavaSuite extends CatsEffectSuite {

  test("OtelJava toString returns useful info") {
    val testSdk: JOpenTelemetrySdk = JOpenTelemetrySdk.builder().build()
    OtelJava
      .forAsync[IO](testSdk)
      .map { testOtel4s =>
        val res = testOtel4s.toString()
        assert(clue(res).startsWith("OpenTelemetrySdk"))
      }
  }

  test("interop with java") {
    val sdk = createSdk

    OtelJava
      .forAsync[IO](sdk)
      .map { otel4s =>
        val getCurrentSpan = otel4s.useJContextUnsafe(_ => JSpan.current())

        val jTracer = sdk.getTracer("tracer")
        val span = jTracer.spanBuilder("test").startSpan()

        span.storeInContext(JContext.current()).makeCurrent()

        val ioSpan = otel4s
          .withJContext(JContext.current())(getCurrentSpan)
          .unsafeRunSync()

        span.end()

        assertEquals(span, ioSpan)
      }
  }

  private def createSdk: JOpenTelemetrySdk = {
    val exporter = InMemorySpanExporter.create()

    val builder = SdkTracerProvider
      .builder()
      .addSpanProcessor(SimpleSpanProcessor.create(exporter))

    JOpenTelemetrySdk
      .builder()
      .setTracerProvider(builder.build())
      .build()
  }

}
