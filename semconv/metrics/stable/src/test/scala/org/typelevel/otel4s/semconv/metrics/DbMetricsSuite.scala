/*
 * Copyright 2024 Typelevel
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
package semconv
package metrics

import munit._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics-tests/SemanticMetricsSuite.scala.j2
class DbMetricsSuite extends FunSuite {

  test("all specs must be stable") {
    DbMetrics.specs.foreach { spec =>
      assert(spec.stability == Stability.stable, s"${spec.name} is not stable")
    }
  }

  test("all attributes must be stable") {
    DbMetrics.specs.foreach { spec =>
      spec.attributeSpecs.foreach { attributeSpec =>
        assert(attributeSpec.stability == Stability.stable, s"${spec.name} / ${attributeSpec.key} is not stable")
      }
    }
  }

}
