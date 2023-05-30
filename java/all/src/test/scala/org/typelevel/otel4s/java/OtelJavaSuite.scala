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
import io.opentelemetry.api.{OpenTelemetry => JOpenTelemetry}
import io.opentelemetry.sdk.{OpenTelemetrySdk => JOpenTelemetrySdk}
import org.typelevel.otel4s.Otel4s
import org.typelevel.otel4s.java.OtelJava
import org.typelevel.otel4s.ContextPropagators
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.trace.TracerProvider
import munit.CatsEffectSuite

class OtelJavaSuite extends CatsEffectSuite {

  test("OtelJava toString returns useful info") {

    val testSdk = JOpenTelemetrySdk.builder().build()
    val testOtel4s = new Otel4s[IO] {
      def propagators: ContextPropagators[IO] =
        testSdk.getPropagators() // TODO: resolve
      def meterProvider: MeterProvider[IO] = testSdk.getMeterProvider()
      def tracerProvider: TracerProvider[IO] = testSdk.getTracerProvider()

      override def toString: String = testSdk.toString()
    }

    assertEquals(
      testOtel4s.toString(),
      "some string here"
    )
  }
}
