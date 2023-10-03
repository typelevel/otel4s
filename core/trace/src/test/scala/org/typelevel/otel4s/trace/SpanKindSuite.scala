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

class SpanKindSuite extends ScalaCheckSuite {

  private val spanKindGen: Gen[SpanKind] =
    Gen.oneOf(
      SpanKind.Internal,
      SpanKind.Server,
      SpanKind.Client,
      SpanKind.Producer,
      SpanKind.Consumer
    )

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

  test("Hash[SpanKind]") {
    val allWithIndex = List(
      SpanKind.Internal,
      SpanKind.Server,
      SpanKind.Client,
      SpanKind.Producer,
      SpanKind.Consumer
    ).zipWithIndex

    allWithIndex.foreach { case (that, thatIdx) =>
      allWithIndex.foreach { case (other, otherIdx) =>
        assertEquals(Hash[SpanKind].eqv(that, other), thatIdx == otherIdx)
      }
    }
  }

}
