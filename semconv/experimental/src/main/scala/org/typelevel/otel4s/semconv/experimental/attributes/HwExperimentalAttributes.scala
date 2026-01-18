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

  /** Design capacity in Watts-hours or Amper-hours
    */
  val HwBatteryCapacity: AttributeKey[String] =
    AttributeKey("hw.battery.capacity")

  /** Battery <a href="https://schemas.dmtf.org/wbem/cim-html/2.31.0/CIM_Battery.html">chemistry</a>, e.g. Lithium-Ion,
    * Nickel-Cadmium, etc.
    */
  val HwBatteryChemistry: AttributeKey[String] =
    AttributeKey("hw.battery.chemistry")

  /** The current state of the battery
    */
  val HwBatteryState: AttributeKey[String] =
    AttributeKey("hw.battery.state")

  /** BIOS version of the hardware component
    */
  val HwBiosVersion: AttributeKey[String] =
    AttributeKey("hw.bios_version")

  /** Driver version for the hardware component
    */
  val HwDriverVersion: AttributeKey[String] =
    AttributeKey("hw.driver_version")

  /** Type of the enclosure (useful for modular systems)
    */
  val HwEnclosureType: AttributeKey[String] =
    AttributeKey("hw.enclosure.type")

  /** Firmware version of the hardware component
    */
  val HwFirmwareVersion: AttributeKey[String] =
    AttributeKey("hw.firmware_version")

  /** Type of task the GPU is performing
    */
  val HwGpuTask: AttributeKey[String] =
    AttributeKey("hw.gpu.task")

  /** An identifier for the hardware component, unique within the monitored host
    */
  val HwId: AttributeKey[String] =
    AttributeKey("hw.id")

  /** Type of limit for hardware components
    */
  val HwLimitType: AttributeKey[String] =
    AttributeKey("hw.limit_type")

  /** RAID Level of the logical disk
    */
  val HwLogicalDiskRaidLevel: AttributeKey[String] =
    AttributeKey("hw.logical_disk.raid_level")

  /** State of the logical disk space usage
    */
  val HwLogicalDiskState: AttributeKey[String] =
    AttributeKey("hw.logical_disk.state")

  /** Type of the memory module
    */
  val HwMemoryType: AttributeKey[String] =
    AttributeKey("hw.memory.type")

  /** Descriptive model name of the hardware component
    */
  val HwModel: AttributeKey[String] =
    AttributeKey("hw.model")

  /** An easily-recognizable name for the hardware component
    */
  val HwName: AttributeKey[String] =
    AttributeKey("hw.name")

  /** Logical addresses of the adapter (e.g. IP address, or WWPN)
    */
  val HwNetworkLogicalAddresses: AttributeKey[Seq[String]] =
    AttributeKey("hw.network.logical_addresses")

  /** Physical address of the adapter (e.g. MAC address, or WWNN)
    */
  val HwNetworkPhysicalAddress: AttributeKey[String] =
    AttributeKey("hw.network.physical_address")

  /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk controller)
    */
  val HwParent: AttributeKey[String] =
    AttributeKey("hw.parent")

  /** <a href="https://wikipedia.org/wiki/S.M.A.R.T.">S.M.A.R.T.</a> (Self-Monitoring, Analysis, and Reporting
    * Technology) attribute of the physical disk
    */
  val HwPhysicalDiskSmartAttribute: AttributeKey[String] =
    AttributeKey("hw.physical_disk.smart_attribute")

  /** State of the physical disk endurance utilization
    */
  val HwPhysicalDiskState: AttributeKey[String] =
    AttributeKey("hw.physical_disk.state")

  /** Type of the physical disk
    */
  val HwPhysicalDiskType: AttributeKey[String] =
    AttributeKey("hw.physical_disk.type")

  /** Location of the sensor
    */
  val HwSensorLocation: AttributeKey[String] =
    AttributeKey("hw.sensor_location")

  /** Serial number of the hardware component
    */
  val HwSerialNumber: AttributeKey[String] =
    AttributeKey("hw.serial_number")

  /** The current state of the component
    */
  val HwState: AttributeKey[String] =
    AttributeKey("hw.state")

  /** Type of tape drive operation
    */
  val HwTapeDriveOperationType: AttributeKey[String] =
    AttributeKey("hw.tape_drive.operation_type")

  /** Type of the component
    *
    * @note
    *   <p> Describes the category of the hardware component for which `hw.state` is being reported. For example,
    *   `hw.type=temperature` along with `hw.state=degraded` would indicate that the temperature of the hardware
    *   component has been reported as `degraded`.
    */
  val HwType: AttributeKey[String] =
    AttributeKey("hw.type")

  /** Vendor name of the hardware component
    */
  val HwVendor: AttributeKey[String] =
    AttributeKey("hw.vendor")

  /** Values for [[HwBatteryState]].
    */
  abstract class HwBatteryStateValue(val value: String)
  object HwBatteryStateValue {
    implicit val attributeFromHwBatteryStateValue: Attribute.From[HwBatteryStateValue, String] = _.value

    /** Charging
      */
    case object Charging extends HwBatteryStateValue("charging")

    /** Discharging
      */
    case object Discharging extends HwBatteryStateValue("discharging")
  }

  /** Values for [[HwGpuTask]].
    */
  abstract class HwGpuTaskValue(val value: String)
  object HwGpuTaskValue {
    implicit val attributeFromHwGpuTaskValue: Attribute.From[HwGpuTaskValue, String] = _.value

    /** Decoder
      */
    case object Decoder extends HwGpuTaskValue("decoder")

    /** Encoder
      */
    case object Encoder extends HwGpuTaskValue("encoder")

    /** General
      */
    case object General extends HwGpuTaskValue("general")
  }

  /** Values for [[HwLimitType]].
    */
  abstract class HwLimitTypeValue(val value: String)
  object HwLimitTypeValue {
    implicit val attributeFromHwLimitTypeValue: Attribute.From[HwLimitTypeValue, String] = _.value

    /** Critical
      */
    case object Critical extends HwLimitTypeValue("critical")

    /** Degraded
      */
    case object Degraded extends HwLimitTypeValue("degraded")

    /** High Critical
      */
    case object HighCritical extends HwLimitTypeValue("high.critical")

    /** High Degraded
      */
    case object HighDegraded extends HwLimitTypeValue("high.degraded")

    /** Low Critical
      */
    case object LowCritical extends HwLimitTypeValue("low.critical")

    /** Low Degraded
      */
    case object LowDegraded extends HwLimitTypeValue("low.degraded")

    /** Maximum
      */
    case object Max extends HwLimitTypeValue("max")

    /** Throttled
      */
    case object Throttled extends HwLimitTypeValue("throttled")

    /** Turbo
      */
    case object Turbo extends HwLimitTypeValue("turbo")
  }

  /** Values for [[HwLogicalDiskState]].
    */
  abstract class HwLogicalDiskStateValue(val value: String)
  object HwLogicalDiskStateValue {
    implicit val attributeFromHwLogicalDiskStateValue: Attribute.From[HwLogicalDiskStateValue, String] = _.value

    /** Used
      */
    case object Used extends HwLogicalDiskStateValue("used")

    /** Free
      */
    case object Free extends HwLogicalDiskStateValue("free")
  }

  /** Values for [[HwPhysicalDiskState]].
    */
  abstract class HwPhysicalDiskStateValue(val value: String)
  object HwPhysicalDiskStateValue {
    implicit val attributeFromHwPhysicalDiskStateValue: Attribute.From[HwPhysicalDiskStateValue, String] = _.value

    /** Remaining
      */
    case object Remaining extends HwPhysicalDiskStateValue("remaining")
  }

  /** Values for [[HwState]].
    */
  abstract class HwStateValue(val value: String)
  object HwStateValue {
    implicit val attributeFromHwStateValue: Attribute.From[HwStateValue, String] = _.value

    /** Degraded
      */
    case object Degraded extends HwStateValue("degraded")

    /** Failed
      */
    case object Failed extends HwStateValue("failed")

    /** Needs Cleaning
      */
    case object NeedsCleaning extends HwStateValue("needs_cleaning")

    /** OK
      */
    case object Ok extends HwStateValue("ok")

    /** Predicted Failure
      */
    case object PredictedFailure extends HwStateValue("predicted_failure")
  }

  /** Values for [[HwTapeDriveOperationType]].
    */
  abstract class HwTapeDriveOperationTypeValue(val value: String)
  object HwTapeDriveOperationTypeValue {
    implicit val attributeFromHwTapeDriveOperationTypeValue: Attribute.From[HwTapeDriveOperationTypeValue, String] =
      _.value

    /** Mount
      */
    case object Mount extends HwTapeDriveOperationTypeValue("mount")

    /** Unmount
      */
    case object Unmount extends HwTapeDriveOperationTypeValue("unmount")

    /** Clean
      */
    case object Clean extends HwTapeDriveOperationTypeValue("clean")
  }

  /** Values for [[HwType]].
    */
  abstract class HwTypeValue(val value: String)
  object HwTypeValue {
    implicit val attributeFromHwTypeValue: Attribute.From[HwTypeValue, String] = _.value

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
