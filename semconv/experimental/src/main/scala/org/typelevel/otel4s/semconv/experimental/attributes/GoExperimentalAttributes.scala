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
object GoExperimentalAttributes {

  /** The type of memory.
    */
  val GoMemoryType: AttributeKey[String] =
    AttributeKey("go.memory.type")

  /** Values for [[GoMemoryType]].
    */
  abstract class GoMemoryTypeValue(val value: String)
  object GoMemoryTypeValue {

    /** Memory allocated from the heap that is reserved for stack space, whether or not it is currently in-use.
      */
    case object Stack extends GoMemoryTypeValue("stack")

    /** Memory used by the Go runtime, excluding other categories of memory usage described in this enumeration.
      */
    case object Other extends GoMemoryTypeValue("other")
  }

}
