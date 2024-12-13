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
import org.scalacheck.Prop
import org.typelevel.otel4s.trace.scalacheck.Arbitraries._
import org.typelevel.otel4s.trace.scalacheck.Cogens._
import org.typelevel.otel4s.trace.scalacheck.Gens

class SpanKindSuite extends DisciplineSuite {

  checkAll("SpanKind.HashLaws", HashTests[SpanKind].hash)

  property("Show[SpanKind]") {
    Prop.forAll(Gens.spanKind) { spanKind =>
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
