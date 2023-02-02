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
package java

import io.opentelemetry.api.common.{Attributes => JAttributes}
import munit.FunSuite

class ConversionsSuite extends FunSuite {

  test("proper conversion to OpenTelemetry Attributes") {
    val attributes = List(
      Attribute("string", "string"),
      Attribute("boolean", false),
      Attribute("long", 0L),
      Attribute("double", 0.0),
      Attribute("string", List("string 1", "string 2")),
      Attribute("boolean", List(false, true)),
      Attribute("long", List(Long.MinValue, Long.MaxValue)),
      Attribute("double", List(Double.MinValue, Double.MaxValue))
    )

    val expected = JAttributes
      .builder()
      .put("string", "string")
      .put("boolean", false)
      .put("long", 0L)
      .put("double", 0.0)
      .put("string", "string 1", "string 2")
      .put("boolean", false, true)
      .put("long", Long.MinValue, Long.MaxValue)
      .put("double", Double.MinValue, Double.MaxValue)
      .build()

    assertEquals(Conversions.toJAttributes(attributes), expected)
  }

}
