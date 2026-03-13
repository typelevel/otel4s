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

import munit.FunSuite
import org.typelevel.otel4s.{Attribute, Attributes}

class AttributesExpectationSuite extends FunSuite {

  test("exact matches equal attributes") {
    val attributes = Attributes(Attribute("http.method", "GET"))

    assert(AttributesExpectation.exact(attributes).matches(attributes))
  }

  test("exact fails when attributes differ") {
    val actual = Attributes(
      Attribute("http.method", "GET"),
      Attribute("http.route", "/users")
    )

    assert(!AttributesExpectation.exact(Attributes(Attribute("http.method", "GET"))).matches(actual))
  }

  test("subset matches contained attributes") {
    val actual = Attributes(
      Attribute("http.method", "GET"),
      Attribute("http.route", "/users")
    )

    assert(AttributesExpectation.subset(Attributes(Attribute("http.method", "GET"))).matches(actual))
  }

  test("subset fails when an expected attribute is missing") {
    val actual = Attributes(Attribute("http.method", "GET"))

    assert(!AttributesExpectation.subset(Attributes(Attribute("http.route", "/users"))).matches(actual))
  }

  test("empty matches only empty attributes") {
    assert(AttributesExpectation.empty.matches(Attributes.empty))
    assert(!AttributesExpectation.empty.matches(Attributes(Attribute("http.method", "GET"))))
  }

  test("predicate delegates to custom function") {
    val expectation = AttributesExpectation.predicate { attributes =>
      attributes.iterator.map(_.key.name).toSet == Set("http.method")
    }

    assert(expectation.matches(Attributes(Attribute("http.method", "GET"))))
    assert(!expectation.matches(Attributes(Attribute("http.route", "/users"))))
  }
}
