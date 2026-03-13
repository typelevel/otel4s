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
import io.opentelemetry.sdk.common.InstrumentationScopeInfo
import munit.FunSuite
import org.typelevel.otel4s.{Attribute, Attributes}

class InstrumentationScopeExpectationSuite extends FunSuite {

  test("name matches only scope name") {
    assert(InstrumentationScopeExpectation.name("test").matches(scope(name = "test")))
    assert(!InstrumentationScopeExpectation.name("test").matches(scope(name = "other")))
  }

  test("withVersion matches exact version") {
    assert(InstrumentationScopeExpectation.name("test").withVersion("1.0").matches(scope(version = Some("1.0"))))
    assert(!InstrumentationScopeExpectation.name("test").withVersion("1.0").matches(scope(version = Some("2.0"))))
  }

  test("withVersion(None) requires missing version") {
    val expectation = InstrumentationScopeExpectation.name("test").withVersion(None)

    assert(expectation.matches(scope(version = None)))
    assert(!expectation.matches(scope(version = Some("1.0"))))
  }

  test("withSchemaUrl matches exact schema url") {
    val expectation =
      InstrumentationScopeExpectation.name("test").withSchemaUrl("https://opentelemetry.io/schemas/1.0.0")

    assert(expectation.matches(scope(schemaUrl = Some("https://opentelemetry.io/schemas/1.0.0"))))
    assert(!expectation.matches(scope(schemaUrl = Some("https://opentelemetry.io/schemas/1.1.0"))))
  }

  test("withAttributesExact matches exact attributes") {
    val expectation =
      InstrumentationScopeExpectation.name("test").withAttributesExact(Attributes(Attribute("scope.attr", "value")))

    assert(expectation.matches(scope(attributes = jAttributes("scope.attr" -> "value"))))
    assert(!expectation.matches(scope(attributes = jAttributes("scope.attr" -> "other"))))
  }

  test("withAttributesSubset matches contained attributes") {
    val expectation =
      InstrumentationScopeExpectation
        .name("test")
        .withAttributesSubset(Attributes(Attribute("scope.attr", "value")))

    assert(expectation.matches(scope(attributes = jAttributes("scope.attr" -> "value", "other" -> "x"))))
    assert(!expectation.matches(scope(attributes = jAttributes("other" -> "x"))))
  }

  test("exact matches the full scope") {
    val actual = scope(
      name = "test",
      version = Some("1.0"),
      schemaUrl = Some("https://opentelemetry.io/schemas/1.0.0"),
      attributes = jAttributes("scope.attr" -> "value")
    )

    assert(InstrumentationScopeExpectation.exact(actual).matches(actual))
    assert(!InstrumentationScopeExpectation.exact(actual).matches(scope(name = "other")))
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
