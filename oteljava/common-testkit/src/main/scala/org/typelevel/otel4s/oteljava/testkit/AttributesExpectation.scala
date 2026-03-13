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

import org.typelevel.otel4s.Attributes

/** A partial expectation for [[Attributes]].
  *
  * Use [[AttributesExpectation.exact]] to require the full attribute set to match or [[AttributesExpectation.subset]]
  * to require only a subset.
  */
sealed trait AttributesExpectation {

  /** Returns `true` if this expectation matches the given attributes. */
  def matches(attributes: Attributes): Boolean
}

object AttributesExpectation {

  /** Creates an expectation that matches only when all attributes are equal. */
  def exact(attributes: Attributes): AttributesExpectation =
    Exact(attributes)

  /** Creates an expectation that matches when all expected attributes are present in the actual set. */
  def subset(attributes: Attributes): AttributesExpectation =
    Subset(attributes)

  /** Creates an expectation that matches only an empty attribute set. */
  def empty: AttributesExpectation =
    exact(Attributes.empty)

  /** Creates an expectation from a custom predicate. */
  def predicate(f: Attributes => Boolean): AttributesExpectation =
    Predicate(f)

  private final case class Exact(expected: Attributes) extends AttributesExpectation {
    def matches(attributes: Attributes): Boolean =
      expected == attributes
  }

  private final case class Subset(expected: Attributes) extends AttributesExpectation {
    def matches(attributes: Attributes): Boolean =
      expected.iterator.forall { attribute =>
        attributes.get(attribute.key).contains(attribute)
      }
  }

  private final case class Predicate(f: Attributes => Boolean) extends AttributesExpectation {
    def matches(attributes: Attributes): Boolean =
      f(attributes)
  }
}
