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
package oteljava

import io.opentelemetry.api.common.{AttributeKey => JAttributeKey}
import io.opentelemetry.api.common.{Attributes => JAttributes}
import munit.FunSuite
import org.typelevel.otel4s.oteljava.AttributeConverters._

class AttributeConvertersSuite extends FunSuite {

  test("proper conversion to and from OpenTelemetry Attributes") {
    val attributesList = List(
      Attribute("string", "string"),
      Attribute("boolean", false),
      Attribute("long", 0L),
      Attribute("double", 0.0),
      Attribute("strings", Seq("string 1", "string 2")),
      Attribute("booleans", Seq(false, true)),
      Attribute("longs", Seq(Long.MinValue, Long.MaxValue)),
      Attribute("doubles", Seq(Double.MinValue, Double.MaxValue)),
      Attribute("duplicate-key", "1"),
      Attribute("duplicate-key", 2L),
    )
    val attributes = attributesList.to(Attributes)

    val jAttributes = JAttributes
      .builder()
      .put("string", "string")
      .put("boolean", false)
      .put("long", 0L)
      .put("double", 0.0)
      .put("strings", "string 1", "string 2")
      .put("booleans", false, true)
      .put("longs", Long.MinValue, Long.MaxValue)
      .put("doubles", Double.MinValue, Double.MaxValue)
      .put("duplicate-key", "1")
      .put("duplicate-key", 2L)
      .build()

    assertEquals(attributesList.toJavaAttributes, jAttributes)
    assertEquals(attributes.toJava, jAttributes)
    assertEquals(jAttributes.toScala, attributes)

    assertEquals(attributes.toJava.toScala, attributes)
    assertEquals(jAttributes.toScala.toJava, jAttributes)
  }

  test("proper conversion to and from OpenTelemetry AttributeKey") {
    val sKey = AttributeKey.stringSeq("key")
    val jKey = JAttributeKey.stringArrayKey("key")

    // can't use `assertEquals` due to existential type
    assert(sKey.toJava == jKey)
    assert(jKey.toScala == sKey)

    assert(sKey.toJava.toScala == sKey)
    assert(jKey.toScala.toJava == jKey)
  }
}
