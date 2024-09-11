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

import org.typelevel.otel4s.metrics._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object SystemExperimentalMetrics {

  /** Reports the current frequency of the CPU in Hz
    */
  object CpuFrequency {

    val Name = "system.cpu.frequency"
    val Description = "Reports the current frequency of the CPU in Hz"
    val Unit = "{Hz}"

    object AttributeSpecs {

      /** The logical CPU number [0..n-1]
        */
      val systemCpuLogicalNumber: AttributeSpec[Long] =
        AttributeSpec(
          AttributeKey("system.cpu.logical_number"),
          List(
            1,
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemCpuLogicalNumber,
        )
    }

    def create[F[_]: Meter]: F[Gauge[F, Long]] =
      Meter[F]
        .gauge[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Reports the number of logical (virtual) processor cores created by the operating system to manage multitasking
    */
  object CpuLogicalCount {

    val Name = "system.cpu.logical.count"
    val Description =
      "Reports the number of logical (virtual) processor cores created by the operating system to manage multitasking"
    val Unit = "{cpu}"

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Reports the number of actual physical processor cores on the hardware
    */
  object CpuPhysicalCount {

    val Name = "system.cpu.physical.count"
    val Description = "Reports the number of actual physical processor cores on the hardware"
    val Unit = "{cpu}"

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Seconds each logical CPU spent on each mode
    */
  object CpuTime {

    val Name = "system.cpu.time"
    val Description = "Seconds each logical CPU spent on each mode"
    val Unit = "s"

    object AttributeSpecs {

      /** The CPU mode for this data point. A system's CPU SHOULD be characterized <em>either</em> by data points with
        * no `mode` labels, <em>or only</em> data points with `mode` labels. <p>
        * @note
        *   <p> Following states SHOULD be used: `user`, `system`, `nice`, `idle`, `iowait`, `interrupt`, `steal`
        */
      val cpuMode: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("cpu.mode"),
          List(
            "user",
            "system",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The logical CPU number [0..n-1]
        */
      val systemCpuLogicalNumber: AttributeSpec[Long] =
        AttributeSpec(
          AttributeKey("system.cpu.logical_number"),
          List(
            1,
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          cpuMode,
          systemCpuLogicalNumber,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Difference in system.cpu.time since the last measurement, divided by the elapsed time and number of logical CPUs
    */
  object CpuUtilization {

    val Name = "system.cpu.utilization"
    val Description =
      "Difference in system.cpu.time since the last measurement, divided by the elapsed time and number of logical CPUs"
    val Unit = "1"

    object AttributeSpecs {

      /** The CPU mode for this data point. A system's CPU SHOULD be characterized <em>either</em> by data points with
        * no `mode` labels, <em>or only</em> data points with `mode` labels. <p>
        * @note
        *   <p> Following modes SHOULD be used: `user`, `system`, `nice`, `idle`, `iowait`, `interrupt`, `steal`
        */
      val cpuMode: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("cpu.mode"),
          List(
            "user",
            "system",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The logical CPU number [0..n-1]
        */
      val systemCpuLogicalNumber: AttributeSpec[Long] =
        AttributeSpec(
          AttributeKey("system.cpu.logical_number"),
          List(
            1,
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          cpuMode,
          systemCpuLogicalNumber,
        )
    }

    def create[F[_]: Meter]: F[Gauge[F, Long]] =
      Meter[F]
        .gauge[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** */
  object DiskIo {

    val Name = "system.disk.io"
    val Description = ""
    val Unit = "By"

    object AttributeSpecs {

      /** The disk IO operation direction.
        */
      val diskIoDirection: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("disk.io.direction"),
          List(
            "read",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.device"),
          List(
            "(identifier)",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          diskIoDirection,
          systemDevice,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Time disk spent activated <p>
    * @note
    *   <p> The real elapsed time ("wall clock") used in the I/O path (time from operations running in parallel are not
    *   counted). Measured as: <p> <ul> <li>Linux: Field 13 from <a
    *   href="https://www.kernel.org/doc/Documentation/ABI/testing/procfs-diskstats">procfs-diskstats</a> <li>Windows:
    *   The complement of <a
    *   href="https://learn.microsoft.com/archive/blogs/askcore/windows-performance-monitor-disk-counters-explained#windows-performance-monitor-disk-counters-explained">"Disk%
    *   Idle Time"</a> performance counter: `uptime * (100 - "Disk\% Idle Time") / 100` </ul>
    */
  object DiskIoTime {

    val Name = "system.disk.io_time"
    val Description = "Time disk spent activated"
    val Unit = "s"

    object AttributeSpecs {

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.device"),
          List(
            "(identifier)",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemDevice,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** */
  object DiskMerged {

    val Name = "system.disk.merged"
    val Description = ""
    val Unit = "{operation}"

    object AttributeSpecs {

      /** The disk IO operation direction.
        */
      val diskIoDirection: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("disk.io.direction"),
          List(
            "read",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.device"),
          List(
            "(identifier)",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          diskIoDirection,
          systemDevice,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Sum of the time each operation took to complete <p>
    * @note
    *   <p> Because it is the sum of time each request took, parallel-issued requests each contribute to make the count
    *   grow. Measured as: <p> <ul> <li>Linux: Fields 7 & 11 from <a
    *   href="https://www.kernel.org/doc/Documentation/ABI/testing/procfs-diskstats">procfs-diskstats</a> <li>Windows:
    *   "Avg. Disk sec/Read" perf counter multiplied by "Disk Reads/sec" perf counter (similar for Writes) </ul>
    */
  object DiskOperationTime {

    val Name = "system.disk.operation_time"
    val Description = "Sum of the time each operation took to complete"
    val Unit = "s"

    object AttributeSpecs {

      /** The disk IO operation direction.
        */
      val diskIoDirection: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("disk.io.direction"),
          List(
            "read",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.device"),
          List(
            "(identifier)",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          diskIoDirection,
          systemDevice,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** */
  object DiskOperations {

    val Name = "system.disk.operations"
    val Description = ""
    val Unit = "{operation}"

    object AttributeSpecs {

      /** The disk IO operation direction.
        */
      val diskIoDirection: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("disk.io.direction"),
          List(
            "read",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.device"),
          List(
            "(identifier)",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          diskIoDirection,
          systemDevice,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** */
  object FilesystemUsage {

    val Name = "system.filesystem.usage"
    val Description = ""
    val Unit = "By"

    object AttributeSpecs {

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.device"),
          List(
            "(identifier)",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The filesystem mode
        */
      val systemFilesystemMode: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.filesystem.mode"),
          List(
            "rw, ro",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The filesystem mount path
        */
      val systemFilesystemMountpoint: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.filesystem.mountpoint"),
          List(
            "/mnt/data",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The filesystem state
        */
      val systemFilesystemState: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.filesystem.state"),
          List(
            "used",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The filesystem type
        */
      val systemFilesystemType: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.filesystem.type"),
          List(
            "ext4",
          ),
          Requirement.recommended,
          Stability.experimental
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

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** */
  object FilesystemUtilization {

    val Name = "system.filesystem.utilization"
    val Description = ""
    val Unit = "1"

    object AttributeSpecs {

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.device"),
          List(
            "(identifier)",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The filesystem mode
        */
      val systemFilesystemMode: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.filesystem.mode"),
          List(
            "rw, ro",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The filesystem mount path
        */
      val systemFilesystemMountpoint: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.filesystem.mountpoint"),
          List(
            "/mnt/data",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The filesystem state
        */
      val systemFilesystemState: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.filesystem.state"),
          List(
            "used",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The filesystem type
        */
      val systemFilesystemType: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.filesystem.type"),
          List(
            "ext4",
          ),
          Requirement.recommended,
          Stability.experimental
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

    def create[F[_]: Meter]: F[Gauge[F, Long]] =
      Meter[F]
        .gauge[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** An estimate of how much memory is available for starting new applications, without causing swapping <p>
    * @note
    *   <p> This is an alternative to `system.memory.usage` metric with `state=free`. Linux starting from 3.14 exports
    *   "available" memory. It takes "free" memory as a baseline, and then factors in kernel-specific values. This is
    *   supposed to be more accurate than just "free" memory. For reference, see the calculations <a
    *   href="https://superuser.com/a/980821">here</a>. See also `MemAvailable` in <a
    *   href="https://man7.org/linux/man-pages/man5/proc.5.html">/proc/meminfo</a>.
    */
  object LinuxMemoryAvailable {

    val Name = "system.linux.memory.available"
    val Description =
      "An estimate of how much memory is available for starting new applications, without causing swapping"
    val Unit = "By"

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Reports the memory used by the Linux kernel for managing caches of frequently used objects. <p>
    * @note
    *   <p> The sum over the `reclaimable` and `unreclaimable` state values in `linux.memory.slab.usage` SHOULD be equal
    *   to the total slab memory available on the system. Note that the total slab memory is not constant and may vary
    *   over time. See also the <a
    *   href="https://blogs.oracle.com/linux/post/understanding-linux-kernel-memory-statistics">Slab allocator</a> and
    *   `Slab` in <a href="https://man7.org/linux/man-pages/man5/proc.5.html">/proc/meminfo</a>.
    */
  object LinuxMemorySlabUsage {

    val Name = "system.linux.memory.slab.usage"
    val Description = "Reports the memory used by the Linux kernel for managing caches of frequently used objects."
    val Unit = "By"

    object AttributeSpecs {

      /** The Linux Slab memory state
        */
      val linuxMemorySlabState: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("linux.memory.slab.state"),
          List(
            "reclaimable",
            "unreclaimable",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          linuxMemorySlabState,
        )
    }

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Total memory available in the system. <p>
    * @note
    *   <p> Its value SHOULD equal the sum of `system.memory.state` over all states.
    */
  object MemoryLimit {

    val Name = "system.memory.limit"
    val Description = "Total memory available in the system."
    val Unit = "By"

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Shared memory used (mostly by tmpfs). <p>
    * @note
    *   <p> Equivalent of `shared` from <a href="https://man7.org/linux/man-pages/man1/free.1.html">`free` command</a>
    *   or `Shmem` from <a href="https://man7.org/linux/man-pages/man5/proc.5.html">`/proc/meminfo`</a>"
    */
  object MemoryShared {

    val Name = "system.memory.shared"
    val Description = "Shared memory used (mostly by tmpfs)."
    val Unit = "By"

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Reports memory in use by state. <p>
    * @note
    *   <p> The sum over all `system.memory.state` values SHOULD equal the total memory available on the system, that is
    *   `system.memory.limit`.
    */
  object MemoryUsage {

    val Name = "system.memory.usage"
    val Description = "Reports memory in use by state."
    val Unit = "By"

    object AttributeSpecs {

      /** The memory state
        */
      val systemMemoryState: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.memory.state"),
          List(
            "free",
            "cached",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemMemoryState,
        )
    }

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** */
  object MemoryUtilization {

    val Name = "system.memory.utilization"
    val Description = ""
    val Unit = "1"

    object AttributeSpecs {

      /** The memory state
        */
      val systemMemoryState: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.memory.state"),
          List(
            "free",
            "cached",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemMemoryState,
        )
    }

    def create[F[_]: Meter]: F[Gauge[F, Long]] =
      Meter[F]
        .gauge[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** */
  object NetworkConnections {

    val Name = "system.network.connections"
    val Description = ""
    val Unit = "{connection}"

    object AttributeSpecs {

      /** <a href="https://osi-model.com/transport-layer/">OSI transport layer</a> or <a
        * href="https://wikipedia.org/wiki/Inter-process_communication">inter-process communication method</a>. <p>
        * @note
        *   <p> The value SHOULD be normalized to lowercase. <p> Consider always setting the transport when setting a
        *   port number, since a port number is ambiguous without knowing the transport. For example different processes
        *   could be listening on TCP port 12345 and UDP port 12345.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("network.transport"),
          List(
            "tcp",
            "udp",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.device"),
          List(
            "(identifier)",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** A stateless protocol MUST NOT set this attribute
        */
      val systemNetworkState: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.network.state"),
          List(
            "close_wait",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          networkTransport,
          systemDevice,
          systemNetworkState,
        )
    }

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Count of packets that are dropped or discarded even though there was no error <p>
    * @note
    *   <p> Measured as: <p> <ul> <li>Linux: the `drop` column in `/proc/dev/net` (<a
    *   href="https://web.archive.org/web/20180321091318/http://www.onlamp.com/pub/a/linux/2000/11/16/LinuxAdmin.html">source</a>)
    *   <li>Windows: <a
    *   href="https://docs.microsoft.com/windows/win32/api/netioapi/ns-netioapi-mib_if_row2">`InDiscards`/`OutDiscards`</a>
    *   from <a href="https://docs.microsoft.com/windows/win32/api/netioapi/nf-netioapi-getifentry2">`GetIfEntry2`</a>
    *   </ul>
    */
  object NetworkDropped {

    val Name = "system.network.dropped"
    val Description = "Count of packets that are dropped or discarded even though there was no error"
    val Unit = "{packet}"

    object AttributeSpecs {

      /** The network IO operation direction.
        */
      val networkIoDirection: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("network.io.direction"),
          List(
            "transmit",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.device"),
          List(
            "(identifier)",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          networkIoDirection,
          systemDevice,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Count of network errors detected <p>
    * @note
    *   <p> Measured as: <p> <ul> <li>Linux: the `errs` column in `/proc/dev/net` (<a
    *   href="https://web.archive.org/web/20180321091318/http://www.onlamp.com/pub/a/linux/2000/11/16/LinuxAdmin.html">source</a>).
    *   <li>Windows: <a
    *   href="https://docs.microsoft.com/windows/win32/api/netioapi/ns-netioapi-mib_if_row2">`InErrors`/`OutErrors`</a>
    *   from <a href="https://docs.microsoft.com/windows/win32/api/netioapi/nf-netioapi-getifentry2">`GetIfEntry2`</a>.
    *   </ul>
    */
  object NetworkErrors {

    val Name = "system.network.errors"
    val Description = "Count of network errors detected"
    val Unit = "{error}"

    object AttributeSpecs {

      /** The network IO operation direction.
        */
      val networkIoDirection: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("network.io.direction"),
          List(
            "transmit",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.device"),
          List(
            "(identifier)",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          networkIoDirection,
          systemDevice,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** */
  object NetworkIo {

    val Name = "system.network.io"
    val Description = ""
    val Unit = "By"

    object AttributeSpecs {

      /** The network IO operation direction.
        */
      val networkIoDirection: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("network.io.direction"),
          List(
            "transmit",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.device"),
          List(
            "(identifier)",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          networkIoDirection,
          systemDevice,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** */
  object NetworkPackets {

    val Name = "system.network.packets"
    val Description = ""
    val Unit = "{packet}"

    object AttributeSpecs {

      /** The network IO operation direction.
        */
      val networkIoDirection: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("network.io.direction"),
          List(
            "transmit",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.device"),
          List(
            "(identifier)",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          networkIoDirection,
          systemDevice,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** */
  object PagingFaults {

    val Name = "system.paging.faults"
    val Description = ""
    val Unit = "{fault}"

    object AttributeSpecs {

      /** The memory paging type
        */
      val systemPagingType: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.paging.type"),
          List(
            "minor",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemPagingType,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** */
  object PagingOperations {

    val Name = "system.paging.operations"
    val Description = ""
    val Unit = "{operation}"

    object AttributeSpecs {

      /** The paging access direction
        */
      val systemPagingDirection: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.paging.direction"),
          List(
            "in",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The memory paging type
        */
      val systemPagingType: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.paging.type"),
          List(
            "minor",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemPagingDirection,
          systemPagingType,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Unix swap or windows pagefile usage
    */
  object PagingUsage {

    val Name = "system.paging.usage"
    val Description = "Unix swap or windows pagefile usage"
    val Unit = "By"

    object AttributeSpecs {

      /** The memory paging state
        */
      val systemPagingState: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.paging.state"),
          List(
            "free",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemPagingState,
        )
    }

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** */
  object PagingUtilization {

    val Name = "system.paging.utilization"
    val Description = ""
    val Unit = "1"

    object AttributeSpecs {

      /** The memory paging state
        */
      val systemPagingState: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.paging.state"),
          List(
            "free",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemPagingState,
        )
    }

    def create[F[_]: Meter]: F[Gauge[F, Long]] =
      Meter[F]
        .gauge[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Total number of processes in each state
    */
  object ProcessCount {

    val Name = "system.process.count"
    val Description = "Total number of processes in each state"
    val Unit = "{process}"

    object AttributeSpecs {

      /** The process state, e.g., <a href="https://man7.org/linux/man-pages/man1/ps.1.html#PROCESS_STATE_CODES">Linux
        * Process State Codes</a>
        */
      val systemProcessStatus: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("system.process.status"),
          List(
            "running",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemProcessStatus,
        )
    }

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Total number of processes created over uptime of the host
    */
  object ProcessCreated {

    val Name = "system.process.created"
    val Description = "Total number of processes created over uptime of the host"
    val Unit = "{process}"

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

}
