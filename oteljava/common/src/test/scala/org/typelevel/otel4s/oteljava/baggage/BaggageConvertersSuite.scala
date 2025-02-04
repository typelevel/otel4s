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

package org.typelevel.otel4s.oteljava.baggage

import io.opentelemetry.api.baggage.{Baggage => JBaggage}
import io.opentelemetry.api.baggage.BaggageEntry
import io.opentelemetry.api.baggage.BaggageEntryMetadata
import munit.FunSuite
import org.typelevel.otel4s.baggage.Baggage
import org.typelevel.otel4s.oteljava.baggage.BaggageConverters._

class BaggageConvertersSuite extends FunSuite {
  import BaggageConvertersSuite._

  test("conversion to and from OpenTelemetry Baggage") {
    val baggage = Baggage.empty
      .updated("key", "value")
      .updated("foo", "bar", "baz")
    val jBaggage = JBaggage
      .builder()
      .put("key", "value")
      .put("foo", "bar", BaggageEntryMetadata.create("baz"))
      .build()

    assertEquals(baggage.toJava, jBaggage)
    assertEquals(jBaggage.toScala, baggage)
    assertEquals(baggage.toJava.toScala, baggage)
    assertEquals(jBaggage.toScala.toJava, jBaggage)
  }

  test("conversion from OpenTelemetry BaggageEntry") {
    assertEquals(jBaggageEntry("foo").toScala, Baggage.Entry("foo", None))
    assertEquals(
      jBaggageEntry("bar", "baz").toScala,
      Baggage.Entry("bar", Some(Baggage.Metadata("baz")))
    )
    assertEquals(jBaggageEntry("qux", "").toScala, Baggage.Entry("qux", None))
  }
}

private object BaggageConvertersSuite {
  def jBaggageEntry(value: String): BaggageEntry =
    JBaggage.builder().put("key", value).build().getEntry("key")
  def jBaggageEntry(value: String, metadata: String): BaggageEntry =
    JBaggage
      .builder()
      .put("key", value, BaggageEntryMetadata.create(metadata))
      .build()
      .getEntry("key")
}
