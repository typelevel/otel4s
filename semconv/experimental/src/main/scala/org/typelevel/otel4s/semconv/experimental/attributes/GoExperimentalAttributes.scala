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

  /** The detailed state of the CPU.
    *
    * @note
    *   <p> Value SHOULD match the specific CPU class reported by the Go runtime under `/cpu/classes/...`. The list of
    *   possible values is subject to change with the Go version used.
    */
  val GoCpuDetailedState: AttributeKey[String] =
    AttributeKey("go.cpu.detailed_state")

  /** The state of the CPU.
    */
  val GoCpuState: AttributeKey[String] =
    AttributeKey("go.cpu.state")

  /** The detailed type of memory.
    *
    * @note
    *   <p> Value SHOULD match the specific memory class reported by the Go runtime under `/memory/classes/...`. The
    *   list of possible values is subject to change with the Go version used.
    */
  val GoMemoryDetailedType: AttributeKey[String] =
    AttributeKey("go.memory.detailed_type")

  /** The type of memory.
    */
  val GoMemoryType: AttributeKey[String] =
    AttributeKey("go.memory.type")

  /** Values for [[GoCpuState]].
    */
  abstract class GoCpuStateValue(val value: String)
  object GoCpuStateValue {
    implicit val attributeFromGoCpuStateValue: Attribute.From[GoCpuStateValue, String] = _.value

    /** CPU time spent running user Go code.
      */
    case object User extends GoCpuStateValue("user")

    /** CPU time spent performing garbage collection tasks.
      */
    case object Gc extends GoCpuStateValue("gc")

    /** CPU time spent returning unused memory to the underlying platform.
      */
    case object Scavenge extends GoCpuStateValue("scavenge")

    /** Available CPU time not spent executing any Go or Go runtime code.
      */
    case object Idle extends GoCpuStateValue("idle")
  }

  /** Values for [[GoMemoryType]].
    */
  abstract class GoMemoryTypeValue(val value: String)
  object GoMemoryTypeValue {
    implicit val attributeFromGoMemoryTypeValue: Attribute.From[GoMemoryTypeValue, String] = _.value

    /** Memory allocated from the heap that is reserved for stack space, whether or not it is currently in-use.
      */
    case object Stack extends GoMemoryTypeValue("stack")

    /** Memory used by the Go runtime, excluding other categories of memory usage described in this enumeration.
      */
    case object Other extends GoMemoryTypeValue("other")
  }

}
