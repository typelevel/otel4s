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

import cats.Hash
import cats.Show
import munit._
import org.scalacheck.Gen
import org.scalacheck.Prop

class StatusSuite extends ScalaCheckSuite {

  private val statusGen: Gen[Status] =
    Gen.oneOf(Status.Unset, Status.Ok, Status.Error)

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

  test("Hash[Status]") {
    val allWithIndex = List(Status.Unset, Status.Ok, Status.Error).zipWithIndex

    allWithIndex.foreach { case (that, thatIdx) =>
      allWithIndex.foreach { case (other, otherIdx) =>
        assertEquals(Hash[Status].eqv(that, other), thatIdx == otherIdx)
      }
    }
  }

}
