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

import io.opentelemetry.api.common.{AttributeKey => JAttributeKey}
import io.opentelemetry.api.common.{AttributeType => JAttributeType}
import io.opentelemetry.api.common.{Attributes => JAttributes}

import scala.jdk.CollectionConverters._

private[java] object Conversions {

  def toJAttributes(attributes: Seq[Attribute[_]]): JAttributes = {
    val builder = JAttributes.builder

    def put(name: String, jType: JAttributeType, value: Any): Unit = {
      val jKey = new JAttributeKey[Any] {
        def getKey: String = name
        def getType: JAttributeType = jType
        override def toString: String = name
      }
      builder.put[Any](jKey, value)
      ()
    }

    def putList(name: String, jType: JAttributeType, values: Any): Unit = {
      val jKey = new JAttributeKey[Any] {
        def getKey: String = name
        def getType: JAttributeType = jType
        override def toString: String = name
      }
      builder.put[Any](jKey, values.asInstanceOf[Seq[Any]].asJava)
      ()
    }

    attributes.foreach { case Attribute(key, value) =>
      key.`type` match {
        case AttributeType.String =>
          put(key.name, JAttributeType.STRING, value)
        case AttributeType.Boolean =>
          put(key.name, JAttributeType.BOOLEAN, value)
        case AttributeType.Long =>
          put(key.name, JAttributeType.LONG, value)
        case AttributeType.Double =>
          put(key.name, JAttributeType.DOUBLE, value)
        case AttributeType.StringList =>
          putList(key.name, JAttributeType.STRING_ARRAY, value)
        case AttributeType.BooleanList =>
          putList(key.name, JAttributeType.BOOLEAN_ARRAY, value)
        case AttributeType.LongList =>
          putList(key.name, JAttributeType.LONG_ARRAY, value)
        case AttributeType.DoubleList =>
          putList(key.name, JAttributeType.DOUBLE_ARRAY, value)
      }
    }
    builder.build()
  }

}
