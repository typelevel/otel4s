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

package org.typelevel.otel4s.semconv

import org.typelevel.otel4s.AttributeKey

/** The attribute's specification.
  *
  * @tparam A
  *   the type of the attribute
  */
sealed trait AttributeSpec[A] {

  /** The attribute's key.
    */
  def key: AttributeKey[A]

  /** The list of value examples.
    */
  def examples: List[A]

  /** The requirement level of the attribute.
    */
  def requirement: Requirement

  /** The stability of the attribute.
    */
  def stability: Stability
}

object AttributeSpec {

  def apply[A](
      key: AttributeKey[A],
      examples: List[A],
      requirement: Requirement,
      stability: Stability
  ): AttributeSpec[A] =
    Impl(key, examples, requirement, stability)

  final case class Impl[A](
      key: AttributeKey[A],
      examples: List[A],
      requirement: Requirement,
      stability: Stability
  ) extends AttributeSpec[A]

}
