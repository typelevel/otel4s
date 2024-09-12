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

  val specs: List[MetricSpec] = List(
    ContextSwitches,
    CpuTime,
    CpuUtilization,
    DiskIo,
    MemoryUsage,
    MemoryVirtual,
    NetworkIo,
    OpenFileDescriptorCount,
    PagingFaults,
    ThreadCount,
  )

  /** Number of times the process has been context switched.
    */
  object ContextSwitches extends MetricSpec {

    val name: String = "process.context_switches"
    val description: String = "Number of times the process has been context switched."
    val unit: String = "{count}"
    val stability: Stability = Stability.experimental
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

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
        .counter[Long](name)
        .withDescription(description)
        .withUnit(unit)
        .create

  }

  /** Total CPU seconds broken down by different states.
    */
  object CpuTime extends MetricSpec {

    val name: String = "process.cpu.time"
    val description: String = "Total CPU seconds broken down by different states."
    val unit: String = "s"
    val stability: Stability = Stability.experimental
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

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
        .counter[Long](name)
        .withDescription(description)
        .withUnit(unit)
        .create

  }

  /** Difference in process.cpu.time since the last measurement, divided by the elapsed time and number of CPUs
    * available to the process.
    */
  object CpuUtilization extends MetricSpec {

    val name: String = "process.cpu.utilization"
    val description: String =
      "Difference in process.cpu.time since the last measurement, divided by the elapsed time and number of CPUs available to the process."
    val unit: String = "1"
    val stability: Stability = Stability.experimental
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

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
        .gauge[Long](name)
        .withDescription(description)
        .withUnit(unit)
        .create

  }

  /** Disk bytes transferred.
    */
  object DiskIo extends MetricSpec {

    val name: String = "process.disk.io"
    val description: String = "Disk bytes transferred."
    val unit: String = "By"
    val stability: Stability = Stability.experimental
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
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          diskIoDirection,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](name)
        .withDescription(description)
        .withUnit(unit)
        .create

  }

  /** The amount of physical memory in use.
    */
  object MemoryUsage extends MetricSpec {

    val name: String = "process.memory.usage"
    val description: String = "The amount of physical memory in use."
    val unit: String = "By"
    val stability: Stability = Stability.experimental
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](name)
        .withDescription(description)
        .withUnit(unit)
        .create

  }

  /** The amount of committed virtual memory.
    */
  object MemoryVirtual extends MetricSpec {

    val name: String = "process.memory.virtual"
    val description: String = "The amount of committed virtual memory."
    val unit: String = "By"
    val stability: Stability = Stability.experimental
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](name)
        .withDescription(description)
        .withUnit(unit)
        .create

  }

  /** Network bytes transferred.
    */
  object NetworkIo extends MetricSpec {

    val name: String = "process.network.io"
    val description: String = "Network bytes transferred."
    val unit: String = "By"
    val stability: Stability = Stability.experimental
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
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          networkIoDirection,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](name)
        .withDescription(description)
        .withUnit(unit)
        .create

  }

  /** Number of file descriptors in use by the process.
    */
  object OpenFileDescriptorCount extends MetricSpec {

    val name: String = "process.open_file_descriptor.count"
    val description: String = "Number of file descriptors in use by the process."
    val unit: String = "{count}"
    val stability: Stability = Stability.experimental
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](name)
        .withDescription(description)
        .withUnit(unit)
        .create

  }

  /** Number of page faults the process has made.
    */
  object PagingFaults extends MetricSpec {

    val name: String = "process.paging.faults"
    val description: String = "Number of page faults the process has made."
    val unit: String = "{fault}"
    val stability: Stability = Stability.experimental
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

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
        .counter[Long](name)
        .withDescription(description)
        .withUnit(unit)
        .create

  }

  /** Process threads count.
    */
  object ThreadCount extends MetricSpec {

    val name: String = "process.thread.count"
    val description: String = "Process threads count."
    val unit: String = "{thread}"
    val stability: Stability = Stability.experimental
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](name)
        .withDescription(description)
        .withUnit(unit)
        .create

  }

}
