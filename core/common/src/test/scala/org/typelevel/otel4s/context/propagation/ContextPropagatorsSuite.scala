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

class ContextPropagatorsSuite extends FunSuite {

  test("create a no-op instance") {
    val propagators = ContextPropagators.noop

    assertEquals(propagators.textMapPropagator.fields, Nil)
    assertEquals(
      propagators.textMapPropagator.toString,
      "TextMapPropagator.Noop"
    )
    assertEquals(propagators.toString, "ContextPropagators.Noop")
  }

  test("of (single input) - use this input") {
    val fields = List("a", "b", "c")
    val propagator = new TestPropagator[String](fields, "TestPropagator")
    val propagators = ContextPropagators.of(propagator)

    assertEquals(propagators.textMapPropagator.fields, fields)
    assertEquals(
      propagators.toString,
      "ContextPropagators.Default{textMapPropagator=TestPropagator}"
    )
  }

  test("of (multiple inputs) - create a multi text map propagator instance") {
    val fieldsA = List("a", "b")
    val fieldsB = List("c", "d")

    val propagatorA = new TestPropagator[String](fieldsA, "TestPropagatorA")
    val propagatorB = new TestPropagator[String](fieldsB, "TestPropagatorB")

    val propagators = ContextPropagators.of(propagatorA, propagatorB)

    assertEquals(propagators.textMapPropagator.fields, fieldsA ++ fieldsB)
    assertEquals(
      propagators.toString,
      "ContextPropagators.Default{textMapPropagator=TextMapPropagator.Multi(TestPropagatorA, TestPropagatorB)}"
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
