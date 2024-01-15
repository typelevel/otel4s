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

package org.typelevel.otel4s.metrics

import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes

sealed trait Measurement[A] {

  /** The value to record.
    */
  def value: A

  /** The set of attributes to associate with the value.
    */
  def attributes: Attributes
}

object Measurement {

  def apply[A](value: A): Measurement[A] =
    Impl(value, Attributes.empty)

  def apply[A](value: A, attributes: Attributes): Measurement[A] =
    Impl(value, attributes)

  def apply[A](value: A, attributes: Iterable[Attribute[_]]): Measurement[A] =
    Impl(value, Attributes.fromSpecific(attributes))

  def apply[A](value: A, attributes: Attribute[_]*): Measurement[A] =
    Impl(value, Attributes.fromSpecific(attributes))

  private final case class Impl[A](
      value: A,
      attributes: Attributes
  ) extends Measurement[A]
}
