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
object ContainerExperimentalMetrics {

  /** Total CPU time consumed <p>
    * @note
    *   <p> Total CPU time consumed by the specific container on all available CPU cores
    */
  object CpuTime {

    val Name = "container.cpu.time"
    val Description = "Total CPU time consumed"
    val Unit = "s"

    object AttributeSpecs {

      /** The CPU mode for this data point. A container's CPU metric SHOULD be characterized <em>either</em> by data
        * points with no `mode` labels, <em>or only</em> data points with `mode` labels. <p>
        * @note
        *   <p> Following states SHOULD be used: `user`, `system`, `kernel`
        */
      val cpuMode: AttributeSpec[String] =
        AttributeSpec(
          CpuExperimentalAttributes.CpuMode,
          List(
            "user",
            "system",
          ),
          Requirement.optIn,
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

  /** Disk bytes for the container. <p>
    * @note
    *   <p> The total number of bytes read/written successfully (aggregated from all disks).
    */
  object DiskIo {

    val Name = "container.disk.io"
    val Description = "Disk bytes for the container."
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

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemDevice,
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

  /** Memory usage of the container. <p>
    * @note
    *   <p> Memory usage of the container.
    */
  object MemoryUsage {

    val Name = "container.memory.usage"
    val Description = "Memory usage of the container."
    val Unit = "By"

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Network bytes for the container. <p>
    * @note
    *   <p> The number of bytes sent/received on all network interfaces by the container.
    */
  object NetworkIo {

    val Name = "container.network.io"
    val Description = "Network bytes for the container."
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

      /** The device identifier
        */
      val systemDevice: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemDevice,
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

}
