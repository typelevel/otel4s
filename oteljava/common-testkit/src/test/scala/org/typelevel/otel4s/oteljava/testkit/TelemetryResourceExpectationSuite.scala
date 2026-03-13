/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.oteljava.testkit

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.{Attributes => JAttributes}
import io.opentelemetry.sdk.resources.Resource
import munit.FunSuite
import org.typelevel.otel4s.{Attribute, Attributes}

class TelemetryResourceExpectationSuite extends FunSuite {

  test("empty expectation matches any resource") {
    assert(TelemetryResourceExpectation.any.matches(resource()))
  }

  test("withSchemaUrl matches exact schema url") {
    val expectation = TelemetryResourceExpectation.any.withSchemaUrl("https://opentelemetry.io/schemas/1.0.0")

    assert(expectation.matches(resource(schemaUrl = Some("https://opentelemetry.io/schemas/1.0.0"))))
    assert(!expectation.matches(resource(schemaUrl = Some("https://opentelemetry.io/schemas/1.1.0"))))
  }

  test("withSchemaUrl(None) requires missing schema url") {
    val expectation = TelemetryResourceExpectation.any.withSchemaUrl(None)

    assert(expectation.matches(resource(schemaUrl = None)))
    assert(!expectation.matches(resource(schemaUrl = Some("https://opentelemetry.io/schemas/1.0.0"))))
  }

  test("withAttributesExact matches exact attributes") {
    val expectation =
      TelemetryResourceExpectation.any.withAttributesExact(Attributes(Attribute("service.name", "service")))

    assert(expectation.matches(resource(attributes = jAttributes("service.name" -> "service"))))
    assert(!expectation.matches(resource(attributes = jAttributes("service.name" -> "other"))))
  }

  test("withAttributesSubset matches contained attributes") {
    val expectation =
      TelemetryResourceExpectation.any
        .withAttributesSubset(Attributes(Attribute("service.name", "service")))

    assert(
      expectation.matches(resource(attributes = jAttributes("service.name" -> "service", "host.name" -> "localhost")))
    )
    assert(!expectation.matches(resource(attributes = jAttributes("host.name" -> "localhost"))))
  }

  test("exact matches the full resource") {
    val actual = resource(
      attributes = jAttributes("service.name" -> "service"),
      schemaUrl = Some("https://opentelemetry.io/schemas/1.0.0")
    )

    assert(TelemetryResourceExpectation.exact(actual).matches(actual))
    assert(
      !TelemetryResourceExpectation.exact(actual).matches(resource(attributes = jAttributes("service.name" -> "other")))
    )
  }

  private def resource(
      attributes: JAttributes = JAttributes.empty(),
      schemaUrl: Option[String] = None
  ): Resource =
    Resource.create(attributes, schemaUrl.orNull)

  private def jAttributes(entries: (String, String)*): JAttributes =
    entries
      .foldLeft(JAttributes.builder()) { case (builder, (key, value)) =>
        builder.put(AttributeKey.stringKey(key), value)
      }
      .build()
}
