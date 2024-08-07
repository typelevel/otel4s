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

package org.typelevel.otel4s.sdk.trace.data

import munit.ScalaCheckSuite
import org.scalacheck.Prop
import org.typelevel.otel4s.trace.scalacheck.Gens

class LimitedDataSuite extends ScalaCheckSuite {

  test("drop extra elements when appending one by one") {
    Prop.forAll(
      Gens.attributes(100),
      Gens.attributes(200)
    ) { (attributes, extraAttributes) =>
      val data = LimitedData
        .attributes(150)
        .appendAll(attributes)
      val dataWithDropped = extraAttributes.foldLeft(data) {
        (data, attribute) =>
          data.append(attribute)
      }

      assertEquals(dataWithDropped.dropped, 150)
    }
  }

  test("drop extra elements when appending all at once") {
    Prop.forAll(
      Gens.attributes(100),
      Gens.attributes(200)
    ) { (attributes, extraAttributes) =>
      val data = LimitedData
        .attributes(150)
        .appendAll(attributes)
      val dataWithDropped = data.appendAll(extraAttributes)

      assertEquals(dataWithDropped.dropped, 150)
    }
  }

}
