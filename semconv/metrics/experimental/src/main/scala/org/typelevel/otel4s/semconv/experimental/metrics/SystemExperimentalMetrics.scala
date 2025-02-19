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
object SystemExperimentalMetrics {

  val specs: List[MetricSpec] = List(
    CpuFrequency,
    CpuLogicalCount,
    CpuPhysicalCount,
    CpuTime,
    CpuUtilization,
    DiskIo,
    DiskIoTime,
    DiskLimit,
    DiskMerged,
    DiskOperationTime,
    DiskOperations,
    FilesystemLimit,
    FilesystemUsage,
    FilesystemUtilization,
    LinuxMemoryAvailable,
    LinuxMemorySlabUsage,
    MemoryLimit,
    MemoryShared,
    MemoryUsage,
    MemoryUtilization,
    NetworkConnections,
    NetworkDropped,
    NetworkErrors,
    NetworkIo,
    NetworkPackets,
    PagingFaults,
    PagingOperations,
    PagingUsage,
    PagingUtilization,
    ProcessCount,
    ProcessCreated,
    Uptime,
  )

  /** Reports the current frequency of the CPU in Hz
    */
  object CpuFrequency extends MetricSpec {

    val name: String = "system.cpu.frequency"
    val description: String = "Reports the current frequency of the CPU in Hz"
    val unit: String = "{Hz}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The logical CPU number [0..n-1]
        */
      val systemCpuLogicalNumber: AttributeSpec[Long] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemCpuLogicalNumber,
          List(
            1,
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemCpuLogicalNumber,
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

  /** Reports the number of logical (virtual) processor cores created by the operating system to manage multitasking <p>
    * @note
    *   <p> Calculated by multiplying the number of sockets by the number of cores per socket, and then by the number of
    *   threads per core
    */
  object CpuLogicalCount extends MetricSpec {

    val name: String = "system.cpu.logical.count"
    val description: String =
      "Reports the number of logical (virtual) processor cores created by the operating system to manage multitasking"
    val unit: String = "{cpu}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

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

  /** Reports the number of actual physical processor cores on the hardware <p>
    * @note
    *   <p> Calculated by multiplying the number of sockets by the number of cores per socket
    */
  object CpuPhysicalCount extends MetricSpec {

    val name: String = "system.cpu.physical.count"
    val description: String = "Reports the number of actual physical processor cores on the hardware"
    val unit: String = "{cpu}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

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

  /** Seconds each logical CPU spent on each mode
    */
  object CpuTime extends MetricSpec {

    val name: String = "system.cpu.time"
    val description: String = "Seconds each logical CPU spent on each mode"
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The CPU mode for this data point. A system's CPU SHOULD be characterized <em>either</em> by data points with
        * no `mode` labels, <em>or only</em> data points with `mode` labels. <p>
        * @note
        *   <p> Following states SHOULD be used: `user`, `system`, `nice`, `idle`, `iowait`, `interrupt`, `steal`
        */
      val cpuMode: AttributeSpec[String] =
        AttributeSpec(
          CpuExperimentalAttributes.CpuMode,
          List(
            "user",
            "system",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The logical CPU number [0..n-1]
        */
      val systemCpuLogicalNumber: AttributeSpec[Long] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemCpuLogicalNumber,
          List(
            1,
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          cpuMode,
          systemCpuLogicalNumber,
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

  /** Difference in system.cpu.time since the last measurement, divided by the elapsed time and number of logical CPUs
    */
  object CpuUtilization extends MetricSpec {

    val name: String = "system.cpu.utilization"
    val description: String =
      "Difference in system.cpu.time since the last measurement, divided by the elapsed time and number of logical CPUs"
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The CPU mode for this data point. A system's CPU SHOULD be characterized <em>either</em> by data points with
        * no `mode` labels, <em>or only</em> data points with `mode` labels. <p>
        * @note
        *   <p> Following modes SHOULD be used: `user`, `system`, `nice`, `idle`, `iowait`, `interrupt`, `steal`
        */
      val cpuMode: AttributeSpec[String] =
        AttributeSpec(
          CpuExperimentalAttributes.CpuMode,
          List(
            "user",
            "system",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The logical CPU number [0..n-1]
        */
      val systemCpuLogicalNumber: AttributeSpec[Long] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemCpuLogicalNumber,
          List(
            1,
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          cpuMode,
          systemCpuLogicalNumber,
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

  /** */
  object DiskIo extends MetricSpec {

    val name: String = "system.disk.io"
    val description: String = ""
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The disk IO operation direction.
        */
      val diskIoDirection: AttributeSpec[String] =
        AttributeSpec(
          DiskExperimentalAttributes.DiskIoDirection,
          List(
            "read",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemDevice,
          List(
            "(identifier)",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          diskIoDirection,
          systemDevice,
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

  /** Time disk spent activated <p>
    * @note
    *   <p> The real elapsed time ("wall clock") used in the I/O path (time from operations running in parallel are not
    *   counted). Measured as: <ul> <li>Linux: Field 13 from <a
    *   href="https://www.kernel.org/doc/Documentation/ABI/testing/procfs-diskstats">procfs-diskstats</a> <li>Windows:
    *   The complement of <a
    *   href="https://learn.microsoft.com/archive/blogs/askcore/windows-performance-monitor-disk-counters-explained#windows-performance-monitor-disk-counters-explained">"Disk%
    *   Idle Time"</a> performance counter: `uptime * (100 - "Disk\% Idle Time") / 100` </ul>
    */
  object DiskIoTime extends MetricSpec {

    val name: String = "system.disk.io_time"
    val description: String = "Time disk spent activated"
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemDevice,
          List(
            "(identifier)",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemDevice,
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

  /** The total storage capacity of the disk
    */
  object DiskLimit extends MetricSpec {

    val name: String = "system.disk.limit"
    val description: String = "The total storage capacity of the disk"
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemDevice,
          List(
            "(identifier)",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemDevice,
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

  /** */
  object DiskMerged extends MetricSpec {

    val name: String = "system.disk.merged"
    val description: String = ""
    val unit: String = "{operation}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The disk IO operation direction.
        */
      val diskIoDirection: AttributeSpec[String] =
        AttributeSpec(
          DiskExperimentalAttributes.DiskIoDirection,
          List(
            "read",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemDevice,
          List(
            "(identifier)",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          diskIoDirection,
          systemDevice,
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

  /** Sum of the time each operation took to complete <p>
    * @note
    *   <p> Because it is the sum of time each request took, parallel-issued requests each contribute to make the count
    *   grow. Measured as: <ul> <li>Linux: Fields 7 & 11 from <a
    *   href="https://www.kernel.org/doc/Documentation/ABI/testing/procfs-diskstats">procfs-diskstats</a> <li>Windows:
    *   "Avg. Disk sec/Read" perf counter multiplied by "Disk Reads/sec" perf counter (similar for Writes) </ul>
    */
  object DiskOperationTime extends MetricSpec {

    val name: String = "system.disk.operation_time"
    val description: String = "Sum of the time each operation took to complete"
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The disk IO operation direction.
        */
      val diskIoDirection: AttributeSpec[String] =
        AttributeSpec(
          DiskExperimentalAttributes.DiskIoDirection,
          List(
            "read",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemDevice,
          List(
            "(identifier)",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          diskIoDirection,
          systemDevice,
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

  /** */
  object DiskOperations extends MetricSpec {

    val name: String = "system.disk.operations"
    val description: String = ""
    val unit: String = "{operation}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The disk IO operation direction.
        */
      val diskIoDirection: AttributeSpec[String] =
        AttributeSpec(
          DiskExperimentalAttributes.DiskIoDirection,
          List(
            "read",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemDevice,
          List(
            "(identifier)",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          diskIoDirection,
          systemDevice,
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

  /** The total storage capacity of the filesystem
    */
  object FilesystemLimit extends MetricSpec {

    val name: String = "system.filesystem.limit"
    val description: String = "The total storage capacity of the filesystem"
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Identifier for the device where the filesystem resides.
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemDevice,
          List(
            "/dev/sda",
            "\network-drive",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The filesystem mode
        */
      val systemFilesystemMode: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemFilesystemMode,
          List(
            "rw, ro",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The filesystem mount path
        */
      val systemFilesystemMountpoint: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemFilesystemMountpoint,
          List(
            "/mnt/data",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The filesystem type
        */
      val systemFilesystemType: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemFilesystemType,
          List(
            "ext4",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemDevice,
          systemFilesystemMode,
          systemFilesystemMountpoint,
          systemFilesystemType,
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

  /** Reports a filesystem's space usage across different states. <p>
    * @note
    *   <p> The sum of all `system.filesystem.usage` values over the different `system.filesystem.state` attributes
    *   SHOULD equal the total storage capacity of the filesystem, that is `system.filesystem.limit`.
    */
  object FilesystemUsage extends MetricSpec {

    val name: String = "system.filesystem.usage"
    val description: String = "Reports a filesystem's space usage across different states."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Identifier for the device where the filesystem resides.
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemDevice,
          List(
            "/dev/sda",
            "\network-drive",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The filesystem mode
        */
      val systemFilesystemMode: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemFilesystemMode,
          List(
            "rw, ro",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The filesystem mount path
        */
      val systemFilesystemMountpoint: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemFilesystemMountpoint,
          List(
            "/mnt/data",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The filesystem state
        */
      val systemFilesystemState: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemFilesystemState,
          List(
            "used",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The filesystem type
        */
      val systemFilesystemType: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemFilesystemType,
          List(
            "ext4",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemDevice,
          systemFilesystemMode,
          systemFilesystemMountpoint,
          systemFilesystemState,
          systemFilesystemType,
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

  /** */
  object FilesystemUtilization extends MetricSpec {

    val name: String = "system.filesystem.utilization"
    val description: String = ""
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Identifier for the device where the filesystem resides.
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemDevice,
          List(
            "/dev/sda",
            "\network-drive",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The filesystem mode
        */
      val systemFilesystemMode: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemFilesystemMode,
          List(
            "rw, ro",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The filesystem mount path
        */
      val systemFilesystemMountpoint: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemFilesystemMountpoint,
          List(
            "/mnt/data",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The filesystem state
        */
      val systemFilesystemState: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemFilesystemState,
          List(
            "used",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The filesystem type
        */
      val systemFilesystemType: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemFilesystemType,
          List(
            "ext4",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemDevice,
          systemFilesystemMode,
          systemFilesystemMountpoint,
          systemFilesystemState,
          systemFilesystemType,
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

  /** An estimate of how much memory is available for starting new applications, without causing swapping <p>
    * @note
    *   <p> This is an alternative to `system.memory.usage` metric with `state=free`. Linux starting from 3.14 exports
    *   "available" memory. It takes "free" memory as a baseline, and then factors in kernel-specific values. This is
    *   supposed to be more accurate than just "free" memory. For reference, see the calculations <a
    *   href="https://superuser.com/a/980821">here</a>. See also `MemAvailable` in <a
    *   href="https://man7.org/linux/man-pages/man5/proc.5.html">/proc/meminfo</a>.
    */
  object LinuxMemoryAvailable extends MetricSpec {

    val name: String = "system.linux.memory.available"
    val description: String =
      "An estimate of how much memory is available for starting new applications, without causing swapping"
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

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

  /** Reports the memory used by the Linux kernel for managing caches of frequently used objects. <p>
    * @note
    *   <p> The sum over the `reclaimable` and `unreclaimable` state values in `linux.memory.slab.usage` SHOULD be equal
    *   to the total slab memory available on the system. Note that the total slab memory is not constant and may vary
    *   over time. See also the <a
    *   href="https://blogs.oracle.com/linux/post/understanding-linux-kernel-memory-statistics">Slab allocator</a> and
    *   `Slab` in <a href="https://man7.org/linux/man-pages/man5/proc.5.html">/proc/meminfo</a>.
    */
  object LinuxMemorySlabUsage extends MetricSpec {

    val name: String = "system.linux.memory.slab.usage"
    val description: String =
      "Reports the memory used by the Linux kernel for managing caches of frequently used objects."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The Linux Slab memory state
        */
      val linuxMemorySlabState: AttributeSpec[String] =
        AttributeSpec(
          LinuxExperimentalAttributes.LinuxMemorySlabState,
          List(
            "reclaimable",
            "unreclaimable",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          linuxMemorySlabState,
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

  /** Total memory available in the system. <p>
    * @note
    *   <p> Its value SHOULD equal the sum of `system.memory.state` over all states.
    */
  object MemoryLimit extends MetricSpec {

    val name: String = "system.memory.limit"
    val description: String = "Total memory available in the system."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

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

  /** Shared memory used (mostly by tmpfs). <p>
    * @note
    *   <p> Equivalent of `shared` from <a href="https://man7.org/linux/man-pages/man1/free.1.html">`free` command</a>
    *   or `Shmem` from <a href="https://man7.org/linux/man-pages/man5/proc.5.html">`/proc/meminfo`</a>"
    */
  object MemoryShared extends MetricSpec {

    val name: String = "system.memory.shared"
    val description: String = "Shared memory used (mostly by tmpfs)."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

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

  /** Reports memory in use by state. <p>
    * @note
    *   <p> The sum over all `system.memory.state` values SHOULD equal the total memory available on the system, that is
    *   `system.memory.limit`.
    */
  object MemoryUsage extends MetricSpec {

    val name: String = "system.memory.usage"
    val description: String = "Reports memory in use by state."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The memory state
        */
      val systemMemoryState: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemMemoryState,
          List(
            "free",
            "cached",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemMemoryState,
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

  /** */
  object MemoryUtilization extends MetricSpec {

    val name: String = "system.memory.utilization"
    val description: String = ""
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The memory state
        */
      val systemMemoryState: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemMemoryState,
          List(
            "free",
            "cached",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemMemoryState,
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

  /** */
  object NetworkConnections extends MetricSpec {

    val name: String = "system.network.connections"
    val description: String = ""
    val unit: String = "{connection}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The state of network connection <p>
        * @note
        *   <p> Connection states are defined as part of the <a
        *   href="https://datatracker.ietf.org/doc/html/rfc9293#section-3.3.2">rfc9293</a>
        */
      val networkConnectionState: AttributeSpec[String] =
        AttributeSpec(
          NetworkExperimentalAttributes.NetworkConnectionState,
          List(
            "close_wait",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The network interface name.
        */
      val networkInterfaceName: AttributeSpec[String] =
        AttributeSpec(
          NetworkExperimentalAttributes.NetworkInterfaceName,
          List(
            "lo",
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** <a href="https://wikipedia.org/wiki/Transport_layer">OSI transport layer</a> or <a
        * href="https://wikipedia.org/wiki/Inter-process_communication">inter-process communication method</a>. <p>
        * @note
        *   <p> The value SHOULD be normalized to lowercase. <p> Consider always setting the transport when setting a
        *   port number, since a port number is ambiguous without knowing the transport. For example different processes
        *   could be listening on TCP port 12345 and UDP port 12345.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkTransport,
          List(
            "tcp",
            "udp",
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          networkConnectionState,
          networkInterfaceName,
          networkTransport,
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

  /** Count of packets that are dropped or discarded even though there was no error <p>
    * @note
    *   <p> Measured as: <ul> <li>Linux: the `drop` column in `/proc/dev/net` (<a
    *   href="https://web.archive.org/web/20180321091318/http://www.onlamp.com/pub/a/linux/2000/11/16/LinuxAdmin.html">source</a>)
    *   <li>Windows: <a
    *   href="https://docs.microsoft.com/windows/win32/api/netioapi/ns-netioapi-mib_if_row2">`InDiscards`/`OutDiscards`</a>
    *   from <a href="https://docs.microsoft.com/windows/win32/api/netioapi/nf-netioapi-getifentry2">`GetIfEntry2`</a>
    *   </ul>
    */
  object NetworkDropped extends MetricSpec {

    val name: String = "system.network.dropped"
    val description: String = "Count of packets that are dropped or discarded even though there was no error"
    val unit: String = "{packet}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The network interface name.
        */
      val networkInterfaceName: AttributeSpec[String] =
        AttributeSpec(
          NetworkExperimentalAttributes.NetworkInterfaceName,
          List(
            "lo",
            "eth0",
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
            "transmit",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          networkInterfaceName,
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

  /** Count of network errors detected <p>
    * @note
    *   <p> Measured as: <ul> <li>Linux: the `errs` column in `/proc/dev/net` (<a
    *   href="https://web.archive.org/web/20180321091318/http://www.onlamp.com/pub/a/linux/2000/11/16/LinuxAdmin.html">source</a>).
    *   <li>Windows: <a
    *   href="https://docs.microsoft.com/windows/win32/api/netioapi/ns-netioapi-mib_if_row2">`InErrors`/`OutErrors`</a>
    *   from <a href="https://docs.microsoft.com/windows/win32/api/netioapi/nf-netioapi-getifentry2">`GetIfEntry2`</a>.
    *   </ul>
    */
  object NetworkErrors extends MetricSpec {

    val name: String = "system.network.errors"
    val description: String = "Count of network errors detected"
    val unit: String = "{error}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The network interface name.
        */
      val networkInterfaceName: AttributeSpec[String] =
        AttributeSpec(
          NetworkExperimentalAttributes.NetworkInterfaceName,
          List(
            "lo",
            "eth0",
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
            "transmit",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          networkInterfaceName,
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

  /** */
  object NetworkIo extends MetricSpec {

    val name: String = "system.network.io"
    val description: String = ""
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The network interface name.
        */
      val networkInterfaceName: AttributeSpec[String] =
        AttributeSpec(
          NetworkExperimentalAttributes.NetworkInterfaceName,
          List(
            "lo",
            "eth0",
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
            "transmit",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          networkInterfaceName,
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

  /** */
  object NetworkPackets extends MetricSpec {

    val name: String = "system.network.packets"
    val description: String = ""
    val unit: String = "{packet}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The network IO operation direction.
        */
      val networkIoDirection: AttributeSpec[String] =
        AttributeSpec(
          NetworkExperimentalAttributes.NetworkIoDirection,
          List(
            "transmit",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemDevice,
          List(
            "(identifier)",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          networkIoDirection,
          systemDevice,
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

  /** */
  object PagingFaults extends MetricSpec {

    val name: String = "system.paging.faults"
    val description: String = ""
    val unit: String = "{fault}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The memory paging type
        */
      val systemPagingType: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemPagingType,
          List(
            "minor",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemPagingType,
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

  /** */
  object PagingOperations extends MetricSpec {

    val name: String = "system.paging.operations"
    val description: String = ""
    val unit: String = "{operation}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The paging access direction
        */
      val systemPagingDirection: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemPagingDirection,
          List(
            "in",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The memory paging type
        */
      val systemPagingType: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemPagingType,
          List(
            "minor",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemPagingDirection,
          systemPagingType,
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

  /** Unix swap or windows pagefile usage
    */
  object PagingUsage extends MetricSpec {

    val name: String = "system.paging.usage"
    val description: String = "Unix swap or windows pagefile usage"
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Unique identifier for the device responsible for managing paging operations.
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemDevice,
          List(
            "/dev/dm-0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The memory paging state
        */
      val systemPagingState: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemPagingState,
          List(
            "free",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemDevice,
          systemPagingState,
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

  /** */
  object PagingUtilization extends MetricSpec {

    val name: String = "system.paging.utilization"
    val description: String = ""
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Unique identifier for the device responsible for managing paging operations.
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemDevice,
          List(
            "/dev/dm-0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The memory paging state
        */
      val systemPagingState: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemPagingState,
          List(
            "free",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemDevice,
          systemPagingState,
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

  /** Total number of processes in each state
    */
  object ProcessCount extends MetricSpec {

    val name: String = "system.process.count"
    val description: String = "Total number of processes in each state"
    val unit: String = "{process}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The process state, e.g., <a href="https://man7.org/linux/man-pages/man1/ps.1.html#PROCESS_STATE_CODES">Linux
        * Process State Codes</a>
        */
      val systemProcessStatus: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemProcessStatus,
          List(
            "running",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemProcessStatus,
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

  /** Total number of processes created over uptime of the host
    */
  object ProcessCreated extends MetricSpec {

    val name: String = "system.process.created"
    val description: String = "Total number of processes created over uptime of the host"
    val unit: String = "{process}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

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

  /** The time the system has been running <p>
    * @note
    *   <p> Instrumentations SHOULD use a gauge with type `double` and measure uptime in seconds as a floating point
    *   number with the highest precision available. The actual accuracy would depend on the instrumentation and
    *   operating system.
    */
  object Uptime extends MetricSpec {

    val name: String = "system.uptime"
    val description: String = "The time the system has been running"
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

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
