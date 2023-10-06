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

package org.typelevel.otel4s.context.propagation

import cats.Eq
import cats.kernel.laws.discipline.MonoidTests
import munit._
import org.scalacheck.{Arbitrary, Gen}

class TextMapPropagatorLawTests extends DisciplineSuite {

  implicit val textMapPropagatorEq: Eq[TextMapPropagator[String]] =
    Eq.fromUniversalEquals

  implicit val textMapPropagatorArb: Arbitrary[TextMapPropagator[String]] =
    Arbitrary(
      for {
        fields <- Gen.listOf(Gen.alphaNumStr)
        name <- Gen.alphaNumStr
      } yield TestPropagator(fields, name)
    )

  checkAll(
    "TextMapPropagator.MonoidLaws",
    MonoidTests[TextMapPropagator[String]].monoid
  )

  private case class TestPropagator[Ctx](
      fields: List[String],
      name: String
  ) extends TextMapPropagator[Ctx] {
    def extract[A: TextMapGetter](ctx: Ctx, carrier: A): Ctx = ctx

    def inject[A: TextMapUpdater](ctx: Ctx, carrier: A): A = carrier

    override def toString: String = name
  }
}
