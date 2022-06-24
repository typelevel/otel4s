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

import cats.effect.IO
import munit.CatsEffectSuite

class CounterSuite extends CatsEffectSuite {

  test("do not allocate attributes when instrument is noop") {
    val counter = Counter.noop[IO, Long]

    var allocated = false

    def allocateAttribute = {
      allocated = true
      List(Attribute(AttributeKey.string("key"), "value"))
    }

    for {
      _ <- counter.add(1L, allocateAttribute: _*)
      _ <- counter.inc(allocateAttribute: _*)
    } yield assert(!allocated)
  }

}
