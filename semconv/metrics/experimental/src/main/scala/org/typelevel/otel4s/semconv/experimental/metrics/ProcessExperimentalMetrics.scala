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
import org.typelevel.otel4s.semconv.experimental.attributes._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object ProcessExperimentalMetrics {

  /** Number of times the process has been context switched.
    */
  object ContextSwitches {

    val Name = "process.context_switches"
    val Description = "Number of times the process has been context switched."
    val Unit = "{count}"

    object AttributeSpecs {

      /** Specifies whether the context switches for this data point were voluntary or involuntary.
        */
      val processContextSwitchType: AttributeSpec[String] =
        AttributeSpec(
          ProcessExperimentalAttributes.ProcessContextSwitchType,
          List(
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          processContextSwitchType,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Total CPU seconds broken down by different states.
    */
  object CpuTime {

    val Name = "process.cpu.time"
    val Description = "Total CPU seconds broken down by different states."
    val Unit = "s"

    object AttributeSpecs {

      /** A process SHOULD be characterized <em>either</em> by data points with no `mode` labels, <em>or only</em> data
        * points with `mode` labels. <p>
        * @note
        *   <p> Following states SHOULD be used: `user`, `system`, `wait`
        */
      val cpuMode: AttributeSpec[String] =
        AttributeSpec(
          CpuExperimentalAttributes.CpuMode,
          List(
            "user",
            "system",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          cpuMode,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Difference in process.cpu.time since the last measurement, divided by the elapsed time and number of CPUs
    * available to the process.
    */
  object CpuUtilization {

    val Name = "process.cpu.utilization"
    val Description =
      "Difference in process.cpu.time since the last measurement, divided by the elapsed time and number of CPUs available to the process."
    val Unit = "1"

    object AttributeSpecs {

      /** A process SHOULD be characterized <em>either</em> by data points with no `mode` labels, <em>or only</em> data
        * points with `mode` labels. <p>
        * @note
        *   <p> Following states SHOULD be used: `user`, `system`, `wait`
        */
      val cpuMode: AttributeSpec[String] =
        AttributeSpec(
          CpuExperimentalAttributes.CpuMode,
          List(
            "user",
            "system",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          cpuMode,
        )
    }

    def create[F[_]: Meter]: F[Gauge[F, Long]] =
      Meter[F]
        .gauge[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Disk bytes transferred.
    */
  object DiskIo {

    val Name = "process.disk.io"
    val Description = "Disk bytes transferred."
    val Unit = "By"

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
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          diskIoDirection,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** The amount of physical memory in use.
    */
  object MemoryUsage {

    val Name = "process.memory.usage"
    val Description = "The amount of physical memory in use."
    val Unit = "By"

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** The amount of committed virtual memory.
    */
  object MemoryVirtual {

    val Name = "process.memory.virtual"
    val Description = "The amount of committed virtual memory."
    val Unit = "By"

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Network bytes transferred.
    */
  object NetworkIo {

    val Name = "process.network.io"
    val Description = "Network bytes transferred."
    val Unit = "By"

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
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          networkIoDirection,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Number of file descriptors in use by the process.
    */
  object OpenFileDescriptorCount {

    val Name = "process.open_file_descriptor.count"
    val Description = "Number of file descriptors in use by the process."
    val Unit = "{count}"

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Number of page faults the process has made.
    */
  object PagingFaults {

    val Name = "process.paging.faults"
    val Description = "Number of page faults the process has made."
    val Unit = "{fault}"

    object AttributeSpecs {

      /** The type of page fault for this data point. Type `major` is for major/hard page faults, and `minor` is for
        * minor/soft page faults.
        */
      val processPagingFaultType: AttributeSpec[String] =
        AttributeSpec(
          ProcessExperimentalAttributes.ProcessPagingFaultType,
          List(
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          processPagingFaultType,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Process threads count.
    */
  object ThreadCount {

    val Name = "process.thread.count"
    val Description = "Process threads count."
    val Unit = "{thread}"

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

}
