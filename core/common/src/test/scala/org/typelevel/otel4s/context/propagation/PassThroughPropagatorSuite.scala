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

import munit.FunSuite
import org.typelevel.otel4s.context.vault.VaultContext

class PassThroughPropagatorSuite extends FunSuite {
  private val propagator: TextMapPropagator[VaultContext] =
    PassThroughPropagator("foo", "bar")

  test("propagates only fields given to constructor") {
    val entries = Map("foo" -> "0", "bar" -> "1", "baz" -> "2", "qux" -> "3")
    val extracted = propagator.extract(VaultContext.root, entries)
    val injected = propagator.inject(extracted, Map.empty[String, String])
    assertEquals(injected, Map("foo" -> "0", "bar" -> "1"))
  }

  test("propagates nothing when no matching fields") {
    val entries = Map("baz" -> "2", "qux" -> "3")
    val extracted = propagator.extract(VaultContext.root, entries)
    val injected = propagator.inject(extracted, Map.empty[String, String])
    assert(injected.isEmpty)
  }

  test("does not inject fields after extracting none") {
    val extracted1 = propagator.extract(VaultContext.root, Map("foo" -> "0"))
    val extracted2 = propagator.extract(extracted1, Map.empty[String, String])
    val injected = propagator.inject(extracted2, Map.empty[String, String])
    assert(injected.isEmpty)
  }
}
