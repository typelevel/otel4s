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

import munit.ScalaCheckSuite
import org.scalacheck.Prop.forAll
import org.typelevel.otel4s.context.vault.VaultContext

class PassThroughPropagatorProps extends ScalaCheckSuite {
  private[this] def vaultPropagator(
      fields: Iterable[String]
  ): TextMapPropagator[VaultContext] =
    PassThroughPropagator(fields)

  property("retrieves all stored values matching fields and no others") {
    forAll {
      (
          entries: Map[String, String],
          extraFields: Set[String],
          unpropagated: Map[String, String]
      ) =>
        val propagator = vaultPropagator(entries.keys.view ++ extraFields)
        val toSkip = unpropagated.view
          .filterKeys(s => !entries.contains(s) && !extraFields.contains(s))
          .toMap
        val extracted = propagator.extract(VaultContext.root, entries ++ toSkip)
        val injected = propagator.inject(extracted, Map.empty[String, String])
        assertEquals(injected, entries)
        for (key -> _ <- toSkip) assert(!injected.contains(key)) // redundant
    }
  }

  property("retrieves no values when none were stored") {
    forAll { (entries: Map[String, String], extraFields: Seq[String]) =>
      val propagator = vaultPropagator(entries.keys.view ++ extraFields)
      val injected =
        propagator.inject(VaultContext.root, Map.empty[String, String])
      assert(injected.isEmpty)
    }
  }

  property("retrieves no values when created with no fields") {
    forAll { (entries: Map[String, String]) =>
      val propagator = vaultPropagator(Nil)
      val extracted = propagator.extract(VaultContext.root, entries)
      val injected = propagator.inject(extracted, Map.empty[String, String])
      assertEquals(extracted, VaultContext.root)
      assert(injected.isEmpty)
    }
  }

  property("does not inject fields after extracting none") {
    forAll { (entries: Map[String, String]) =>
      val propagator = vaultPropagator(entries.keySet)
      val extracted1 = propagator.extract(VaultContext.root, entries)
      val extracted2 = propagator.extract(extracted1, Map.empty[String, String])
      val injected = propagator.inject(extracted2, Map.empty[String, String])
      assert(injected.isEmpty)
    }
  }
}
