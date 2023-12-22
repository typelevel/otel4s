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

import io.opentelemetry.api.common.{Attributes => JAttributes}

private[oteljava] object Conversions {

  def toJAttributes(attributes: Seq[Attribute[_]]): JAttributes = {
    val builder = JAttributes.builder

    attributes.foreach { case Attribute(key, value) =>
      key.`type` match {
        case AttributeType.String =>
          builder.put(key.name, value.asInstanceOf[String])
        case AttributeType.Boolean =>
          builder.put(key.name, value.asInstanceOf[Boolean])
        case AttributeType.Long =>
          builder.put(key.name, value.asInstanceOf[Long])
        case AttributeType.Double =>
          builder.put(key.name, value.asInstanceOf[Double])
        case AttributeType.StringList =>
          builder.put(key.name, value.asInstanceOf[List[String]]: _*)
        case AttributeType.BooleanList =>
          builder.put(key.name, value.asInstanceOf[List[Boolean]]: _*)
        case AttributeType.LongList =>
          builder.put(key.name, value.asInstanceOf[List[Long]]: _*)
        case AttributeType.DoubleList =>
          builder.put(key.name, value.asInstanceOf[List[Double]]: _*)
      }
    }

    builder.build()
  }

}
