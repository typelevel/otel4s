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

import munit._

class TextMapPropagatorSuite extends FunSuite {

  test("create a no-op instance") {
    val propagator = TextMapPropagator.noop

    assertEquals(propagator.fields, Nil)
    assertEquals(propagator.toString, "TextMapPropagator.Noop")
  }

  test("composite (empty input) - use noop") {
    val composite = TextMapPropagator.composite()

    assertEquals(composite.fields, Nil)
    assertEquals(composite.toString, "TextMapPropagator.Noop")
  }

  test("composite (single input) - use this input") {
    val fields = List("a", "b", "c")
    val propagator = new TestPropagator[String](fields, "TestPropagator")
    val composite = TextMapPropagator.composite(propagator)

    assertEquals(composite.fields, fields)
    assertEquals(composite.toString, "TestPropagator")
  }

  test("composite (multiple) - create a multi instance") {
    val fieldsA = List("a", "b")
    val fieldsB = List("c", "d")

    val propagatorA = new TestPropagator[String](fieldsA, "PropagatorA")
    val propagatorB = new TestPropagator[String](fieldsB, "PropagatorB")
    val composite = TextMapPropagator.composite(propagatorA, propagatorB)

    assertEquals(composite.fields, fieldsA ++ fieldsB)
    assertEquals(
      composite.toString,
      "TextMapPropagator.Multi(PropagatorA, PropagatorB)"
    )
  }

  private final class TestPropagator[Ctx](
      val fields: List[String],
      name: String
  ) extends TextMapPropagator[Ctx] {
    def extract[A: TextMapGetter](ctx: Ctx, carrier: A): Ctx = ctx
    def inject[A: TextMapUpdater](ctx: Ctx, carrier: A): A = carrier
    override def toString: String = name
  }
}
