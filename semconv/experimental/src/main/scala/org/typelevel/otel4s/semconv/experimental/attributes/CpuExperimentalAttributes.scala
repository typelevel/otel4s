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

  /** The mode of the CPU
    */
  val CpuMode: AttributeKey[String] =
    AttributeKey("cpu.mode")

  /** Values for [[CpuMode]].
    */
  abstract class CpuModeValue(val value: String)
  object CpuModeValue {

    /** user.
      */
    case object User extends CpuModeValue("user")

    /** system.
      */
    case object System extends CpuModeValue("system")

    /** nice.
      */
    case object Nice extends CpuModeValue("nice")

    /** idle.
      */
    case object Idle extends CpuModeValue("idle")

    /** iowait.
      */
    case object Iowait extends CpuModeValue("iowait")

    /** interrupt.
      */
    case object Interrupt extends CpuModeValue("interrupt")

    /** steal.
      */
    case object Steal extends CpuModeValue("steal")

    /** kernel.
      */
    case object Kernel extends CpuModeValue("kernel")
  }

}
