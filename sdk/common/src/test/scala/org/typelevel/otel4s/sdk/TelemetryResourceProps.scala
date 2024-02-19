/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk

import cats.Show
import cats.syntax.show._
import munit.ScalaCheckSuite
import org.scalacheck.Prop.forAll
import org.typelevel.otel4s.sdk.scalacheck.Gens

class TelemetryResourceProps extends ScalaCheckSuite {

  property("TelemetryResource#merge merges attributes") {
    forAll(Gens.telemetryResource, Gens.telemetryResource) { (r1, r2) =>
      val mergedEither = r1.merge(r2)
      mergedEither match {
        case Right(merged) =>
          val mergedAttrs = merged.attributes
          val keys =
            r1.attributes.asMap.keySet ++ r2.attributes.asMap.keySet

          mergedAttrs.size == keys.size && mergedAttrs.forall { a =>
            r2.attributes
              .get(a.key)
              .orElse(r1.attributes.get(a.key))
              .contains(a)
          }
        case Left(_) => true
      }

    }
  }

  property("Show[TelemetryResource]") {
    forAll(Gens.telemetryResource) { resource =>
      val expected =
        show"TelemetryResource{attributes=${resource.attributes}, schemaUrl=${resource.schemaUrl}}"

      assertEquals(Show[TelemetryResource].show(resource), expected)
    }
  }

}
