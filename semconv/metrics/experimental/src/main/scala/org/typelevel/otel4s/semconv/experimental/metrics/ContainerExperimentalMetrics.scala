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
import org.typelevel.otel4s.semconv.experimental.attributes._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object ContainerExperimentalMetrics {

  val specs: List[MetricSpec] = List(
    CpuTime,
    CpuUsage,
    DiskIo,
    MemoryUsage,
    NetworkIo,
  )

  /** Total CPU time consumed <p>
    * @note
    *   <p> Total CPU time consumed by the specific container on all available CPU cores
    */
  object CpuTime extends MetricSpec {

    val name: String = "container.cpu.time"
    val description: String = "Total CPU time consumed"
    val unit: String = "s"
    val stability: Stability = Stability.experimental
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

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
          Requirement.conditionallyRequired(
            "Required if mode is available, i.e. metrics coming from the Docker Stats API."
          ),
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          cpuMode,
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

  /** Container's CPU usage, measured in cpus. Range from 0 to the number of allocatable CPUs <p>
    * @note
    *   <p> CPU usage of the specific container on all available CPU cores, averaged over the sample window
    */
  object CpuUsage extends MetricSpec {

    val name: String = "container.cpu.usage"
    val description: String = "Container's CPU usage, measured in cpus. Range from 0 to the number of allocatable CPUs"
    val unit: String = "{cpu}"
    val stability: Stability = Stability.experimental
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

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
          Requirement.conditionallyRequired(
            "Required if mode is available, i.e. metrics coming from the Docker Stats API."
          ),
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          cpuMode,
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

  /** Disk bytes for the container. <p>
    * @note
    *   <p> The total number of bytes read/written successfully (aggregated from all disks).
    */
  object DiskIo extends MetricSpec {

    val name: String = "container.disk.io"
    val description: String = "Disk bytes for the container."
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

  /** Memory usage of the container. <p>
    * @note
    *   <p> Memory usage of the container.
    */
  object MemoryUsage extends MetricSpec {

    val name: String = "container.memory.usage"
    val description: String = "Memory usage of the container."
    val unit: String = "By"
    val stability: Stability = Stability.experimental
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

  /** Network bytes for the container. <p>
    * @note
    *   <p> The number of bytes sent/received on all network interfaces by the container.
    */
  object NetworkIo extends MetricSpec {

    val name: String = "container.network.io"
    val description: String = "Network bytes for the container."
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

}
