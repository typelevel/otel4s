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

package org.typelevel.otel4s
package semconv
package experimental.attributes

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/attributes/SemanticAttributes.scala.j2
object DiskExperimentalAttributes {

  /** The disk IO operation direction.
    */
  val DiskIoDirection: AttributeKey[String] =
    AttributeKey("disk.io.direction")

  /** Values for [[DiskIoDirection]].
    */
  abstract class DiskIoDirectionValue(val value: String)
  object DiskIoDirectionValue {
    implicit val attributeFromDiskIoDirectionValue: Attribute.From[DiskIoDirectionValue, String] = _.value

    /** read.
      */
    case object Read extends DiskIoDirectionValue("read")

    /** write.
      */
    case object Write extends DiskIoDirectionValue("write")
  }

}
