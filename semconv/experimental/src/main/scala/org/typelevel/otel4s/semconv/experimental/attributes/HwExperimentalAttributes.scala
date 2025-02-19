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
object HwExperimentalAttributes {

  /** An identifier for the hardware component, unique within the monitored host
    */
  val HwId: AttributeKey[String] =
    AttributeKey("hw.id")

  /** An easily-recognizable name for the hardware component
    */
  val HwName: AttributeKey[String] =
    AttributeKey("hw.name")

  /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk controller)
    */
  val HwParent: AttributeKey[String] =
    AttributeKey("hw.parent")

  /** The current state of the component
    */
  val HwState: AttributeKey[String] =
    AttributeKey("hw.state")

  /** Type of the component
    *
    * @note
    *   <p> Describes the category of the hardware component for which `hw.state` is being reported. For example,
    *   `hw.type=temperature` along with `hw.state=degraded` would indicate that the temperature of the hardware
    *   component has been reported as `degraded`.
    */
  val HwType: AttributeKey[String] =
    AttributeKey("hw.type")

  /** Values for [[HwState]].
    */
  abstract class HwStateValue(val value: String)
  object HwStateValue {

    /** Ok
      */
    case object Ok extends HwStateValue("ok")

    /** Degraded
      */
    case object Degraded extends HwStateValue("degraded")

    /** Failed
      */
    case object Failed extends HwStateValue("failed")
  }

  /** Values for [[HwType]].
    */
  abstract class HwTypeValue(val value: String)
  object HwTypeValue {

    /** Battery
      */
    case object Battery extends HwTypeValue("battery")

    /** CPU
      */
    case object Cpu extends HwTypeValue("cpu")

    /** Disk controller
      */
    case object DiskController extends HwTypeValue("disk_controller")

    /** Enclosure
      */
    case object Enclosure extends HwTypeValue("enclosure")

    /** Fan
      */
    case object Fan extends HwTypeValue("fan")

    /** GPU
      */
    case object Gpu extends HwTypeValue("gpu")

    /** Logical disk
      */
    case object LogicalDisk extends HwTypeValue("logical_disk")

    /** Memory
      */
    case object Memory extends HwTypeValue("memory")

    /** Network
      */
    case object Network extends HwTypeValue("network")

    /** Physical disk
      */
    case object PhysicalDisk extends HwTypeValue("physical_disk")

    /** Power supply
      */
    case object PowerSupply extends HwTypeValue("power_supply")

    /** Tape drive
      */
    case object TapeDrive extends HwTypeValue("tape_drive")

    /** Temperature
      */
    case object Temperature extends HwTypeValue("temperature")

    /** Voltage
      */
    case object Voltage extends HwTypeValue("voltage")
  }

}
