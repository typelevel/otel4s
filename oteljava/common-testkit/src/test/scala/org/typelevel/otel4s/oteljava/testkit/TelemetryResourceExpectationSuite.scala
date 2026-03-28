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

import cats.data.NonEmptyList
import io.opentelemetry.api.common.{Attributes => JAttributes}
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.sdk.resources.Resource
import munit.FunSuite
import org.typelevel.otel4s.Attribute

class TelemetryResourceExpectationSuite extends FunSuite {

  test("empty expectation matches any resource") {
    assertEquals(TelemetryResourceExpectation.any.check(resource()), Right(()))
  }

  test("schemaUrl matches exact schema url") {
    val expectation = TelemetryResourceExpectation.any.schemaUrl("https://opentelemetry.io/schemas/1.0.0")

    assertEquals(expectation.check(resource(schemaUrl = Some("https://opentelemetry.io/schemas/1.0.0"))), Right(()))
    assertEquals(
      expectation.check(resource(schemaUrl = Some("https://opentelemetry.io/schemas/1.1.0"))),
      Left(
        NonEmptyList.one(
          TelemetryResourceExpectation.Mismatch.SchemaUrlMismatch(
            Some("https://opentelemetry.io/schemas/1.0.0"),
            Some("https://opentelemetry.io/schemas/1.1.0")
          )
        )
      )
    )
  }

  test("schemaUrl(None) requires missing schema url") {
    val expectation = TelemetryResourceExpectation.any.schemaUrl(None)

    assertEquals(expectation.check(resource(schemaUrl = None)), Right(()))
    assertEquals(
      expectation.check(resource(schemaUrl = Some("https://opentelemetry.io/schemas/1.0.0"))),
      Left(
        NonEmptyList.one(
          TelemetryResourceExpectation.Mismatch.SchemaUrlMismatch(
            None,
            Some("https://opentelemetry.io/schemas/1.0.0")
          )
        )
      )
    )
  }

  test("attributesExact reports nested attribute failures") {
    val expectation =
      TelemetryResourceExpectation.any.attributesExact(Attribute("service.name", "service"))

    assertEquals(expectation.check(resource(attributes = jAttributes("service.name" -> "service"))), Right(()))
    assertEquals(
      expectation.check(resource(attributes = jAttributes("service.name" -> "other"))),
      Left(
        NonEmptyList.one(
          TelemetryResourceExpectation.Mismatch.AttributesMismatch(
            NonEmptyList.one(
              AttributesExpectation.Mismatch.AttributeValueMismatch(
                Attribute("service.name", "service"),
                Attribute("service.name", "other")
              )
            )
          )
        )
      )
    )
  }

  test("attributesSubset matches contained attributes") {
    val expectation =
      TelemetryResourceExpectation.any
        .attributesSubset(Attribute("service.name", "service"))

    assertEquals(
      expectation.check(resource(attributes = jAttributes("service.name" -> "service", "host.name" -> "localhost"))),
      Right(())
    )
    assertEquals(
      expectation.check(resource(attributes = jAttributes("host.name" -> "localhost"))),
      Left(
        NonEmptyList.one(
          TelemetryResourceExpectation.Mismatch.AttributesMismatch(
            NonEmptyList.one(AttributesExpectation.Mismatch.MissingAttribute(Attribute("service.name", "service")))
          )
        )
      )
    )
  }

  test("exact matches the full resource") {
    val actual = resource(
      attributes = jAttributes("service.name" -> "service"),
      schemaUrl = Some("https://opentelemetry.io/schemas/1.0.0")
    )

    assertEquals(TelemetryResourceExpectation.exact(actual).check(actual), Right(()))
    assertEquals(
      TelemetryResourceExpectation.exact(actual).check(resource(attributes = jAttributes("service.name" -> "other"))),
      Left(
        NonEmptyList.of(
          TelemetryResourceExpectation.Mismatch.AttributesMismatch(
            NonEmptyList.one(
              AttributesExpectation.Mismatch.AttributeValueMismatch(
                Attribute("service.name", "service"),
                Attribute("service.name", "other")
              )
            )
          ),
          TelemetryResourceExpectation.Mismatch.SchemaUrlMismatch(
            Some("https://opentelemetry.io/schemas/1.0.0"),
            None
          )
        )
      )
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
