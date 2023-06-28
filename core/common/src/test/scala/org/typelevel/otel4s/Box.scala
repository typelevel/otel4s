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

package org.typelevel.otel4s

import cats.Eq
import cats.laws.discipline.ExhaustiveCheck
import org.scalacheck.Arbitrary
import org.scalacheck.Cogen

/** A simple wrapper around a value. */
final case class Box[A](value: A)

object Box {
  implicit def arbitrary[A: Arbitrary]: Arbitrary[Box[A]] =
    Arbitrary(Arbitrary.arbitrary[A].map(Box(_)))

  implicit def cogen[A: Cogen]: Cogen[Box[A]] =
    Cogen[A].contramap(_.value)

  implicit def boxEq[A: Eq]: Eq[Box[A]] =
    (x: Box[A], y: Box[A]) => Eq[A].eqv(x.value, y.value)

  implicit def exhaustiveCheck[A: ExhaustiveCheck]: ExhaustiveCheck[Box[A]] =
    ExhaustiveCheck.instance(ExhaustiveCheck[A].allValues.map(apply))
}
