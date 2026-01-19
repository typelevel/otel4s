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
object CpuExperimentalAttributes {

  /** The logical CPU number [0..n-1]
    */
  val CpuLogicalNumber: AttributeKey[Long] =
    AttributeKey("cpu.logical_number")

  /** The mode of the CPU
    */
  val CpuMode: AttributeKey[String] =
    AttributeKey("cpu.mode")

  /** Values for [[CpuMode]].
    */
  abstract class CpuModeValue(val value: String)
  object CpuModeValue {
    implicit val attributeFromCpuModeValue: Attribute.From[CpuModeValue, String] = _.value

    /** User
      */
    case object User extends CpuModeValue("user")

    /** System
      */
    case object System extends CpuModeValue("system")

    /** Nice
      */
    case object Nice extends CpuModeValue("nice")

    /** Idle
      */
    case object Idle extends CpuModeValue("idle")

    /** IO Wait
      */
    case object Iowait extends CpuModeValue("iowait")

    /** Interrupt
      */
    case object Interrupt extends CpuModeValue("interrupt")

    /** Steal
      */
    case object Steal extends CpuModeValue("steal")

    /** Kernel
      */
    case object Kernel extends CpuModeValue("kernel")
  }

}
