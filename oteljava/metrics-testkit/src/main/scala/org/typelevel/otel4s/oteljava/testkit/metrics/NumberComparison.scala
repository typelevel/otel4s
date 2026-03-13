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
package metrics

/** A numeric comparison strategy used by metric point expectations.
  *
  * This typeclass controls both equality and rendering for numeric values. The default `Long` instance compares values
  * exactly. The default `Double` instance uses a small tolerance and can be overridden implicitly in a test suite to
  * apply a different threshold globally.
  */
trait NumberComparison[A] {

  /** Returns `true` if the expected and actual values should be treated as equal. */
  def equal(expected: A, actual: A): Boolean

  /** Renders a value for mismatch messages. */
  def render(value: A): String
}

object NumberComparison {

  /** Summons an implicit [[NumberComparison]] instance for `A`. */
  def apply[A](implicit ev: NumberComparison[A]): NumberComparison[A] =
    ev

  /** An exact comparison for `Long` values. */
  implicit val longExact: NumberComparison[Long] = new NumberComparison[Long] {
    def equal(expected: Long, actual: Long): Boolean =
      expected == actual

    def render(value: Long): String =
      value.toString
  }

  /** A default comparison for `Double` values that allows a small absolute tolerance. */
  implicit val doubleDefault: NumberComparison[Double] =
    within(1e-6)

  /** Creates a `Double` comparison that treats values within the given absolute tolerance as equal. */
  def within(tolerance: Double): NumberComparison[Double] =
    new NumberComparison[Double] {
      def equal(expected: Double, actual: Double): Boolean =
        math.abs(expected - actual) <= tolerance

      def render(value: Double): String =
        BigDecimal.decimal(value).bigDecimal.stripTrailingZeros.toPlainString
    }
}
