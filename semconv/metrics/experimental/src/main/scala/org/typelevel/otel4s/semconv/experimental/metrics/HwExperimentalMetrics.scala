/*
 * Copyright 2024 Typelevel
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
package experimental
package metrics

import cats.effect.Resource
import org.typelevel.otel4s.metrics._
import org.typelevel.otel4s.semconv.attributes._
import org.typelevel.otel4s.semconv.experimental.attributes._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object HwExperimentalMetrics {

  val specs: List[MetricSpec] = List(
    BatteryCharge,
    BatteryChargeLimit,
    BatteryTimeLeft,
    CpuSpeed,
    CpuSpeedLimit,
    Energy,
    Errors,
    FanSpeed,
    FanSpeedLimit,
    FanSpeedRatio,
    GpuIo,
    GpuMemoryLimit,
    GpuMemoryUsage,
    GpuMemoryUtilization,
    GpuUtilization,
    HostAmbientTemperature,
    HostEnergy,
    HostHeatingMargin,
    HostPower,
    LogicalDiskLimit,
    LogicalDiskUsage,
    LogicalDiskUtilization,
    MemorySize,
    NetworkBandwidthLimit,
    NetworkBandwidthUtilization,
    NetworkIo,
    NetworkPackets,
    NetworkUp,
    PhysicalDiskEnduranceUtilization,
    PhysicalDiskSize,
    PhysicalDiskSmart,
    Power,
    PowerSupplyLimit,
    PowerSupplyUsage,
    PowerSupplyUtilization,
    Status,
    TapeDriveOperations,
    Temperature,
    TemperatureLimit,
    Voltage,
    VoltageLimit,
    VoltageNominal,
  )

  /** Remaining fraction of battery charge.
    */
  object BatteryCharge extends MetricSpec.Unsealed {

    val name: String = "hw.battery.charge"
    val description: String = "Remaining fraction of battery charge."
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Design capacity in Watts-hours or Amper-hours
        */
      val hwBatteryCapacity: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwBatteryCapacity,
          List(
            "9.3Ah",
            "50Wh",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Battery <a href="https://schemas.dmtf.org/wbem/cim-html/2.31.0/CIM_Battery.html">chemistry</a>, e.g.
        * Lithium-Ion, Nickel-Cadmium, etc.
        */
      val hwBatteryChemistry: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwBatteryChemistry,
          List(
            "Li-ion",
            "NiMH",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwBatteryCapacity,
          hwBatteryChemistry,
          hwId,
          hwModel,
          hwName,
          hwParent,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Lower limit of battery charge fraction to ensure proper operation.
    */
  object BatteryChargeLimit extends MetricSpec.Unsealed {

    val name: String = "hw.battery.charge.limit"
    val description: String = "Lower limit of battery charge fraction to ensure proper operation."
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Design capacity in Watts-hours or Amper-hours
        */
      val hwBatteryCapacity: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwBatteryCapacity,
          List(
            "9.3Ah",
            "50Wh",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Battery <a href="https://schemas.dmtf.org/wbem/cim-html/2.31.0/CIM_Battery.html">chemistry</a>, e.g.
        * Lithium-Ion, Nickel-Cadmium, etc.
        */
      val hwBatteryChemistry: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwBatteryChemistry,
          List(
            "Li-ion",
            "NiMH",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Represents battery charge level thresholds relevant to device operation and health. Each `limit_type` denotes
        * a specific charge limit such as the minimum or maximum optimal charge, the shutdown threshold, or
        * energy-saving thresholds. These values are typically provided by the hardware or firmware to guide safe and
        * efficient battery usage.
        */
      val hwLimitType: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwLimitType,
          List(
            "critical",
            "throttled",
            "degraded",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwBatteryCapacity,
          hwBatteryChemistry,
          hwId,
          hwLimitType,
          hwModel,
          hwName,
          hwParent,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Time left before battery is completely charged or discharged.
    */
  object BatteryTimeLeft extends MetricSpec.Unsealed {

    val name: String = "hw.battery.time_left"
    val description: String = "Time left before battery is completely charged or discharged."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Design capacity in Watts-hours or Amper-hours
        */
      val hwBatteryCapacity: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwBatteryCapacity,
          List(
            "9.3Ah",
            "50Wh",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Battery <a href="https://schemas.dmtf.org/wbem/cim-html/2.31.0/CIM_Battery.html">chemistry</a>, e.g.
        * Lithium-Ion, Nickel-Cadmium, etc.
        */
      val hwBatteryChemistry: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwBatteryChemistry,
          List(
            "Li-ion",
            "NiMH",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The current state of the battery
        *
        * @note
        *   <p> The `hw.state` attribute should indicate the current state of the battery. It should be one of the
        *   predefined states such as "charging" or "discharging".
        */
      val hwBatteryState: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwBatteryState,
          List(
          ),
          Requirement.conditionallyRequired("If the battery is charging or discharging"),
          Stability.development
        )

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The current state of the component
        */
      val hwState: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwState,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwBatteryCapacity,
          hwBatteryChemistry,
          hwBatteryState,
          hwId,
          hwModel,
          hwName,
          hwParent,
          hwState,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** CPU current frequency.
    */
  object CpuSpeed extends MetricSpec.Unsealed {

    val name: String = "hw.cpu.speed"
    val description: String = "CPU current frequency."
    val unit: String = "Hz"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwModel,
          hwName,
          hwParent,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** CPU maximum frequency.
    */
  object CpuSpeedLimit extends MetricSpec.Unsealed {

    val name: String = "hw.cpu.speed.limit"
    val description: String = "CPU maximum frequency."
    val unit: String = "Hz"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Type of limit for hardware components
        */
      val hwLimitType: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwLimitType,
          List(
            "throttled",
            "max",
            "turbo",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwLimitType,
          hwModel,
          hwName,
          hwParent,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Energy consumed by the component.
    */
  object Energy extends MetricSpec.Unsealed {

    val name: String = "hw.energy"
    val description: String = "Energy consumed by the component."
    val unit: String = "J"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Type of the component
        *
        * @note
        *   <p> Describes the category of the hardware component for which `hw.state` is being reported. For example,
        *   `hw.type=temperature` along with `hw.state=degraded` would indicate that the temperature of the hardware
        *   component has been reported as `degraded`.
        */
      val hwType: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwType,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwName,
          hwParent,
          hwType,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Number of errors encountered by the component.
    */
  object Errors extends MetricSpec.Unsealed {

    val name: String = "hw.errors"
    val description: String = "Number of errors encountered by the component."
    val unit: String = "{error}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The type of error encountered by the component.
        *
        * @note
        *   <p> The `error.type` SHOULD match the error code reported by the component, the canonical name of the error,
        *   or another low-cardinality error identifier. Instrumentations SHOULD document the list of errors they
        *   report.
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "uncorrected",
            "zero_buffer_credit",
            "crc",
            "bad_sector",
          ),
          Requirement.conditionallyRequired("if and only if an error has occurred"),
          Stability.stable
        )

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Type of the component
        *
        * @note
        *   <p> Describes the category of the hardware component for which `hw.state` is being reported. For example,
        *   `hw.type=temperature` along with `hw.state=degraded` would indicate that the temperature of the hardware
        *   component has been reported as `degraded`.
        */
      val hwType: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwType,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** Direction of network traffic for network errors.
        *
        * @note
        *   <p> This attribute SHOULD only be used when `hw.type` is set to `"network"` to indicate the direction of the
        *   error.
        */
      val networkIoDirection: AttributeSpec[String] =
        AttributeSpec(
          NetworkExperimentalAttributes.NetworkIoDirection,
          List(
            "receive",
            "transmit",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          hwId,
          hwName,
          hwParent,
          hwType,
          networkIoDirection,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Fan speed in revolutions per minute.
    */
  object FanSpeed extends MetricSpec.Unsealed {

    val name: String = "hw.fan.speed"
    val description: String = "Fan speed in revolutions per minute."
    val unit: String = "rpm"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Location of the sensor
        */
      val hwSensorLocation: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSensorLocation,
          List(
            "cpu0",
            "ps1",
            "INLET",
            "CPU0_DIE",
            "AMBIENT",
            "MOTHERBOARD",
            "PS0 V3_3",
            "MAIN_12V",
            "CPU_VCORE",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwName,
          hwParent,
          hwSensorLocation,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Speed limit in rpm.
    */
  object FanSpeedLimit extends MetricSpec.Unsealed {

    val name: String = "hw.fan.speed.limit"
    val description: String = "Speed limit in rpm."
    val unit: String = "rpm"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Type of limit for hardware components
        */
      val hwLimitType: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwLimitType,
          List(
            "low.critical",
            "low.degraded",
            "max",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Location of the sensor
        */
      val hwSensorLocation: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSensorLocation,
          List(
            "cpu0",
            "ps1",
            "INLET",
            "CPU0_DIE",
            "AMBIENT",
            "MOTHERBOARD",
            "PS0 V3_3",
            "MAIN_12V",
            "CPU_VCORE",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwLimitType,
          hwName,
          hwParent,
          hwSensorLocation,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Fan speed expressed as a fraction of its maximum speed.
    */
  object FanSpeedRatio extends MetricSpec.Unsealed {

    val name: String = "hw.fan.speed_ratio"
    val description: String = "Fan speed expressed as a fraction of its maximum speed."
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Location of the sensor
        */
      val hwSensorLocation: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSensorLocation,
          List(
            "cpu0",
            "ps1",
            "INLET",
            "CPU0_DIE",
            "AMBIENT",
            "MOTHERBOARD",
            "PS0 V3_3",
            "MAIN_12V",
            "CPU_VCORE",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwName,
          hwParent,
          hwSensorLocation,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Received and transmitted bytes by the GPU.
    */
  object GpuIo extends MetricSpec.Unsealed {

    val name: String = "hw.gpu.io"
    val description: String = "Received and transmitted bytes by the GPU."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Driver version for the hardware component
        */
      val hwDriverVersion: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwDriverVersion,
          List(
            "10.2.1-3",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Firmware version of the hardware component
        */
      val hwFirmwareVersion: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwFirmwareVersion,
          List(
            "2.0.1",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Serial number of the hardware component
        */
      val hwSerialNumber: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSerialNumber,
          List(
            "CNFCP0123456789",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The network IO operation direction.
        */
      val networkIoDirection: AttributeSpec[String] =
        AttributeSpec(
          NetworkExperimentalAttributes.NetworkIoDirection,
          List(
            "receive",
            "transmit",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwDriverVersion,
          hwFirmwareVersion,
          hwId,
          hwModel,
          hwName,
          hwParent,
          hwSerialNumber,
          hwVendor,
          networkIoDirection,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Size of the GPU memory.
    */
  object GpuMemoryLimit extends MetricSpec.Unsealed {

    val name: String = "hw.gpu.memory.limit"
    val description: String = "Size of the GPU memory."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Driver version for the hardware component
        */
      val hwDriverVersion: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwDriverVersion,
          List(
            "10.2.1-3",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Firmware version of the hardware component
        */
      val hwFirmwareVersion: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwFirmwareVersion,
          List(
            "2.0.1",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Serial number of the hardware component
        */
      val hwSerialNumber: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSerialNumber,
          List(
            "CNFCP0123456789",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwDriverVersion,
          hwFirmwareVersion,
          hwId,
          hwModel,
          hwName,
          hwParent,
          hwSerialNumber,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** GPU memory used.
    */
  object GpuMemoryUsage extends MetricSpec.Unsealed {

    val name: String = "hw.gpu.memory.usage"
    val description: String = "GPU memory used."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Driver version for the hardware component
        */
      val hwDriverVersion: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwDriverVersion,
          List(
            "10.2.1-3",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Firmware version of the hardware component
        */
      val hwFirmwareVersion: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwFirmwareVersion,
          List(
            "2.0.1",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Serial number of the hardware component
        */
      val hwSerialNumber: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSerialNumber,
          List(
            "CNFCP0123456789",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwDriverVersion,
          hwFirmwareVersion,
          hwId,
          hwModel,
          hwName,
          hwParent,
          hwSerialNumber,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Fraction of GPU memory used.
    */
  object GpuMemoryUtilization extends MetricSpec.Unsealed {

    val name: String = "hw.gpu.memory.utilization"
    val description: String = "Fraction of GPU memory used."
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Driver version for the hardware component
        */
      val hwDriverVersion: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwDriverVersion,
          List(
            "10.2.1-3",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Firmware version of the hardware component
        */
      val hwFirmwareVersion: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwFirmwareVersion,
          List(
            "2.0.1",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Serial number of the hardware component
        */
      val hwSerialNumber: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSerialNumber,
          List(
            "CNFCP0123456789",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwDriverVersion,
          hwFirmwareVersion,
          hwId,
          hwModel,
          hwName,
          hwParent,
          hwSerialNumber,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Fraction of time spent in a specific task.
    */
  object GpuUtilization extends MetricSpec.Unsealed {

    val name: String = "hw.gpu.utilization"
    val description: String = "Fraction of time spent in a specific task."
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Driver version for the hardware component
        */
      val hwDriverVersion: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwDriverVersion,
          List(
            "10.2.1-3",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Firmware version of the hardware component
        */
      val hwFirmwareVersion: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwFirmwareVersion,
          List(
            "2.0.1",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Type of task the GPU is performing
        */
      val hwGpuTask: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwGpuTask,
          List(
            "decoder",
            "encoder",
            "general",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Serial number of the hardware component
        */
      val hwSerialNumber: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSerialNumber,
          List(
            "CNFCP0123456789",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwDriverVersion,
          hwFirmwareVersion,
          hwGpuTask,
          hwId,
          hwModel,
          hwName,
          hwParent,
          hwSerialNumber,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Ambient (external) temperature of the physical host.
    */
  object HostAmbientTemperature extends MetricSpec.Unsealed {

    val name: String = "hw.host.ambient_temperature"
    val description: String = "Ambient (external) temperature of the physical host."
    val unit: String = "Cel"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwName,
          hwParent,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Total energy consumed by the entire physical host, in joules.
    *
    * @note
    *   <p> The overall energy usage of a host MUST be reported using the specific `hw.host.energy` and `hw.host.power`
    *   metrics <strong>only</strong>, instead of the generic `hw.energy` and `hw.power` described in the previous
    *   section, to prevent summing up overlapping values.
    */
  object HostEnergy extends MetricSpec.Unsealed {

    val name: String = "hw.host.energy"
    val description: String = "Total energy consumed by the entire physical host, in joules."
    val unit: String = "J"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwName,
          hwParent,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** By how many degrees Celsius the temperature of the physical host can be increased, before reaching a warning
    * threshold on one of the internal sensors.
    */
  object HostHeatingMargin extends MetricSpec.Unsealed {

    val name: String = "hw.host.heating_margin"
    val description: String =
      "By how many degrees Celsius the temperature of the physical host can be increased, before reaching a warning threshold on one of the internal sensors."
    val unit: String = "Cel"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwName,
          hwParent,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Instantaneous power consumed by the entire physical host in Watts (`hw.host.energy` is preferred).
    *
    * @note
    *   <p> The overall energy usage of a host MUST be reported using the specific `hw.host.energy` and `hw.host.power`
    *   metrics <strong>only</strong>, instead of the generic `hw.energy` and `hw.power` described in the previous
    *   section, to prevent summing up overlapping values.
    */
  object HostPower extends MetricSpec.Unsealed {

    val name: String = "hw.host.power"
    val description: String =
      "Instantaneous power consumed by the entire physical host in Watts (`hw.host.energy` is preferred)."
    val unit: String = "W"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwName,
          hwParent,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Size of the logical disk.
    */
  object LogicalDiskLimit extends MetricSpec.Unsealed {

    val name: String = "hw.logical_disk.limit"
    val description: String = "Size of the logical disk."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** RAID Level of the logical disk
        */
      val hwLogicalDiskRaidLevel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwLogicalDiskRaidLevel,
          List(
            "RAID0+1",
            "RAID5",
            "RAID10",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwLogicalDiskRaidLevel,
          hwName,
          hwParent,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Logical disk space usage.
    */
  object LogicalDiskUsage extends MetricSpec.Unsealed {

    val name: String = "hw.logical_disk.usage"
    val description: String = "Logical disk space usage."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** RAID Level of the logical disk
        */
      val hwLogicalDiskRaidLevel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwLogicalDiskRaidLevel,
          List(
            "RAID0+1",
            "RAID5",
            "RAID10",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** State of the logical disk space usage
        */
      val hwLogicalDiskState: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwLogicalDiskState,
          List(
            "used",
            "free",
          ),
          Requirement.required,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwLogicalDiskRaidLevel,
          hwLogicalDiskState,
          hwName,
          hwParent,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Logical disk space utilization as a fraction.
    */
  object LogicalDiskUtilization extends MetricSpec.Unsealed {

    val name: String = "hw.logical_disk.utilization"
    val description: String = "Logical disk space utilization as a fraction."
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** RAID Level of the logical disk
        */
      val hwLogicalDiskRaidLevel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwLogicalDiskRaidLevel,
          List(
            "RAID0+1",
            "RAID5",
            "RAID10",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** State of the logical disk space usage
        */
      val hwLogicalDiskState: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwLogicalDiskState,
          List(
            "used",
            "free",
          ),
          Requirement.required,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwLogicalDiskRaidLevel,
          hwLogicalDiskState,
          hwName,
          hwParent,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Size of the memory module.
    */
  object MemorySize extends MetricSpec.Unsealed {

    val name: String = "hw.memory.size"
    val description: String = "Size of the memory module."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Type of the memory module
        */
      val hwMemoryType: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwMemoryType,
          List(
            "DDR4",
            "DDR5",
            "LPDDR5",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Serial number of the hardware component
        */
      val hwSerialNumber: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSerialNumber,
          List(
            "CNFCP0123456789",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwMemoryType,
          hwModel,
          hwName,
          hwParent,
          hwSerialNumber,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Link speed.
    */
  object NetworkBandwidthLimit extends MetricSpec.Unsealed {

    val name: String = "hw.network.bandwidth.limit"
    val description: String = "Link speed."
    val unit: String = "By/s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Logical addresses of the adapter (e.g. IP address, or WWPN)
        */
      val hwNetworkLogicalAddresses: AttributeSpec[Seq[String]] =
        AttributeSpec(
          HwExperimentalAttributes.HwNetworkLogicalAddresses,
          List(
            Seq("172.16.8.21", "57.11.193.42"),
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Physical address of the adapter (e.g. MAC address, or WWNN)
        */
      val hwNetworkPhysicalAddress: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwNetworkPhysicalAddress,
          List(
            "00-90-F5-E9-7B-36",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Serial number of the hardware component
        */
      val hwSerialNumber: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSerialNumber,
          List(
            "CNFCP0123456789",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwModel,
          hwName,
          hwNetworkLogicalAddresses,
          hwNetworkPhysicalAddress,
          hwParent,
          hwSerialNumber,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Utilization of the network bandwidth as a fraction.
    */
  object NetworkBandwidthUtilization extends MetricSpec.Unsealed {

    val name: String = "hw.network.bandwidth.utilization"
    val description: String = "Utilization of the network bandwidth as a fraction."
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Logical addresses of the adapter (e.g. IP address, or WWPN)
        */
      val hwNetworkLogicalAddresses: AttributeSpec[Seq[String]] =
        AttributeSpec(
          HwExperimentalAttributes.HwNetworkLogicalAddresses,
          List(
            Seq("172.16.8.21", "57.11.193.42"),
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Physical address of the adapter (e.g. MAC address, or WWNN)
        */
      val hwNetworkPhysicalAddress: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwNetworkPhysicalAddress,
          List(
            "00-90-F5-E9-7B-36",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Serial number of the hardware component
        */
      val hwSerialNumber: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSerialNumber,
          List(
            "CNFCP0123456789",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwModel,
          hwName,
          hwNetworkLogicalAddresses,
          hwNetworkPhysicalAddress,
          hwParent,
          hwSerialNumber,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Received and transmitted network traffic in bytes.
    */
  object NetworkIo extends MetricSpec.Unsealed {

    val name: String = "hw.network.io"
    val description: String = "Received and transmitted network traffic in bytes."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Logical addresses of the adapter (e.g. IP address, or WWPN)
        */
      val hwNetworkLogicalAddresses: AttributeSpec[Seq[String]] =
        AttributeSpec(
          HwExperimentalAttributes.HwNetworkLogicalAddresses,
          List(
            Seq("172.16.8.21", "57.11.193.42"),
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Physical address of the adapter (e.g. MAC address, or WWNN)
        */
      val hwNetworkPhysicalAddress: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwNetworkPhysicalAddress,
          List(
            "00-90-F5-E9-7B-36",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Serial number of the hardware component
        */
      val hwSerialNumber: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSerialNumber,
          List(
            "CNFCP0123456789",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The network IO operation direction.
        */
      val networkIoDirection: AttributeSpec[String] =
        AttributeSpec(
          NetworkExperimentalAttributes.NetworkIoDirection,
          List(
            "receive",
            "transmit",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwModel,
          hwName,
          hwNetworkLogicalAddresses,
          hwNetworkPhysicalAddress,
          hwParent,
          hwSerialNumber,
          hwVendor,
          networkIoDirection,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Received and transmitted network traffic in packets (or frames).
    */
  object NetworkPackets extends MetricSpec.Unsealed {

    val name: String = "hw.network.packets"
    val description: String = "Received and transmitted network traffic in packets (or frames)."
    val unit: String = "{packet}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Logical addresses of the adapter (e.g. IP address, or WWPN)
        */
      val hwNetworkLogicalAddresses: AttributeSpec[Seq[String]] =
        AttributeSpec(
          HwExperimentalAttributes.HwNetworkLogicalAddresses,
          List(
            Seq("172.16.8.21", "57.11.193.42"),
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Physical address of the adapter (e.g. MAC address, or WWNN)
        */
      val hwNetworkPhysicalAddress: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwNetworkPhysicalAddress,
          List(
            "00-90-F5-E9-7B-36",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Serial number of the hardware component
        */
      val hwSerialNumber: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSerialNumber,
          List(
            "CNFCP0123456789",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The network IO operation direction.
        */
      val networkIoDirection: AttributeSpec[String] =
        AttributeSpec(
          NetworkExperimentalAttributes.NetworkIoDirection,
          List(
            "receive",
            "transmit",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwModel,
          hwName,
          hwNetworkLogicalAddresses,
          hwNetworkPhysicalAddress,
          hwParent,
          hwSerialNumber,
          hwVendor,
          networkIoDirection,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Link status: `1` (up) or `0` (down).
    */
  object NetworkUp extends MetricSpec.Unsealed {

    val name: String = "hw.network.up"
    val description: String = "Link status: `1` (up) or `0` (down)."
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Logical addresses of the adapter (e.g. IP address, or WWPN)
        */
      val hwNetworkLogicalAddresses: AttributeSpec[Seq[String]] =
        AttributeSpec(
          HwExperimentalAttributes.HwNetworkLogicalAddresses,
          List(
            Seq("172.16.8.21", "57.11.193.42"),
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Physical address of the adapter (e.g. MAC address, or WWNN)
        */
      val hwNetworkPhysicalAddress: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwNetworkPhysicalAddress,
          List(
            "00-90-F5-E9-7B-36",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Serial number of the hardware component
        */
      val hwSerialNumber: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSerialNumber,
          List(
            "CNFCP0123456789",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwModel,
          hwName,
          hwNetworkLogicalAddresses,
          hwNetworkPhysicalAddress,
          hwParent,
          hwSerialNumber,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Endurance remaining for this SSD disk.
    */
  object PhysicalDiskEnduranceUtilization extends MetricSpec.Unsealed {

    val name: String = "hw.physical_disk.endurance_utilization"
    val description: String = "Endurance remaining for this SSD disk."
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Firmware version of the hardware component
        */
      val hwFirmwareVersion: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwFirmwareVersion,
          List(
            "2.0.1",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** State of the physical disk endurance utilization
        */
      val hwPhysicalDiskState: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwPhysicalDiskState,
          List(
            "remaining",
          ),
          Requirement.required,
          Stability.development
        )

      /** Type of the physical disk
        */
      val hwPhysicalDiskType: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwPhysicalDiskType,
          List(
            "HDD",
            "SSD",
            "10K",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Serial number of the hardware component
        */
      val hwSerialNumber: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSerialNumber,
          List(
            "CNFCP0123456789",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwFirmwareVersion,
          hwId,
          hwModel,
          hwName,
          hwParent,
          hwPhysicalDiskState,
          hwPhysicalDiskType,
          hwSerialNumber,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Size of the disk.
    */
  object PhysicalDiskSize extends MetricSpec.Unsealed {

    val name: String = "hw.physical_disk.size"
    val description: String = "Size of the disk."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Firmware version of the hardware component
        */
      val hwFirmwareVersion: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwFirmwareVersion,
          List(
            "2.0.1",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Type of the physical disk
        */
      val hwPhysicalDiskType: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwPhysicalDiskType,
          List(
            "HDD",
            "SSD",
            "10K",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Serial number of the hardware component
        */
      val hwSerialNumber: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSerialNumber,
          List(
            "CNFCP0123456789",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwFirmwareVersion,
          hwId,
          hwModel,
          hwName,
          hwParent,
          hwPhysicalDiskType,
          hwSerialNumber,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Value of the corresponding <a href="https://wikipedia.org/wiki/S.M.A.R.T.">S.M.A.R.T.</a> (Self-Monitoring,
    * Analysis, and Reporting Technology) attribute.
    */
  object PhysicalDiskSmart extends MetricSpec.Unsealed {

    val name: String = "hw.physical_disk.smart"
    val description: String =
      "Value of the corresponding [S.M.A.R.T.](https://wikipedia.org/wiki/S.M.A.R.T.) (Self-Monitoring, Analysis, and Reporting Technology) attribute."
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Firmware version of the hardware component
        */
      val hwFirmwareVersion: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwFirmwareVersion,
          List(
            "2.0.1",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** <a href="https://wikipedia.org/wiki/S.M.A.R.T.">S.M.A.R.T.</a> (Self-Monitoring, Analysis, and Reporting
        * Technology) attribute of the physical disk
        */
      val hwPhysicalDiskSmartAttribute: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwPhysicalDiskSmartAttribute,
          List(
            "Spin Retry Count",
            "Seek Error Rate",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Type of the physical disk
        */
      val hwPhysicalDiskType: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwPhysicalDiskType,
          List(
            "HDD",
            "SSD",
            "10K",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Serial number of the hardware component
        */
      val hwSerialNumber: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSerialNumber,
          List(
            "CNFCP0123456789",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwFirmwareVersion,
          hwId,
          hwModel,
          hwName,
          hwParent,
          hwPhysicalDiskSmartAttribute,
          hwPhysicalDiskType,
          hwSerialNumber,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Instantaneous power consumed by the component.
    *
    * @note
    *   <p> It is recommended to report `hw.energy` instead of `hw.power` when possible.
    */
  object Power extends MetricSpec.Unsealed {

    val name: String = "hw.power"
    val description: String = "Instantaneous power consumed by the component."
    val unit: String = "W"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Type of the component
        *
        * @note
        *   <p> Describes the category of the hardware component for which `hw.state` is being reported. For example,
        *   `hw.type=temperature` along with `hw.state=degraded` would indicate that the temperature of the hardware
        *   component has been reported as `degraded`.
        */
      val hwType: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwType,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwName,
          hwParent,
          hwType,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Maximum power output of the power supply.
    */
  object PowerSupplyLimit extends MetricSpec.Unsealed {

    val name: String = "hw.power_supply.limit"
    val description: String = "Maximum power output of the power supply."
    val unit: String = "W"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Type of limit for hardware components
        */
      val hwLimitType: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwLimitType,
          List(
            "max",
            "critical",
            "throttled",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Serial number of the hardware component
        */
      val hwSerialNumber: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSerialNumber,
          List(
            "CNFCP0123456789",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwLimitType,
          hwModel,
          hwName,
          hwParent,
          hwSerialNumber,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Current power output of the power supply.
    */
  object PowerSupplyUsage extends MetricSpec.Unsealed {

    val name: String = "hw.power_supply.usage"
    val description: String = "Current power output of the power supply."
    val unit: String = "W"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Serial number of the hardware component
        */
      val hwSerialNumber: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSerialNumber,
          List(
            "CNFCP0123456789",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwModel,
          hwName,
          hwParent,
          hwSerialNumber,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Utilization of the power supply as a fraction of its maximum output.
    */
  object PowerSupplyUtilization extends MetricSpec.Unsealed {

    val name: String = "hw.power_supply.utilization"
    val description: String = "Utilization of the power supply as a fraction of its maximum output."
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Serial number of the hardware component
        */
      val hwSerialNumber: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSerialNumber,
          List(
            "CNFCP0123456789",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwModel,
          hwName,
          hwParent,
          hwSerialNumber,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Operational status: `1` (true) or `0` (false) for each of the possible states.
    *
    * @note
    *   <p> `hw.status` is currently specified as an <em>UpDownCounter</em> but would ideally be represented using a <a
    *   href="https://github.com/prometheus/OpenMetrics/blob/v1.0.0/specification/OpenMetrics.md#stateset"><em>StateSet</em>
    *   as defined in OpenMetrics</a>. This semantic convention will be updated once <em>StateSet</em> is specified in
    *   OpenTelemetry. This planned change is not expected to have any consequence on the way users query their
    *   timeseries backend to retrieve the values of `hw.status` over time.
    */
  object Status extends MetricSpec.Unsealed {

    val name: String = "hw.status"
    val description: String = "Operational status: `1` (true) or `0` (false) for each of the possible states."
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The current state of the component
        */
      val hwState: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwState,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** Type of the component
        *
        * @note
        *   <p> Describes the category of the hardware component for which `hw.state` is being reported. For example,
        *   `hw.type=temperature` along with `hw.state=degraded` would indicate that the temperature of the hardware
        *   component has been reported as `degraded`.
        */
      val hwType: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwType,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwName,
          hwParent,
          hwState,
          hwType,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Operations performed by the tape drive.
    */
  object TapeDriveOperations extends MetricSpec.Unsealed {

    val name: String = "hw.tape_drive.operations"
    val description: String = "Operations performed by the tape drive."
    val unit: String = "{operation}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Descriptive model name of the hardware component
        */
      val hwModel: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwModel,
          List(
            "PERC H740P",
            "Intel(R) Core(TM) i7-10700K",
            "Dell XPS 15 Battery",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Serial number of the hardware component
        */
      val hwSerialNumber: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSerialNumber,
          List(
            "CNFCP0123456789",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Type of tape drive operation
        */
      val hwTapeDriveOperationType: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwTapeDriveOperationType,
          List(
            "mount",
            "unmount",
            "clean",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Vendor name of the hardware component
        */
      val hwVendor: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwVendor,
          List(
            "Dell",
            "HP",
            "Intel",
            "AMD",
            "LSI",
            "Lenovo",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwModel,
          hwName,
          hwParent,
          hwSerialNumber,
          hwTapeDriveOperationType,
          hwVendor,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Temperature in degrees Celsius.
    */
  object Temperature extends MetricSpec.Unsealed {

    val name: String = "hw.temperature"
    val description: String = "Temperature in degrees Celsius."
    val unit: String = "Cel"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Location of the sensor
        */
      val hwSensorLocation: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSensorLocation,
          List(
            "cpu0",
            "ps1",
            "INLET",
            "CPU0_DIE",
            "AMBIENT",
            "MOTHERBOARD",
            "PS0 V3_3",
            "MAIN_12V",
            "CPU_VCORE",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwName,
          hwParent,
          hwSensorLocation,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Temperature limit in degrees Celsius.
    */
  object TemperatureLimit extends MetricSpec.Unsealed {

    val name: String = "hw.temperature.limit"
    val description: String = "Temperature limit in degrees Celsius."
    val unit: String = "Cel"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Type of limit for hardware components
        */
      val hwLimitType: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwLimitType,
          List(
            "low.critical",
            "low.degraded",
            "high.degraded",
            "high.critical",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Location of the sensor
        */
      val hwSensorLocation: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSensorLocation,
          List(
            "cpu0",
            "ps1",
            "INLET",
            "CPU0_DIE",
            "AMBIENT",
            "MOTHERBOARD",
            "PS0 V3_3",
            "MAIN_12V",
            "CPU_VCORE",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwLimitType,
          hwName,
          hwParent,
          hwSensorLocation,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Voltage measured by the sensor.
    */
  object Voltage extends MetricSpec.Unsealed {

    val name: String = "hw.voltage"
    val description: String = "Voltage measured by the sensor."
    val unit: String = "V"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Location of the sensor
        */
      val hwSensorLocation: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSensorLocation,
          List(
            "cpu0",
            "ps1",
            "INLET",
            "CPU0_DIE",
            "AMBIENT",
            "MOTHERBOARD",
            "PS0 V3_3",
            "MAIN_12V",
            "CPU_VCORE",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwName,
          hwParent,
          hwSensorLocation,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Voltage limit in Volts.
    */
  object VoltageLimit extends MetricSpec.Unsealed {

    val name: String = "hw.voltage.limit"
    val description: String = "Voltage limit in Volts."
    val unit: String = "V"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** Type of limit for hardware components
        */
      val hwLimitType: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwLimitType,
          List(
            "low.critical",
            "low.degraded",
            "high.degraded",
            "high.critical",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Location of the sensor
        */
      val hwSensorLocation: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSensorLocation,
          List(
            "cpu0",
            "ps1",
            "INLET",
            "CPU0_DIE",
            "AMBIENT",
            "MOTHERBOARD",
            "PS0 V3_3",
            "MAIN_12V",
            "CPU_VCORE",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwLimitType,
          hwName,
          hwParent,
          hwSensorLocation,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Nominal (expected) voltage.
    */
  object VoltageNominal extends MetricSpec.Unsealed {

    val name: String = "hw.voltage.nominal"
    val description: String = "Nominal (expected) voltage."
    val unit: String = "V"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Location of the sensor
        */
      val hwSensorLocation: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwSensorLocation,
          List(
            "cpu0",
            "ps1",
            "INLET",
            "CPU0_DIE",
            "AMBIENT",
            "MOTHERBOARD",
            "PS0 V3_3",
            "MAIN_12V",
            "CPU_VCORE",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwName,
          hwParent,
          hwSensorLocation,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

}
