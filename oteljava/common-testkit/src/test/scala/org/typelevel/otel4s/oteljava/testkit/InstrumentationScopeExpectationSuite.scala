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
import io.opentelemetry.sdk.common.InstrumentationScopeInfo
import munit.FunSuite
import org.typelevel.otel4s.Attribute

class InstrumentationScopeExpectationSuite extends FunSuite {

  test("name matches only scope name") {
    assertEquals(InstrumentationScopeExpectation.name("test").check(scope(name = "test")), Right(()))
    assertEquals(
      InstrumentationScopeExpectation.name("test").check(scope(name = "other")),
      Left(NonEmptyList.one(InstrumentationScopeExpectation.Mismatch.nameMismatch("test", "other")))
    )
  }

  test("withVersion matches exact version") {
    assertEquals(
      InstrumentationScopeExpectation.name("test").withVersion("1.0").check(scope(version = Some("1.0"))),
      Right(())
    )
    assertEquals(
      InstrumentationScopeExpectation.name("test").withVersion("1.0").check(scope(version = Some("2.0"))),
      Left(NonEmptyList.one(InstrumentationScopeExpectation.Mismatch.versionMismatch(Some("1.0"), Some("2.0"))))
    )
  }

  test("withVersion(None) requires missing version") {
    val expectation = InstrumentationScopeExpectation.name("test").withVersion(None)

    assertEquals(expectation.check(scope(version = None)), Right(()))
    assertEquals(
      expectation.check(scope(version = Some("1.0"))),
      Left(NonEmptyList.one(InstrumentationScopeExpectation.Mismatch.versionMismatch(None, Some("1.0"))))
    )
  }

  test("withSchemaUrl matches exact schema url") {
    val expectation =
      InstrumentationScopeExpectation.name("test").withSchemaUrl("https://opentelemetry.io/schemas/1.0.0")

    assertEquals(expectation.check(scope(schemaUrl = Some("https://opentelemetry.io/schemas/1.0.0"))), Right(()))
    assertEquals(
      expectation.check(scope(schemaUrl = Some("https://opentelemetry.io/schemas/1.1.0"))),
      Left(
        NonEmptyList.one(
          InstrumentationScopeExpectation.Mismatch.schemaUrlMismatch(
            Some("https://opentelemetry.io/schemas/1.0.0"),
            Some("https://opentelemetry.io/schemas/1.1.0")
          )
        )
      )
    )
  }

  test("withAttributesExact reports nested attribute failures") {
    val expectation =
      InstrumentationScopeExpectation.name("test").withAttributesExact(Attribute("scope.attr", "value"))

    assertEquals(expectation.check(scope(attributes = jAttributes("scope.attr" -> "value"))), Right(()))
    assertEquals(
      expectation.check(scope(attributes = jAttributes("scope.attr" -> "other"))),
      Left(
        NonEmptyList.one(
          InstrumentationScopeExpectation.Mismatch.attributesMismatch(
            NonEmptyList.one(
              AttributesExpectation.Mismatch.attributeValueMismatch(
                Attribute("scope.attr", "value"),
                Attribute("scope.attr", "other")
              )
            )
          )
        )
      )
    )
  }

  test("withAttributesSubset matches contained attributes") {
    val expectation =
      InstrumentationScopeExpectation
        .name("test")
        .withAttributesSubset(Attribute("scope.attr", "value"))

    assertEquals(expectation.check(scope(attributes = jAttributes("scope.attr" -> "value", "other" -> "x"))), Right(()))
    assertEquals(
      expectation.check(scope(attributes = jAttributes("other" -> "x"))),
      Left(
        NonEmptyList.one(
          InstrumentationScopeExpectation.Mismatch.attributesMismatch(
            NonEmptyList.one(AttributesExpectation.Mismatch.missingAttribute(Attribute("scope.attr", "value")))
          )
        )
      )
    )
  }

  test("exact matches the full scope") {
    val actual = scope(
      name = "test",
      version = Some("1.0"),
      schemaUrl = Some("https://opentelemetry.io/schemas/1.0.0"),
      attributes = jAttributes("scope.attr" -> "value")
    )

    assertEquals(InstrumentationScopeExpectation.exact(actual).check(actual), Right(()))
    assertEquals(
      InstrumentationScopeExpectation.exact(actual).check(scope(name = "other")),
      Left(
        NonEmptyList.of(
          InstrumentationScopeExpectation.Mismatch.nameMismatch("test", "other"),
          InstrumentationScopeExpectation.Mismatch.versionMismatch(Some("1.0"), None),
          InstrumentationScopeExpectation.Mismatch.schemaUrlMismatch(
            Some("https://opentelemetry.io/schemas/1.0.0"),
            None
          ),
          InstrumentationScopeExpectation.Mismatch.attributesMismatch(
            NonEmptyList.one(AttributesExpectation.Mismatch.missingAttribute(Attribute("scope.attr", "value")))
          )
        )
      )
    )
  }

  private def scope(
      name: String = "test",
      version: Option[String] = None,
      schemaUrl: Option[String] = None,
      attributes: JAttributes = JAttributes.empty()
  ): InstrumentationScopeInfo = {
    val builder = InstrumentationScopeInfo.builder(name).setAttributes(attributes)
    version.foreach(builder.setVersion)
    schemaUrl.foreach(builder.setSchemaUrl)
    builder.build()
  }

  private def jAttributes(entries: (String, String)*): JAttributes =
    entries
      .foldLeft(JAttributes.builder()) { case (builder, (key, value)) =>
        builder.put(AttributeKey.stringKey(key), value)
      }
      .build()
}
