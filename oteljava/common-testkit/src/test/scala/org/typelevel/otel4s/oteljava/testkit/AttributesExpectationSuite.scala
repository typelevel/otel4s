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
import munit.FunSuite
import org.typelevel.otel4s.{Attribute, Attributes}

class AttributesExpectationSuite extends FunSuite {

  test("exact matches equal attributes") {
    val attributes = Attributes(Attribute("http.method", "GET"))

    assertEquals(AttributesExpectation.exact(attributes).check(attributes), Right(()))
  }

  test("exact reports mismatched and unexpected attributes") {
    val actual = Attributes(
      Attribute("http.method", "GET"),
      Attribute("http.route", "/users")
    )

    assertEquals(
      AttributesExpectation.exact(Attributes(Attribute("http.method", "POST"))).check(actual),
      Left(
        NonEmptyList.of(
          AttributesExpectation.Mismatch.attributeValueMismatch(
            Attribute("http.method", "POST"),
            Attribute("http.method", "GET")
          ),
          AttributesExpectation.Mismatch.unexpectedAttribute(Attribute("http.route", "/users"))
        )
      )
    )
  }

  test("subset matches contained attributes") {
    val actual = Attributes(
      Attribute("http.method", "GET"),
      Attribute("http.route", "/users")
    )

    assertEquals(
      AttributesExpectation.subset(Attributes(Attribute("http.method", "GET"))).check(actual),
      Right(())
    )
  }

  test("subset reports missing expected attribute") {
    val actual = Attributes(Attribute("http.method", "GET"))

    assertEquals(
      AttributesExpectation.subset(Attributes(Attribute("http.route", "/users"))).check(actual),
      Left(NonEmptyList.one(AttributesExpectation.Mismatch.missingAttribute(Attribute("http.route", "/users"))))
    )
  }

  test("empty matches only empty attributes") {
    assertEquals(AttributesExpectation.empty.check(Attributes.empty), Right(()))
    assertEquals(
      AttributesExpectation.empty.check(Attributes(Attribute("http.method", "GET"))),
      Left(NonEmptyList.one(AttributesExpectation.Mismatch.unexpectedAttribute(Attribute("http.method", "GET"))))
    )
  }

  test("predicate delegates to custom function and reports failure") {
    val expectation = AttributesExpectation.where { attributes =>
      attributes.map(_.key.name).toSet == Set("http.method")
    }

    assertEquals(expectation.check(Attributes(Attribute("http.method", "GET"))), Right(()))
    assertEquals(
      expectation.check(Attributes(Attribute("http.route", "/users"))),
      Left(NonEmptyList.one(AttributesExpectation.Mismatch.predicateFailed(None)))
    )
  }

  test("predicate clue is included in mismatches and formatted output") {
    val expectation = AttributesExpectation.where("only http.method is expected") { attributes =>
      attributes.map(_.key.name).toSet == Set("http.method")
    }

    val mismatch = AttributesExpectation.Mismatch.predicateFailed(Some("only http.method is expected"))

    assertEquals(
      expectation.check(Attributes(Attribute("http.route", "/users"))),
      Left(NonEmptyList.one(mismatch))
    )
    assertEquals(
      mismatch.message,
      "attributes predicate returned false: only http.method is expected"
    )
  }
}
