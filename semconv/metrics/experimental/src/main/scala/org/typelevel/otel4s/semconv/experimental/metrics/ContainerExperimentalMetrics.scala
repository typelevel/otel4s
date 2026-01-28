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
    FilesystemAvailable,
    FilesystemCapacity,
    FilesystemUsage,
    MemoryAvailable,
    MemoryPagingFaults,
    MemoryRss,
    MemoryUsage,
    MemoryWorkingSet,
    NetworkIo,
    Uptime,
  )

  /** Total CPU time consumed.
    *
    * @note
    *   <p> Total CPU time consumed by the specific container on all available CPU cores
    */
  object CpuTime extends MetricSpec.Unsealed {

    val name: String = "container.cpu.time"
    val description: String = "Total CPU time consumed."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The CPU mode for this data point. A container's CPU metric SHOULD be characterized <em>either</em> by data
        * points with no `mode` labels, <em>or only</em> data points with `mode` labels.
        *
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
          Stability.development
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

  /** Container's CPU usage, measured in cpus. Range from 0 to the number of allocatable CPUs.
    *
    * @note
    *   <p> CPU usage of the specific container on all available CPU cores, averaged over the sample window
    */
  object CpuUsage extends MetricSpec.Unsealed {

    val name: String = "container.cpu.usage"
    val description: String = "Container's CPU usage, measured in cpus. Range from 0 to the number of allocatable CPUs."
    val unit: String = "{cpu}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The CPU mode for this data point. A container's CPU metric SHOULD be characterized <em>either</em> by data
        * points with no `mode` labels, <em>or only</em> data points with `mode` labels.
        *
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
          Stability.development
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

  /** Disk bytes for the container.
    *
    * @note
    *   <p> The total number of bytes read/written successfully (aggregated from all disks).
    */
  object DiskIo extends MetricSpec.Unsealed {

    val name: String = "container.disk.io"
    val description: String = "Disk bytes for the container."
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

  /** Container filesystem available bytes.
    *
    * @note
    *   <p> In K8s, this metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#FsStats">FsStats.AvailableBytes</a>
    *   field of the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#ContainerStats">ContainerStats.Rootfs</a>
    *   of the Kubelet's stats API.
    */
  object FilesystemAvailable extends MetricSpec.Unsealed {

    val name: String = "container.filesystem.available"
    val description: String = "Container filesystem available bytes."
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

  /** Container filesystem capacity.
    *
    * @note
    *   <p> In K8s, this metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#FsStats">FsStats.CapacityBytes</a> field
    *   of the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#ContainerStats">ContainerStats.Rootfs</a>
    *   of the Kubelet's stats API.
    */
  object FilesystemCapacity extends MetricSpec.Unsealed {

    val name: String = "container.filesystem.capacity"
    val description: String = "Container filesystem capacity."
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

  /** Container filesystem usage.
    *
    * @note
    *   <p> This may not equal capacity - available. <p> In K8s, this metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#FsStats">FsStats.UsedBytes</a> field of
    *   the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#ContainerStats">ContainerStats.Rootfs</a>
    *   of the Kubelet's stats API.
    */
  object FilesystemUsage extends MetricSpec.Unsealed {

    val name: String = "container.filesystem.usage"
    val description: String = "Container filesystem usage."
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

  /** Container memory available.
    *
    * @note
    *   <p> Available memory for use. This is defined as the memory limit - workingSetBytes. If memory limit is
    *   undefined, the available bytes is omitted. In general, this metric can be derived from <a
    *   href="https://github.com/google/cadvisor/blob/v0.53.0/docs/storage/prometheus.md#prometheus-container-metrics">cadvisor</a>
    *   and by subtracting the `container_memory_working_set_bytes` metric from the `container_spec_memory_limit_bytes`
    *   metric. In K8s, this metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#MemoryStats">MemoryStats.AvailableBytes</a>
    *   field of the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#PodStats">PodStats.Memory</a> of the
    *   Kubelet's stats API.
    */
  object MemoryAvailable extends MetricSpec.Unsealed {

    val name: String = "container.memory.available"
    val description: String = "Container memory available."
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

  /** Container memory paging faults.
    *
    * @note
    *   <p> In general, this metric can be derived from <a
    *   href="https://github.com/google/cadvisor/blob/v0.53.0/docs/storage/prometheus.md#prometheus-container-metrics">cadvisor</a>
    *   and specifically the `container_memory_failures_total{failure_type=pgfault, scope=container}` and
    *   `container_memory_failures_total{failure_type=pgmajfault, scope=container}`metric. In K8s, this metric is
    *   derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#MemoryStats">MemoryStats.PageFaults</a>
    *   and <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#MemoryStats">MemoryStats.MajorPageFaults</a>
    *   field of the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#PodStats">PodStats.Memory</a> of the
    *   Kubelet's stats API.
    */
  object MemoryPagingFaults extends MetricSpec.Unsealed {

    val name: String = "container.memory.paging.faults"
    val description: String = "Container memory paging faults."
    val unit: String = "{fault}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The paging fault type
        */
      val systemPagingFaultType: AttributeSpec[String] =
        AttributeSpec(
          SystemExperimentalAttributes.SystemPagingFaultType,
          List(
            "minor",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          systemPagingFaultType,
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

  /** Container memory RSS.
    *
    * @note
    *   <p> In general, this metric can be derived from <a
    *   href="https://github.com/google/cadvisor/blob/v0.53.0/docs/storage/prometheus.md#prometheus-container-metrics">cadvisor</a>
    *   and specifically the `container_memory_rss` metric. In K8s, this metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#MemoryStats">MemoryStats.RSSBytes</a>
    *   field of the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#PodStats">PodStats.Memory</a> of the
    *   Kubelet's stats API.
    */
  object MemoryRss extends MetricSpec.Unsealed {

    val name: String = "container.memory.rss"
    val description: String = "Container memory RSS."
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

  /** Memory usage of the container.
    *
    * @note
    *   <p> Memory usage of the container.
    */
  object MemoryUsage extends MetricSpec.Unsealed {

    val name: String = "container.memory.usage"
    val description: String = "Memory usage of the container."
    val unit: String = "By"
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

  /** Container memory working set.
    *
    * @note
    *   <p> In general, this metric can be derived from <a
    *   href="https://github.com/google/cadvisor/blob/v0.53.0/docs/storage/prometheus.md#prometheus-container-metrics">cadvisor</a>
    *   and specifically the `container_memory_working_set_bytes` metric. In K8s, this metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#MemoryStats">MemoryStats.WorkingSetBytes</a>
    *   field of the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#PodStats">PodStats.Memory</a> of the
    *   Kubelet's stats API.
    */
  object MemoryWorkingSet extends MetricSpec.Unsealed {

    val name: String = "container.memory.working_set"
    val description: String = "Container memory working set."
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

  /** Network bytes for the container.
    *
    * @note
    *   <p> The number of bytes sent/received on all network interfaces by the container.
    */
  object NetworkIo extends MetricSpec.Unsealed {

    val name: String = "container.network.io"
    val description: String = "Network bytes for the container."
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

  /** The time the container has been running.
    *
    * @note
    *   <p> Instrumentations SHOULD use a gauge with type `double` and measure uptime in seconds as a floating point
    *   number with the highest precision available. The actual accuracy would depend on the instrumentation and
    *   operating system.
    */
  object Uptime extends MetricSpec.Unsealed {

    val name: String = "container.uptime"
    val description: String = "The time the container has been running."
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
