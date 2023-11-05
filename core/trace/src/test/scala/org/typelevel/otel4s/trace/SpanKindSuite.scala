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

package org.typelevel.otel4s.trace

import cats.Show
import cats.kernel.laws.discipline.HashTests
import munit._
import org.scalacheck.Arbitrary
import org.scalacheck.Cogen
import org.scalacheck.Gen
import org.scalacheck.Prop

class SpanKindSuite extends DisciplineSuite {

  private val spanKindGen: Gen[SpanKind] =
    Gen.oneOf(
      SpanKind.Internal,
      SpanKind.Server,
      SpanKind.Client,
      SpanKind.Producer,
      SpanKind.Consumer
    )

  private implicit val spanKindArbitrary: Arbitrary[SpanKind] =
    Arbitrary(spanKindGen)

  private implicit val spanKindCogen: Cogen[SpanKind] =
    Cogen[String].contramap(_.toString)

  checkAll("SpanKind.HashLaws", HashTests[SpanKind].hash)

  property("Show[SpanKind]") {
    Prop.forAll(spanKindGen) { spanKind =>
      val expected = spanKind match {
        case SpanKind.Internal => "Internal"
        case SpanKind.Server   => "Server"
        case SpanKind.Client   => "Client"
        case SpanKind.Producer => "Producer"
        case SpanKind.Consumer => "Consumer"
      }

      assertEquals(Show[SpanKind].show(spanKind), expected)
    }
  }

}
