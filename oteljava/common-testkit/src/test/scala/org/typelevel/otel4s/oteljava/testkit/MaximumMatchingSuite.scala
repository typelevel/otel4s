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

package org.typelevel.otel4s.oteljava.testkit

import munit.FunSuite

class MaximumMatchingSuite extends FunSuite {

  test("find returns a complete matching when one exists") {
    val result = MaximumMatching.find(
      Vector(
        List(0),
        List(0, 1),
        List(1, 2)
      )
    )

    assert(result.isComplete)
    assertEquals(result.size, 3)
    assertEquals(result.matchedExpectationIndices, Set(0, 1, 2))
    assertEquals(result.matchedCandidateIndices, Set(0, 1, 2))
  }

  test("find returns an incomplete matching when candidates collide") {
    val result = MaximumMatching.find(
      Vector(
        List(0),
        List(0)
      )
    )

    assert(!result.isComplete)
    assertEquals(result.size, 1)
    assertEquals(result.matchedExpectationIndices.size, 1)
    assertEquals(result.matchedCandidateIndices, Set(0))
  }

  test("find prefers constrained expectations first") {
    val result = MaximumMatching.find(
      Vector(
        List(0, 1),
        List(1),
        List(0, 2)
      )
    )

    assert(result.isComplete)
    assertEquals(result.size, 3)
  }
}
