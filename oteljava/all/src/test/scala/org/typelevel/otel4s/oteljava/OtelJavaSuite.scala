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

package org.typelevel.otel4s.oteljava

import cats.effect.IO
import io.opentelemetry.sdk.{OpenTelemetrySdk => JOpenTelemetrySdk}
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import munit.CatsEffectSuite
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.oteljava.context.Context

class OtelJavaSuite extends CatsEffectSuite {

  test("contextManager - capture the actual span and baggage for deferred work") {
    val exporter = InMemorySpanExporter.create()
    val attribute = Attribute("restored", true)

    OtelJava
      .resource[IO](IO {
        val processor = SimpleSpanProcessor.create(exporter)
        val provider = SdkTracerProvider.builder().addSpanProcessor(processor).build()
        JOpenTelemetrySdk.builder().setTracerProvider(provider).build()
      })
      .use { otel4s =>
        val manager = otel4s.contextManager
        val baggage = otel4s.baggageManager

        for {
          tracer <- otel4s.tracerProvider.get("context-manager")
          _ <- tracer.span("captured").use { capturedSpan =>
            for {
              restore <- baggage.local(_.updated("key", "captured"))(manager.captureScope)
              _ <- tracer.span("consumer").use { consumerSpan =>
                baggage.local(_.updated("key", "consumer")) {
                  for {
                    _ <- restore(tracer.currentSpanContext).assertEquals(Some(capturedSpan.context))
                    _ <- restore(baggage.getValue("key")).assertEquals(Some("captured"))
                    _ <- restore(tracer.currentSpanOrThrow.flatMap(_.addAttribute(attribute)))
                    _ <- tracer.currentSpanContext.assertEquals(Some(consumerSpan.context))
                    _ <- baggage.getValue("key").assertEquals(Some("consumer"))
                    context <- manager.current
                    local <- otel4s.localContext.ask[Context]
                    _ <- IO(assertEquals(context, local))
                  } yield ()
                }
              }
            } yield ()
          }
          spans <- IO(exporter.getFinishedSpanItems)
        } yield {
          val key = io.opentelemetry.api.common.AttributeKey.booleanKey("restored")
          val captured = spans.get(1)
          val consumer = spans.get(0)
          assertEquals(captured.getName, "captured")
          assertEquals(captured.getAttributes.get(key), java.lang.Boolean.TRUE)
          assertEquals(consumer.getName, "consumer")
          assert(!consumer.getAttributes.asMap().containsKey(key))
        }
      }
  }

  test("OtelJava toString returns useful info") {
    val testSdk: JOpenTelemetrySdk = JOpenTelemetrySdk.builder().build()
    OtelJava
      .fromJOpenTelemetry[IO](testSdk)
      .map(testOtel4s => {
        val res = testOtel4s.toString()
        assert(clue(res).contains("OpenTelemetrySdk"))
      })
  }
}
