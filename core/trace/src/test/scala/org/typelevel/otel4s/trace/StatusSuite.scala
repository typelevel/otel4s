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

class StatusSuite extends DisciplineSuite {

  private val statusGen: Gen[Status] =
    Gen.oneOf(Status.Unset, Status.Ok, Status.Error)

  private implicit val statusArbitrary: Arbitrary[Status] =
    Arbitrary(statusGen)

  private implicit val statusCogen: Cogen[Status] =
    Cogen[String].contramap(_.toString)

  checkAll("Status.HashLaws", HashTests[Status].hash)

  property("Show[Status]") {
    Prop.forAll(statusGen) { status =>
      val expected = status match {
        case Status.Unset => "Unset"
        case Status.Ok    => "Ok"
        case Status.Error => "Error"
      }

      assertEquals(Show[Status].show(status), expected)
    }
  }

}
