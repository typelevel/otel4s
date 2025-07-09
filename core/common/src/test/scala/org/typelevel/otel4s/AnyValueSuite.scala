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

import cats.Show
import cats.kernel.laws.discipline.HashTests
import munit.DisciplineSuite
import org.scalacheck.Prop
import org.typelevel.otel4s.scalacheck.Arbitraries
import org.typelevel.otel4s.scalacheck.Cogens

import java.util.Base64

class AnyValueSuite extends DisciplineSuite {
  import Arbitraries.anyValueArbitrary
  import Cogens.anyValueCogen

  checkAll("AnyValue.HashLaws", HashTests[AnyValue].hash)

  test("Show[AnyValue]") {
    Prop.forAll(anyValueArbitrary.arbitrary) { value =>
      def render(v: AnyValue): String = v match {
        case _: AnyValue.EmptyValue             => "EmptyValue"
        case AnyValue.StringValueImpl(value)    => s"StringValue($value)"
        case AnyValue.BooleanValueImpl(value)   => s"BooleanValue($value)"
        case AnyValue.LongValueImpl(value)      => s"LongValue($value)"
        case AnyValue.DoubleValueImpl(value)    => s"DoubleValue($value)"
        case AnyValue.ByteArrayValueImpl(bytes) => s"ByteArrayValue(${Base64.getEncoder.encodeToString(bytes)})"
        case AnyValue.ListValueImpl(values)     => s"ListValue(${values.map(render).mkString("[", ", ", "]")})"
        case AnyValue.MapValueImpl(values) =>
          s"MapValue(${values.map { case (k, v) => s"$k -> ${render(v)}" }.mkString("{", ", ", "}")})"
      }

      val expected = render(value)

      assertEquals(Show[AnyValue].show(value), expected)
      assertEquals(value.toString, expected)
    }
  }
}
