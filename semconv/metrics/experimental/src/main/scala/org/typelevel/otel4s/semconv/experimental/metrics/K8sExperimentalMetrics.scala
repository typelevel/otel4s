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
object K8sExperimentalMetrics {

  @annotation.nowarn("cat=deprecation")
  val specs: List[MetricSpec] = List(
    ContainerCpuLimit,
    ContainerCpuLimitUtilization,
    ContainerCpuRequest,
    ContainerCpuRequestUtilization,
    ContainerEphemeralStorageLimit,
    ContainerEphemeralStorageRequest,
    ContainerMemoryLimit,
    ContainerMemoryRequest,
    ContainerReady,
    ContainerRestartCount,
    ContainerStatusReason,
    ContainerStatusState,
    ContainerStorageLimit,
    ContainerStorageRequest,
    CronjobActiveJobs,
    CronjobJobActive,
    DaemonsetCurrentScheduledNodes,
    DaemonsetDesiredScheduledNodes,
    DaemonsetMisscheduledNodes,
    DaemonsetNodeCurrentScheduled,
    DaemonsetNodeDesiredScheduled,
    DaemonsetNodeMisscheduled,
    DaemonsetNodeReady,
    DaemonsetReadyNodes,
    DeploymentAvailablePods,
    DeploymentDesiredPods,
    DeploymentPodAvailable,
    DeploymentPodDesired,
    HpaCurrentPods,
    HpaDesiredPods,
    HpaMaxPods,
    HpaMetricTargetCpuAverageUtilization,
    HpaMetricTargetCpuAverageValue,
    HpaMetricTargetCpuValue,
    HpaMinPods,
    HpaPodCurrent,
    HpaPodDesired,
    HpaPodMax,
    HpaPodMin,
    JobActivePods,
    JobDesiredSuccessfulPods,
    JobFailedPods,
    JobMaxParallelPods,
    JobPodActive,
    JobPodDesiredSuccessful,
    JobPodFailed,
    JobPodMaxParallel,
    JobPodSuccessful,
    JobSuccessfulPods,
    NamespacePhase,
    NodeAllocatableCpu,
    NodeAllocatableEphemeralStorage,
    NodeAllocatableMemory,
    NodeAllocatablePods,
    NodeConditionStatus,
    NodeCpuAllocatable,
    NodeCpuTime,
    NodeCpuUsage,
    NodeEphemeralStorageAllocatable,
    NodeFilesystemAvailable,
    NodeFilesystemCapacity,
    NodeFilesystemUsage,
    NodeMemoryAllocatable,
    NodeMemoryAvailable,
    NodeMemoryPagingFaults,
    NodeMemoryRss,
    NodeMemoryUsage,
    NodeMemoryWorkingSet,
    NodeNetworkErrors,
    NodeNetworkIo,
    NodePodAllocatable,
    NodeUptime,
    PodCpuTime,
    PodCpuUsage,
    PodFilesystemAvailable,
    PodFilesystemCapacity,
    PodFilesystemUsage,
    PodMemoryAvailable,
    PodMemoryPagingFaults,
    PodMemoryRss,
    PodMemoryUsage,
    PodMemoryWorkingSet,
    PodNetworkErrors,
    PodNetworkIo,
    PodStatusPhase,
    PodStatusReason,
    PodUptime,
    PodVolumeAvailable,
    PodVolumeCapacity,
    PodVolumeInodeCount,
    PodVolumeInodeFree,
    PodVolumeInodeUsed,
    PodVolumeUsage,
    ReplicasetAvailablePods,
    ReplicasetDesiredPods,
    ReplicasetPodAvailable,
    ReplicasetPodDesired,
    ReplicationcontrollerAvailablePods,
    ReplicationcontrollerDesiredPods,
    ReplicationcontrollerPodAvailable,
    ReplicationcontrollerPodDesired,
    ResourcequotaCpuLimitHard,
    ResourcequotaCpuLimitUsed,
    ResourcequotaCpuRequestHard,
    ResourcequotaCpuRequestUsed,
    ResourcequotaEphemeralStorageLimitHard,
    ResourcequotaEphemeralStorageLimitUsed,
    ResourcequotaEphemeralStorageRequestHard,
    ResourcequotaEphemeralStorageRequestUsed,
    ResourcequotaHugepageCountRequestHard,
    ResourcequotaHugepageCountRequestUsed,
    ResourcequotaMemoryLimitHard,
    ResourcequotaMemoryLimitUsed,
    ResourcequotaMemoryRequestHard,
    ResourcequotaMemoryRequestUsed,
    ResourcequotaObjectCountHard,
    ResourcequotaObjectCountUsed,
    ResourcequotaPersistentvolumeclaimCountHard,
    ResourcequotaPersistentvolumeclaimCountUsed,
    ResourcequotaStorageRequestHard,
    ResourcequotaStorageRequestUsed,
    StatefulsetCurrentPods,
    StatefulsetDesiredPods,
    StatefulsetPodCurrent,
    StatefulsetPodDesired,
    StatefulsetPodReady,
    StatefulsetPodUpdated,
    StatefulsetReadyPods,
    StatefulsetUpdatedPods,
  )

  /** Maximum CPU resource limit set for the container.
    *
    * @note
    *   <p> See https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#resourcerequirements-v1-core for
    *   details.
    */
  object ContainerCpuLimit extends MetricSpec.Unsealed {

    val name: String = "k8s.container.cpu.limit"
    val description: String = "Maximum CPU resource limit set for the container."
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

  /** The ratio of container CPU usage to its CPU limit.
    *
    * @note
    *   <p> The value range is [0.0,1.0]. A value of 1.0 means the container is using 100% of its CPU limit. If the CPU
    *   limit is not set, this metric SHOULD NOT be emitted for that container.
    */
  object ContainerCpuLimitUtilization extends MetricSpec.Unsealed {

    val name: String = "k8s.container.cpu.limit_utilization"
    val description: String = "The ratio of container CPU usage to its CPU limit."
    val unit: String = "1"
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

  /** CPU resource requested for the container.
    *
    * @note
    *   <p> See https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#resourcerequirements-v1-core for
    *   details.
    */
  object ContainerCpuRequest extends MetricSpec.Unsealed {

    val name: String = "k8s.container.cpu.request"
    val description: String = "CPU resource requested for the container."
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

  /** The ratio of container CPU usage to its CPU request.
    */
  object ContainerCpuRequestUtilization extends MetricSpec.Unsealed {

    val name: String = "k8s.container.cpu.request_utilization"
    val description: String = "The ratio of container CPU usage to its CPU request."
    val unit: String = "1"
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

  /** Maximum ephemeral storage resource limit set for the container.
    *
    * @note
    *   <p> See https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#resourcerequirements-v1-core for
    *   details.
    */
  object ContainerEphemeralStorageLimit extends MetricSpec.Unsealed {

    val name: String = "k8s.container.ephemeral_storage.limit"
    val description: String = "Maximum ephemeral storage resource limit set for the container."
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

  /** Ephemeral storage resource requested for the container.
    *
    * @note
    *   <p> See https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#resourcerequirements-v1-core for
    *   details.
    */
  object ContainerEphemeralStorageRequest extends MetricSpec.Unsealed {

    val name: String = "k8s.container.ephemeral_storage.request"
    val description: String = "Ephemeral storage resource requested for the container."
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

  /** Maximum memory resource limit set for the container.
    *
    * @note
    *   <p> See https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#resourcerequirements-v1-core for
    *   details.
    */
  object ContainerMemoryLimit extends MetricSpec.Unsealed {

    val name: String = "k8s.container.memory.limit"
    val description: String = "Maximum memory resource limit set for the container."
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

  /** Memory resource requested for the container.
    *
    * @note
    *   <p> See https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#resourcerequirements-v1-core for
    *   details.
    */
  object ContainerMemoryRequest extends MetricSpec.Unsealed {

    val name: String = "k8s.container.memory.request"
    val description: String = "Memory resource requested for the container."
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

  /** Indicates whether the container is currently marked as ready to accept traffic, based on its readiness probe (1 =
    * ready, 0 = not ready).
    *
    * @note
    *   <p> This metric SHOULD reflect the value of the `ready` field in the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#containerstatus-v1-core">K8s
    *   ContainerStatus</a>.
    */
  object ContainerReady extends MetricSpec.Unsealed {

    val name: String = "k8s.container.ready"
    val description: String =
      "Indicates whether the container is currently marked as ready to accept traffic, based on its readiness probe (1 = ready, 0 = not ready)."
    val unit: String = "{container}"
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

  /** Describes how many times the container has restarted (since the last counter reset).
    *
    * @note
    *   <p> This value is pulled directly from the K8s API and the value can go indefinitely high and be reset to 0 at
    *   any time depending on how your kubelet is configured to prune dead containers. It is best to not depend too much
    *   on the exact value but rather look at it as either == 0, in which case you can conclude there were no restarts
    *   in the recent past, or > 0, in which case you can conclude there were restarts in the recent past, and not try
    *   and analyze the value beyond that.
    */
  object ContainerRestartCount extends MetricSpec.Unsealed {

    val name: String = "k8s.container.restart.count"
    val description: String = "Describes how many times the container has restarted (since the last counter reset)."
    val unit: String = "{restart}"
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

  /** Describes the number of K8s containers that are currently in a state for a given reason.
    *
    * @note
    *   <p> All possible container state reasons will be reported at each time interval to avoid missing metrics. Only
    *   the value corresponding to the current state reason will be non-zero.
    */
  object ContainerStatusReason extends MetricSpec.Unsealed {

    val name: String = "k8s.container.status.reason"
    val description: String = "Describes the number of K8s containers that are currently in a state for a given reason."
    val unit: String = "{container}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The reason for the container state. Corresponds to the `reason` field of the: <a
        * href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#containerstatewaiting-v1-core">K8s
        * ContainerStateWaiting</a> or <a
        * href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#containerstateterminated-v1-core">K8s
        * ContainerStateTerminated</a>
        */
      val k8sContainerStatusReason: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sContainerStatusReason,
          List(
            "ContainerCreating",
            "CrashLoopBackOff",
            "CreateContainerConfigError",
            "ErrImagePull",
            "ImagePullBackOff",
            "OOMKilled",
            "Completed",
            "Error",
            "ContainerCannotRun",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sContainerStatusReason,
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

  /** Describes the number of K8s containers that are currently in a given state.
    *
    * @note
    *   <p> All possible container states will be reported at each time interval to avoid missing metrics. Only the
    *   value corresponding to the current state will be non-zero.
    */
  object ContainerStatusState extends MetricSpec.Unsealed {

    val name: String = "k8s.container.status.state"
    val description: String = "Describes the number of K8s containers that are currently in a given state."
    val unit: String = "{container}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The state of the container. <a
        * href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#containerstate-v1-core">K8s
        * ContainerState</a>
        */
      val k8sContainerStatusState: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sContainerStatusState,
          List(
            "terminated",
            "running",
            "waiting",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sContainerStatusState,
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

  /** Maximum storage resource limit set for the container.
    *
    * @note
    *   <p> See https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#resourcerequirements-v1-core for
    *   details.
    */
  object ContainerStorageLimit extends MetricSpec.Unsealed {

    val name: String = "k8s.container.storage.limit"
    val description: String = "Maximum storage resource limit set for the container."
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

  /** Storage resource requested for the container.
    *
    * @note
    *   <p> See https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#resourcerequirements-v1-core for
    *   details.
    */
  object ContainerStorageRequest extends MetricSpec.Unsealed {

    val name: String = "k8s.container.storage.request"
    val description: String = "Storage resource requested for the container."
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

  /** Deprecated, use `k8s.cronjob.job.active` instead.
    *
    * @note
    *   <p> This metric aligns with the `active` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#cronjobstatus-v1-batch">K8s
    *   CronJobStatus</a>.
    */
  @deprecated("Replaced by `k8s.cronjob.job.active`.", "")
  object CronjobActiveJobs extends MetricSpec.Unsealed {

    val name: String = "k8s.cronjob.active_jobs"
    val description: String = "Deprecated, use `k8s.cronjob.job.active` instead."
    val unit: String = "{job}"
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

  /** The number of actively running jobs for a cronjob.
    *
    * @note
    *   <p> This metric aligns with the `active` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#cronjobstatus-v1-batch">K8s
    *   CronJobStatus</a>.
    */
  object CronjobJobActive extends MetricSpec.Unsealed {

    val name: String = "k8s.cronjob.job.active"
    val description: String = "The number of actively running jobs for a cronjob."
    val unit: String = "{job}"
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

  /** Deprecated, use `k8s.daemonset.node.current_scheduled` instead.
    *
    * @note
    *   <p> This metric aligns with the `currentNumberScheduled` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#daemonsetstatus-v1-apps">K8s
    *   DaemonSetStatus</a>.
    */
  @deprecated("Replaced by `k8s.daemonset.node.current_scheduled`.", "")
  object DaemonsetCurrentScheduledNodes extends MetricSpec.Unsealed {

    val name: String = "k8s.daemonset.current_scheduled_nodes"
    val description: String = "Deprecated, use `k8s.daemonset.node.current_scheduled` instead."
    val unit: String = "{node}"
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

  /** Deprecated, use `k8s.daemonset.node.desired_scheduled` instead.
    *
    * @note
    *   <p> This metric aligns with the `desiredNumberScheduled` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#daemonsetstatus-v1-apps">K8s
    *   DaemonSetStatus</a>.
    */
  @deprecated("Replaced by `k8s.daemonset.node.desired_scheduled`.", "")
  object DaemonsetDesiredScheduledNodes extends MetricSpec.Unsealed {

    val name: String = "k8s.daemonset.desired_scheduled_nodes"
    val description: String = "Deprecated, use `k8s.daemonset.node.desired_scheduled` instead."
    val unit: String = "{node}"
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

  /** Deprecated, use `k8s.daemonset.node.misscheduled` instead.
    *
    * @note
    *   <p> This metric aligns with the `numberMisscheduled` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#daemonsetstatus-v1-apps">K8s
    *   DaemonSetStatus</a>.
    */
  @deprecated("Replaced by `k8s.daemonset.node.misscheduled`.", "")
  object DaemonsetMisscheduledNodes extends MetricSpec.Unsealed {

    val name: String = "k8s.daemonset.misscheduled_nodes"
    val description: String = "Deprecated, use `k8s.daemonset.node.misscheduled` instead."
    val unit: String = "{node}"
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

  /** Number of nodes that are running at least 1 daemon pod and are supposed to run the daemon pod.
    *
    * @note
    *   <p> This metric aligns with the `currentNumberScheduled` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#daemonsetstatus-v1-apps">K8s
    *   DaemonSetStatus</a>.
    */
  object DaemonsetNodeCurrentScheduled extends MetricSpec.Unsealed {

    val name: String = "k8s.daemonset.node.current_scheduled"
    val description: String =
      "Number of nodes that are running at least 1 daemon pod and are supposed to run the daemon pod."
    val unit: String = "{node}"
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

  /** Number of nodes that should be running the daemon pod (including nodes currently running the daemon pod).
    *
    * @note
    *   <p> This metric aligns with the `desiredNumberScheduled` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#daemonsetstatus-v1-apps">K8s
    *   DaemonSetStatus</a>.
    */
  object DaemonsetNodeDesiredScheduled extends MetricSpec.Unsealed {

    val name: String = "k8s.daemonset.node.desired_scheduled"
    val description: String =
      "Number of nodes that should be running the daemon pod (including nodes currently running the daemon pod)."
    val unit: String = "{node}"
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

  /** Number of nodes that are running the daemon pod, but are not supposed to run the daemon pod.
    *
    * @note
    *   <p> This metric aligns with the `numberMisscheduled` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#daemonsetstatus-v1-apps">K8s
    *   DaemonSetStatus</a>.
    */
  object DaemonsetNodeMisscheduled extends MetricSpec.Unsealed {

    val name: String = "k8s.daemonset.node.misscheduled"
    val description: String =
      "Number of nodes that are running the daemon pod, but are not supposed to run the daemon pod."
    val unit: String = "{node}"
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

  /** Number of nodes that should be running the daemon pod and have one or more of the daemon pod running and ready.
    *
    * @note
    *   <p> This metric aligns with the `numberReady` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#daemonsetstatus-v1-apps">K8s
    *   DaemonSetStatus</a>.
    */
  object DaemonsetNodeReady extends MetricSpec.Unsealed {

    val name: String = "k8s.daemonset.node.ready"
    val description: String =
      "Number of nodes that should be running the daemon pod and have one or more of the daemon pod running and ready."
    val unit: String = "{node}"
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

  /** Deprecated, use `k8s.daemonset.node.ready` instead.
    *
    * @note
    *   <p> This metric aligns with the `numberReady` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#daemonsetstatus-v1-apps">K8s
    *   DaemonSetStatus</a>.
    */
  @deprecated("Replaced by `k8s.daemonset.node.ready`.", "")
  object DaemonsetReadyNodes extends MetricSpec.Unsealed {

    val name: String = "k8s.daemonset.ready_nodes"
    val description: String = "Deprecated, use `k8s.daemonset.node.ready` instead."
    val unit: String = "{node}"
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

  /** Deprecated, use `k8s.deployment.pod.available` instead.
    *
    * @note
    *   <p> This metric aligns with the `availableReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#deploymentstatus-v1-apps">K8s
    *   DeploymentStatus</a>.
    */
  @deprecated("Replaced by `k8s.deployment.pod.available`.", "")
  object DeploymentAvailablePods extends MetricSpec.Unsealed {

    val name: String = "k8s.deployment.available_pods"
    val description: String = "Deprecated, use `k8s.deployment.pod.available` instead."
    val unit: String = "{pod}"
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

  /** Deprecated, use `k8s.deployment.pod.desired` instead.
    *
    * @note
    *   <p> This metric aligns with the `replicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#deploymentspec-v1-apps">K8s
    *   DeploymentSpec</a>.
    */
  @deprecated("Replaced by `k8s.deployment.pod.desired`.", "")
  object DeploymentDesiredPods extends MetricSpec.Unsealed {

    val name: String = "k8s.deployment.desired_pods"
    val description: String = "Deprecated, use `k8s.deployment.pod.desired` instead."
    val unit: String = "{pod}"
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

  /** Total number of available replica pods (ready for at least minReadySeconds) targeted by this deployment.
    *
    * @note
    *   <p> This metric aligns with the `availableReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#deploymentstatus-v1-apps">K8s
    *   DeploymentStatus</a>.
    */
  object DeploymentPodAvailable extends MetricSpec.Unsealed {

    val name: String = "k8s.deployment.pod.available"
    val description: String =
      "Total number of available replica pods (ready for at least minReadySeconds) targeted by this deployment."
    val unit: String = "{pod}"
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

  /** Number of desired replica pods in this deployment.
    *
    * @note
    *   <p> This metric aligns with the `replicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#deploymentspec-v1-apps">K8s
    *   DeploymentSpec</a>.
    */
  object DeploymentPodDesired extends MetricSpec.Unsealed {

    val name: String = "k8s.deployment.pod.desired"
    val description: String = "Number of desired replica pods in this deployment."
    val unit: String = "{pod}"
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

  /** Deprecated, use `k8s.hpa.pod.current` instead.
    *
    * @note
    *   <p> This metric aligns with the `currentReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#horizontalpodautoscalerstatus-v2-autoscaling">K8s
    *   HorizontalPodAutoscalerStatus</a>
    */
  @deprecated("Replaced by `k8s.hpa.pod.current`.", "")
  object HpaCurrentPods extends MetricSpec.Unsealed {

    val name: String = "k8s.hpa.current_pods"
    val description: String = "Deprecated, use `k8s.hpa.pod.current` instead."
    val unit: String = "{pod}"
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

  /** Deprecated, use `k8s.hpa.pod.desired` instead.
    *
    * @note
    *   <p> This metric aligns with the `desiredReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#horizontalpodautoscalerstatus-v2-autoscaling">K8s
    *   HorizontalPodAutoscalerStatus</a>
    */
  @deprecated("Replaced by `k8s.hpa.pod.desired`.", "")
  object HpaDesiredPods extends MetricSpec.Unsealed {

    val name: String = "k8s.hpa.desired_pods"
    val description: String = "Deprecated, use `k8s.hpa.pod.desired` instead."
    val unit: String = "{pod}"
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

  /** Deprecated, use `k8s.hpa.pod.max` instead.
    *
    * @note
    *   <p> This metric aligns with the `maxReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#horizontalpodautoscalerspec-v2-autoscaling">K8s
    *   HorizontalPodAutoscalerSpec</a>
    */
  @deprecated("Replaced by `k8s.hpa.pod.max`.", "")
  object HpaMaxPods extends MetricSpec.Unsealed {

    val name: String = "k8s.hpa.max_pods"
    val description: String = "Deprecated, use `k8s.hpa.pod.max` instead."
    val unit: String = "{pod}"
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

  /** Target average utilization, in percentage, for CPU resource in HPA config.
    *
    * @note
    *   <p> This metric aligns with the `averageUtilization` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#metrictarget-v2-autoscaling">K8s HPA
    *   MetricTarget</a>. If the type of the metric is <a
    *   href="https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/#support-for-metrics-apis">`ContainerResource`</a>,
    *   the `k8s.container.name` attribute MUST be set to identify the specific container within the pod to which the
    *   metric applies.
    */
  object HpaMetricTargetCpuAverageUtilization extends MetricSpec.Unsealed {

    val name: String = "k8s.hpa.metric.target.cpu.average_utilization"
    val description: String = "Target average utilization, in percentage, for CPU resource in HPA config."
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the Container from Pod specification, must be unique within a Pod. Container runtime usually uses
        * different globally unique name (`container.name`).
        */
      val k8sContainerName: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sContainerName,
          List(
            "redis",
          ),
          Requirement.conditionallyRequired("if and only if k8s.hpa.metric.type is ContainerResource."),
          Stability.alpha
        )

      /** The type of metric source for the horizontal pod autoscaler.
        *
        * @note
        *   <p> This attribute reflects the `type` field of spec.metrics[] in the HPA.
        */
      val k8sHpaMetricType: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sHpaMetricType,
          List(
            "Resource",
            "ContainerResource",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sContainerName,
          k8sHpaMetricType,
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

  /** Target average value for CPU resource in HPA config.
    *
    * @note
    *   <p> This metric aligns with the `averageValue` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#metrictarget-v2-autoscaling">K8s HPA
    *   MetricTarget</a>. If the type of the metric is <a
    *   href="https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/#support-for-metrics-apis">`ContainerResource`</a>,
    *   the `k8s.container.name` attribute MUST be set to identify the specific container within the pod to which the
    *   metric applies.
    */
  object HpaMetricTargetCpuAverageValue extends MetricSpec.Unsealed {

    val name: String = "k8s.hpa.metric.target.cpu.average_value"
    val description: String = "Target average value for CPU resource in HPA config."
    val unit: String = "{cpu}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the Container from Pod specification, must be unique within a Pod. Container runtime usually uses
        * different globally unique name (`container.name`).
        */
      val k8sContainerName: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sContainerName,
          List(
            "redis",
          ),
          Requirement.conditionallyRequired("if and only if k8s.hpa.metric.type is ContainerResource"),
          Stability.alpha
        )

      /** The type of metric source for the horizontal pod autoscaler.
        *
        * @note
        *   <p> This attribute reflects the `type` field of spec.metrics[] in the HPA.
        */
      val k8sHpaMetricType: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sHpaMetricType,
          List(
            "Resource",
            "ContainerResource",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sContainerName,
          k8sHpaMetricType,
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

  /** Target value for CPU resource in HPA config.
    *
    * @note
    *   <p> This metric aligns with the `value` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#metrictarget-v2-autoscaling">K8s HPA
    *   MetricTarget</a>. If the type of the metric is <a
    *   href="https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/#support-for-metrics-apis">`ContainerResource`</a>,
    *   the `k8s.container.name` attribute MUST be set to identify the specific container within the pod to which the
    *   metric applies.
    */
  object HpaMetricTargetCpuValue extends MetricSpec.Unsealed {

    val name: String = "k8s.hpa.metric.target.cpu.value"
    val description: String = "Target value for CPU resource in HPA config."
    val unit: String = "{cpu}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the Container from Pod specification, must be unique within a Pod. Container runtime usually uses
        * different globally unique name (`container.name`).
        */
      val k8sContainerName: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sContainerName,
          List(
            "redis",
          ),
          Requirement.conditionallyRequired("if and only if k8s.hpa.metric.type is ContainerResource"),
          Stability.alpha
        )

      /** The type of metric source for the horizontal pod autoscaler.
        *
        * @note
        *   <p> This attribute reflects the `type` field of spec.metrics[] in the HPA.
        */
      val k8sHpaMetricType: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sHpaMetricType,
          List(
            "Resource",
            "ContainerResource",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sContainerName,
          k8sHpaMetricType,
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

  /** Deprecated, use `k8s.hpa.pod.min` instead.
    *
    * @note
    *   <p> This metric aligns with the `minReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#horizontalpodautoscalerspec-v2-autoscaling">K8s
    *   HorizontalPodAutoscalerSpec</a>
    */
  @deprecated("Replaced by `k8s.hpa.pod.min`.", "")
  object HpaMinPods extends MetricSpec.Unsealed {

    val name: String = "k8s.hpa.min_pods"
    val description: String = "Deprecated, use `k8s.hpa.pod.min` instead."
    val unit: String = "{pod}"
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

  /** Current number of replica pods managed by this horizontal pod autoscaler, as last seen by the autoscaler.
    *
    * @note
    *   <p> This metric aligns with the `currentReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#horizontalpodautoscalerstatus-v2-autoscaling">K8s
    *   HorizontalPodAutoscalerStatus</a>
    */
  object HpaPodCurrent extends MetricSpec.Unsealed {

    val name: String = "k8s.hpa.pod.current"
    val description: String =
      "Current number of replica pods managed by this horizontal pod autoscaler, as last seen by the autoscaler."
    val unit: String = "{pod}"
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

  /** Desired number of replica pods managed by this horizontal pod autoscaler, as last calculated by the autoscaler.
    *
    * @note
    *   <p> This metric aligns with the `desiredReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#horizontalpodautoscalerstatus-v2-autoscaling">K8s
    *   HorizontalPodAutoscalerStatus</a>
    */
  object HpaPodDesired extends MetricSpec.Unsealed {

    val name: String = "k8s.hpa.pod.desired"
    val description: String =
      "Desired number of replica pods managed by this horizontal pod autoscaler, as last calculated by the autoscaler."
    val unit: String = "{pod}"
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

  /** The upper limit for the number of replica pods to which the autoscaler can scale up.
    *
    * @note
    *   <p> This metric aligns with the `maxReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#horizontalpodautoscalerspec-v2-autoscaling">K8s
    *   HorizontalPodAutoscalerSpec</a>
    */
  object HpaPodMax extends MetricSpec.Unsealed {

    val name: String = "k8s.hpa.pod.max"
    val description: String = "The upper limit for the number of replica pods to which the autoscaler can scale up."
    val unit: String = "{pod}"
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

  /** The lower limit for the number of replica pods to which the autoscaler can scale down.
    *
    * @note
    *   <p> This metric aligns with the `minReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#horizontalpodautoscalerspec-v2-autoscaling">K8s
    *   HorizontalPodAutoscalerSpec</a>
    */
  object HpaPodMin extends MetricSpec.Unsealed {

    val name: String = "k8s.hpa.pod.min"
    val description: String = "The lower limit for the number of replica pods to which the autoscaler can scale down."
    val unit: String = "{pod}"
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

  /** Deprecated, use `k8s.job.pod.active` instead.
    *
    * @note
    *   <p> This metric aligns with the `active` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#jobstatus-v1-batch">K8s
    *   JobStatus</a>.
    */
  @deprecated("Replaced by `k8s.job.pod.active`.", "")
  object JobActivePods extends MetricSpec.Unsealed {

    val name: String = "k8s.job.active_pods"
    val description: String = "Deprecated, use `k8s.job.pod.active` instead."
    val unit: String = "{pod}"
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

  /** Deprecated, use `k8s.job.pod.desired_successful` instead.
    *
    * @note
    *   <p> This metric aligns with the `completions` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#jobspec-v1-batch">K8s JobSpec</a>..
    */
  @deprecated("Replaced by `k8s.job.pod.desired_successful`.", "")
  object JobDesiredSuccessfulPods extends MetricSpec.Unsealed {

    val name: String = "k8s.job.desired_successful_pods"
    val description: String = "Deprecated, use `k8s.job.pod.desired_successful` instead."
    val unit: String = "{pod}"
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

  /** Deprecated, use `k8s.job.pod.failed` instead.
    *
    * @note
    *   <p> This metric aligns with the `failed` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#jobstatus-v1-batch">K8s
    *   JobStatus</a>.
    */
  @deprecated("Replaced by `k8s.job.pod.failed`.", "")
  object JobFailedPods extends MetricSpec.Unsealed {

    val name: String = "k8s.job.failed_pods"
    val description: String = "Deprecated, use `k8s.job.pod.failed` instead."
    val unit: String = "{pod}"
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

  /** Deprecated, use `k8s.job.pod.max_parallel` instead.
    *
    * @note
    *   <p> This metric aligns with the `parallelism` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#jobspec-v1-batch">K8s JobSpec</a>.
    */
  @deprecated("Replaced by `k8s.job.pod.max_parallel`.", "")
  object JobMaxParallelPods extends MetricSpec.Unsealed {

    val name: String = "k8s.job.max_parallel_pods"
    val description: String = "Deprecated, use `k8s.job.pod.max_parallel` instead."
    val unit: String = "{pod}"
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

  /** The number of pending and actively running pods for a job.
    *
    * @note
    *   <p> This metric aligns with the `active` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#jobstatus-v1-batch">K8s
    *   JobStatus</a>.
    */
  object JobPodActive extends MetricSpec.Unsealed {

    val name: String = "k8s.job.pod.active"
    val description: String = "The number of pending and actively running pods for a job."
    val unit: String = "{pod}"
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

  /** The desired number of successfully finished pods the job should be run with.
    *
    * @note
    *   <p> This metric aligns with the `completions` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#jobspec-v1-batch">K8s JobSpec</a>..
    */
  object JobPodDesiredSuccessful extends MetricSpec.Unsealed {

    val name: String = "k8s.job.pod.desired_successful"
    val description: String = "The desired number of successfully finished pods the job should be run with."
    val unit: String = "{pod}"
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

  /** The number of pods which reached phase Failed for a job.
    *
    * @note
    *   <p> This metric aligns with the `failed` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#jobstatus-v1-batch">K8s
    *   JobStatus</a>.
    */
  object JobPodFailed extends MetricSpec.Unsealed {

    val name: String = "k8s.job.pod.failed"
    val description: String = "The number of pods which reached phase Failed for a job."
    val unit: String = "{pod}"
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

  /** The max desired number of pods the job should run at any given time.
    *
    * @note
    *   <p> This metric aligns with the `parallelism` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#jobspec-v1-batch">K8s JobSpec</a>.
    */
  object JobPodMaxParallel extends MetricSpec.Unsealed {

    val name: String = "k8s.job.pod.max_parallel"
    val description: String = "The max desired number of pods the job should run at any given time."
    val unit: String = "{pod}"
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

  /** The number of pods which reached phase Succeeded for a job.
    *
    * @note
    *   <p> This metric aligns with the `succeeded` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#jobstatus-v1-batch">K8s
    *   JobStatus</a>.
    */
  object JobPodSuccessful extends MetricSpec.Unsealed {

    val name: String = "k8s.job.pod.successful"
    val description: String = "The number of pods which reached phase Succeeded for a job."
    val unit: String = "{pod}"
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

  /** Deprecated, use `k8s.job.pod.successful` instead.
    *
    * @note
    *   <p> This metric aligns with the `succeeded` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#jobstatus-v1-batch">K8s
    *   JobStatus</a>.
    */
  @deprecated("Replaced by `k8s.job.pod.successful`.", "")
  object JobSuccessfulPods extends MetricSpec.Unsealed {

    val name: String = "k8s.job.successful_pods"
    val description: String = "Deprecated, use `k8s.job.pod.successful` instead."
    val unit: String = "{pod}"
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

  /** Describes number of K8s namespaces that are currently in a given phase.
    */
  object NamespacePhase extends MetricSpec.Unsealed {

    val name: String = "k8s.namespace.phase"
    val description: String = "Describes number of K8s namespaces that are currently in a given phase."
    val unit: String = "{namespace}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The phase of the K8s namespace.
        *
        * @note
        *   <p> This attribute aligns with the `phase` field of the <a
        *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#namespacestatus-v1-core">K8s
        *   NamespaceStatus</a>
        */
      val k8sNamespacePhase: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sNamespacePhase,
          List(
            "active",
            "terminating",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sNamespacePhase,
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

  /** Deprecated, use `k8s.node.cpu.allocatable` instead.
    */
  @deprecated("Replaced by `k8s.node.cpu.allocatable`.", "")
  object NodeAllocatableCpu extends MetricSpec.Unsealed {

    val name: String = "k8s.node.allocatable.cpu"
    val description: String = "Deprecated, use `k8s.node.cpu.allocatable` instead."
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

  /** Deprecated, use `k8s.node.ephemeral_storage.allocatable` instead.
    */
  @deprecated("Replaced by `k8s.node.ephemeral_storage.allocatable`.", "")
  object NodeAllocatableEphemeralStorage extends MetricSpec.Unsealed {

    val name: String = "k8s.node.allocatable.ephemeral_storage"
    val description: String = "Deprecated, use `k8s.node.ephemeral_storage.allocatable` instead."
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

  /** Deprecated, use `k8s.node.memory.allocatable` instead.
    */
  @deprecated("Replaced by `k8s.node.memory.allocatable`.", "")
  object NodeAllocatableMemory extends MetricSpec.Unsealed {

    val name: String = "k8s.node.allocatable.memory"
    val description: String = "Deprecated, use `k8s.node.memory.allocatable` instead."
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

  /** Deprecated, use `k8s.node.pod.allocatable` instead.
    */
  @deprecated("Replaced by `k8s.node.pod.allocatable`.", "")
  object NodeAllocatablePods extends MetricSpec.Unsealed {

    val name: String = "k8s.node.allocatable.pods"
    val description: String = "Deprecated, use `k8s.node.pod.allocatable` instead."
    val unit: String = "{pod}"
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

  /** Describes the condition of a particular Node.
    *
    * @note
    *   <p> All possible node condition pairs (type and status) will be reported at each time interval to avoid missing
    *   metrics. Condition pairs corresponding to the current conditions' statuses will be non-zero.
    */
  object NodeConditionStatus extends MetricSpec.Unsealed {

    val name: String = "k8s.node.condition.status"
    val description: String = "Describes the condition of a particular Node."
    val unit: String = "{node}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The status of the condition, one of True, False, Unknown.
        *
        * @note
        *   <p> This attribute aligns with the `status` field of the <a
        *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#nodecondition-v1-core">NodeCondition</a>
        */
      val k8sNodeConditionStatus: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sNodeConditionStatus,
          List(
            "true",
            "false",
            "unknown",
          ),
          Requirement.required,
          Stability.development
        )

      /** The condition type of a K8s Node.
        *
        * @note
        *   <p> K8s Node conditions as described by <a
        *   href="https://v1-32.docs.kubernetes.io/docs/reference/node/node-status/#condition">K8s documentation</a>.
        *   <p> This attribute aligns with the `type` field of the <a
        *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#nodecondition-v1-core">NodeCondition</a>
        *   <p> The set of possible values is not limited to those listed here. Managed Kubernetes environments, or
        *   custom controllers MAY introduce additional node condition types. When this occurs, the exact value as
        *   reported by the Kubernetes API SHOULD be used.
        */
      val k8sNodeConditionType: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sNodeConditionType,
          List(
            "Ready",
            "DiskPressure",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sNodeConditionStatus,
          k8sNodeConditionType,
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

  /** Amount of cpu allocatable on the node.
    */
  object NodeCpuAllocatable extends MetricSpec.Unsealed {

    val name: String = "k8s.node.cpu.allocatable"
    val description: String = "Amount of cpu allocatable on the node."
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

  /** Total CPU time consumed.
    *
    * @note
    *   <p> Total CPU time consumed by the specific Node on all available CPU cores
    */
  object NodeCpuTime extends MetricSpec.Unsealed {

    val name: String = "k8s.node.cpu.time"
    val description: String = "Total CPU time consumed."
    val unit: String = "s"
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

  /** Node's CPU usage, measured in cpus. Range from 0 to the number of allocatable CPUs.
    *
    * @note
    *   <p> CPU usage of the specific Node on all available CPU cores, averaged over the sample window
    */
  object NodeCpuUsage extends MetricSpec.Unsealed {

    val name: String = "k8s.node.cpu.usage"
    val description: String = "Node's CPU usage, measured in cpus. Range from 0 to the number of allocatable CPUs."
    val unit: String = "{cpu}"
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

  /** Amount of ephemeral-storage allocatable on the node.
    */
  object NodeEphemeralStorageAllocatable extends MetricSpec.Unsealed {

    val name: String = "k8s.node.ephemeral_storage.allocatable"
    val description: String = "Amount of ephemeral-storage allocatable on the node."
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

  /** Node filesystem available bytes.
    *
    * @note
    *   <p> This metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#FsStats">FsStats.AvailableBytes</a>
    *   field of the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#NodeStats">NodeStats.Fs</a> of the
    *   Kubelet's stats API.
    */
  object NodeFilesystemAvailable extends MetricSpec.Unsealed {

    val name: String = "k8s.node.filesystem.available"
    val description: String = "Node filesystem available bytes."
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

  /** Node filesystem capacity.
    *
    * @note
    *   <p> This metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#FsStats">FsStats.CapacityBytes</a> field
    *   of the <a href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#NodeStats">NodeStats.Fs</a> of
    *   the Kubelet's stats API.
    */
  object NodeFilesystemCapacity extends MetricSpec.Unsealed {

    val name: String = "k8s.node.filesystem.capacity"
    val description: String = "Node filesystem capacity."
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

  /** Node filesystem usage.
    *
    * @note
    *   <p> This may not equal capacity - available. <p> This metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#FsStats">FsStats.UsedBytes</a> field of
    *   the <a href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#NodeStats">NodeStats.Fs</a> of
    *   the Kubelet's stats API.
    */
  object NodeFilesystemUsage extends MetricSpec.Unsealed {

    val name: String = "k8s.node.filesystem.usage"
    val description: String = "Node filesystem usage."
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

  /** Amount of memory allocatable on the node.
    */
  object NodeMemoryAllocatable extends MetricSpec.Unsealed {

    val name: String = "k8s.node.memory.allocatable"
    val description: String = "Amount of memory allocatable on the node."
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

  /** Node memory available.
    *
    * @note
    *   <p> Available memory for use. This is defined as the memory limit - workingSetBytes. If memory limit is
    *   undefined, the available bytes is omitted. This metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#MemoryStats">MemoryStats.AvailableBytes</a>
    *   field of the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#NodeStats">NodeStats.Memory</a> of the
    *   Kubelet's stats API.
    */
  object NodeMemoryAvailable extends MetricSpec.Unsealed {

    val name: String = "k8s.node.memory.available"
    val description: String = "Node memory available."
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

  /** Node memory paging faults.
    *
    * @note
    *   <p> Cumulative number of major/minor page faults. This metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#MemoryStats">MemoryStats.PageFaults</a>
    *   and <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#MemoryStats">MemoryStats.MajorPageFaults</a>
    *   fields of the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#NodeStats">NodeStats.Memory</a> of the
    *   Kubelet's stats API.
    */
  object NodeMemoryPagingFaults extends MetricSpec.Unsealed {

    val name: String = "k8s.node.memory.paging.faults"
    val description: String = "Node memory paging faults."
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

  /** Node memory RSS.
    *
    * @note
    *   <p> The amount of anonymous and swap cache memory (includes transparent hugepages). This metric is derived from
    *   the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#MemoryStats">MemoryStats.RSSBytes</a>
    *   field of the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#NodeStats">NodeStats.Memory</a> of the
    *   Kubelet's stats API.
    */
  object NodeMemoryRss extends MetricSpec.Unsealed {

    val name: String = "k8s.node.memory.rss"
    val description: String = "Node memory RSS."
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

  /** Memory usage of the Node.
    *
    * @note
    *   <p> Total memory usage of the Node
    */
  object NodeMemoryUsage extends MetricSpec.Unsealed {

    val name: String = "k8s.node.memory.usage"
    val description: String = "Memory usage of the Node."
    val unit: String = "By"
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

  /** Node memory working set.
    *
    * @note
    *   <p> The amount of working set memory. This includes recently accessed memory, dirty memory, and kernel memory.
    *   WorkingSetBytes is <= UsageBytes. This metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#MemoryStats">MemoryStats.WorkingSetBytes</a>
    *   field of the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#NodeStats">NodeStats.Memory</a> of the
    *   Kubelet's stats API.
    */
  object NodeMemoryWorkingSet extends MetricSpec.Unsealed {

    val name: String = "k8s.node.memory.working_set"
    val description: String = "Node memory working set."
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

  /** Node network errors.
    */
  object NodeNetworkErrors extends MetricSpec.Unsealed {

    val name: String = "k8s.node.network.errors"
    val description: String = "Node network errors."
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

  /** Network bytes for the Node.
    */
  object NodeNetworkIo extends MetricSpec.Unsealed {

    val name: String = "k8s.node.network.io"
    val description: String = "Network bytes for the Node."
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

  /** Amount of pods allocatable on the node.
    */
  object NodePodAllocatable extends MetricSpec.Unsealed {

    val name: String = "k8s.node.pod.allocatable"
    val description: String = "Amount of pods allocatable on the node."
    val unit: String = "{pod}"
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

  /** The time the Node has been running.
    *
    * @note
    *   <p> Instrumentations SHOULD use a gauge with type `double` and measure uptime in seconds as a floating point
    *   number with the highest precision available. The actual accuracy would depend on the instrumentation and
    *   operating system.
    */
  object NodeUptime extends MetricSpec.Unsealed {

    val name: String = "k8s.node.uptime"
    val description: String = "The time the Node has been running."
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

  /** Total CPU time consumed.
    *
    * @note
    *   <p> Total CPU time consumed by the specific Pod on all available CPU cores
    */
  object PodCpuTime extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.cpu.time"
    val description: String = "Total CPU time consumed."
    val unit: String = "s"
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

  /** Pod's CPU usage, measured in cpus. Range from 0 to the number of allocatable CPUs.
    *
    * @note
    *   <p> CPU usage of the specific Pod on all available CPU cores, averaged over the sample window
    */
  object PodCpuUsage extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.cpu.usage"
    val description: String = "Pod's CPU usage, measured in cpus. Range from 0 to the number of allocatable CPUs."
    val unit: String = "{cpu}"
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

  /** Pod filesystem available bytes.
    *
    * @note
    *   <p> This metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#FsStats">FsStats.AvailableBytes</a>
    *   field of the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#PodStats">PodStats.EphemeralStorage</a>
    *   of the Kubelet's stats API.
    */
  object PodFilesystemAvailable extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.filesystem.available"
    val description: String = "Pod filesystem available bytes."
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

  /** Pod filesystem capacity.
    *
    * @note
    *   <p> This metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#FsStats">FsStats.CapacityBytes</a> field
    *   of the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#PodStats">PodStats.EphemeralStorage</a>
    *   of the Kubelet's stats API.
    */
  object PodFilesystemCapacity extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.filesystem.capacity"
    val description: String = "Pod filesystem capacity."
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

  /** Pod filesystem usage.
    *
    * @note
    *   <p> This may not equal capacity - available. <p> This metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#FsStats">FsStats.UsedBytes</a> field of
    *   the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#PodStats">PodStats.EphemeralStorage</a>
    *   of the Kubelet's stats API.
    */
  object PodFilesystemUsage extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.filesystem.usage"
    val description: String = "Pod filesystem usage."
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

  /** Pod memory available.
    *
    * @note
    *   <p> Available memory for use. This is defined as the memory limit - workingSetBytes. If memory limit is
    *   undefined, the available bytes is omitted. This metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#MemoryStats">MemoryStats.AvailableBytes</a>
    *   field of the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#PodStats">PodStats.Memory</a> of the
    *   Kubelet's stats API.
    */
  object PodMemoryAvailable extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.memory.available"
    val description: String = "Pod memory available."
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

  /** Pod memory paging faults.
    *
    * @note
    *   <p> Cumulative number of major/minor page faults. This metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#MemoryStats">MemoryStats.PageFaults</a>
    *   and <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#MemoryStats">MemoryStats.MajorPageFaults</a>
    *   field of the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#PodStats">PodStats.Memory</a> of the
    *   Kubelet's stats API.
    */
  object PodMemoryPagingFaults extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.memory.paging.faults"
    val description: String = "Pod memory paging faults."
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

  /** Pod memory RSS.
    *
    * @note
    *   <p> The amount of anonymous and swap cache memory (includes transparent hugepages). This metric is derived from
    *   the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#MemoryStats">MemoryStats.RSSBytes</a>
    *   field of the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#PodStats">PodStats.Memory</a> of the
    *   Kubelet's stats API.
    */
  object PodMemoryRss extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.memory.rss"
    val description: String = "Pod memory RSS."
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

  /** Memory usage of the Pod.
    *
    * @note
    *   <p> Total memory usage of the Pod
    */
  object PodMemoryUsage extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.memory.usage"
    val description: String = "Memory usage of the Pod."
    val unit: String = "By"
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

  /** Pod memory working set.
    *
    * @note
    *   <p> The amount of working set memory. This includes recently accessed memory, dirty memory, and kernel memory.
    *   WorkingSetBytes is <= UsageBytes. This metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#MemoryStats">MemoryStats.WorkingSetBytes</a>
    *   field of the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.34.0/pkg/apis/stats/v1alpha1#PodStats">PodStats.Memory</a> of the
    *   Kubelet's stats API.
    */
  object PodMemoryWorkingSet extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.memory.working_set"
    val description: String = "Pod memory working set."
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

  /** Pod network errors.
    */
  object PodNetworkErrors extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.network.errors"
    val description: String = "Pod network errors."
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

  /** Network bytes for the Pod.
    */
  object PodNetworkIo extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.network.io"
    val description: String = "Network bytes for the Pod."
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

  /** Describes number of K8s Pods that are currently in a given phase.
    *
    * @note
    *   <p> All possible pod phases will be reported at each time interval to avoid missing metrics. Only the value
    *   corresponding to the current phase will be non-zero.
    */
  object PodStatusPhase extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.status.phase"
    val description: String = "Describes number of K8s Pods that are currently in a given phase."
    val unit: String = "{pod}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The phase for the pod. Corresponds to the `phase` field of the: <a
        * href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.33/#podstatus-v1-core">K8s
        * PodStatus</a>
        */
      val k8sPodStatusPhase: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sPodStatusPhase,
          List(
            "Pending",
            "Running",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sPodStatusPhase,
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

  /** Describes the number of K8s Pods that are currently in a state for a given reason.
    *
    * @note
    *   <p> All possible pod status reasons will be reported at each time interval to avoid missing metrics. Only the
    *   value corresponding to the current reason will be non-zero.
    */
  object PodStatusReason extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.status.reason"
    val description: String = "Describes the number of K8s Pods that are currently in a state for a given reason."
    val unit: String = "{pod}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The reason for the pod state. Corresponds to the `reason` field of the: <a
        * href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.33/#podstatus-v1-core">K8s
        * PodStatus</a>
        */
      val k8sPodStatusReason: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sPodStatusReason,
          List(
            "Evicted",
            "NodeAffinity",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sPodStatusReason,
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

  /** The time the Pod has been running.
    *
    * @note
    *   <p> Instrumentations SHOULD use a gauge with type `double` and measure uptime in seconds as a floating point
    *   number with the highest precision available. The actual accuracy would depend on the instrumentation and
    *   operating system.
    */
  object PodUptime extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.uptime"
    val description: String = "The time the Pod has been running."
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

  /** Pod volume storage space available.
    *
    * @note
    *   <p> This metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#VolumeStats">VolumeStats.AvailableBytes</a>
    *   field of the <a href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#PodStats">PodStats</a>
    *   of the Kubelet's stats API.
    */
  object PodVolumeAvailable extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.volume.available"
    val description: String = "Pod volume storage space available."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the K8s volume.
        */
      val k8sVolumeName: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sVolumeName,
          List(
            "volume0",
          ),
          Requirement.required,
          Stability.development
        )

      /** The type of the K8s volume.
        */
      val k8sVolumeType: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sVolumeType,
          List(
            "emptyDir",
            "persistentVolumeClaim",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sVolumeName,
          k8sVolumeType,
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

  /** Pod volume total capacity.
    *
    * @note
    *   <p> This metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#VolumeStats">VolumeStats.CapacityBytes</a>
    *   field of the <a href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#PodStats">PodStats</a>
    *   of the Kubelet's stats API.
    */
  object PodVolumeCapacity extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.volume.capacity"
    val description: String = "Pod volume total capacity."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the K8s volume.
        */
      val k8sVolumeName: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sVolumeName,
          List(
            "volume0",
          ),
          Requirement.required,
          Stability.development
        )

      /** The type of the K8s volume.
        */
      val k8sVolumeType: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sVolumeType,
          List(
            "emptyDir",
            "persistentVolumeClaim",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sVolumeName,
          k8sVolumeType,
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

  /** The total inodes in the filesystem of the Pod's volume.
    *
    * @note
    *   <p> This metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#VolumeStats">VolumeStats.Inodes</a>
    *   field of the <a href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#PodStats">PodStats</a>
    *   of the Kubelet's stats API.
    */
  object PodVolumeInodeCount extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.volume.inode.count"
    val description: String = "The total inodes in the filesystem of the Pod's volume."
    val unit: String = "{inode}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the K8s volume.
        */
      val k8sVolumeName: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sVolumeName,
          List(
            "volume0",
          ),
          Requirement.required,
          Stability.development
        )

      /** The type of the K8s volume.
        */
      val k8sVolumeType: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sVolumeType,
          List(
            "emptyDir",
            "persistentVolumeClaim",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sVolumeName,
          k8sVolumeType,
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

  /** The free inodes in the filesystem of the Pod's volume.
    *
    * @note
    *   <p> This metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#VolumeStats">VolumeStats.InodesFree</a>
    *   field of the <a href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#PodStats">PodStats</a>
    *   of the Kubelet's stats API.
    */
  object PodVolumeInodeFree extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.volume.inode.free"
    val description: String = "The free inodes in the filesystem of the Pod's volume."
    val unit: String = "{inode}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the K8s volume.
        */
      val k8sVolumeName: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sVolumeName,
          List(
            "volume0",
          ),
          Requirement.required,
          Stability.development
        )

      /** The type of the K8s volume.
        */
      val k8sVolumeType: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sVolumeType,
          List(
            "emptyDir",
            "persistentVolumeClaim",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sVolumeName,
          k8sVolumeType,
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

  /** The inodes used by the filesystem of the Pod's volume.
    *
    * @note
    *   <p> This metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#VolumeStats">VolumeStats.InodesUsed</a>
    *   field of the <a href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#PodStats">PodStats</a>
    *   of the Kubelet's stats API. <p> This may not be equal to `inodes - free` because filesystem may share inodes
    *   with other filesystems.
    */
  object PodVolumeInodeUsed extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.volume.inode.used"
    val description: String = "The inodes used by the filesystem of the Pod's volume."
    val unit: String = "{inode}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the K8s volume.
        */
      val k8sVolumeName: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sVolumeName,
          List(
            "volume0",
          ),
          Requirement.required,
          Stability.development
        )

      /** The type of the K8s volume.
        */
      val k8sVolumeType: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sVolumeType,
          List(
            "emptyDir",
            "persistentVolumeClaim",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sVolumeName,
          k8sVolumeType,
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

  /** Pod volume usage.
    *
    * @note
    *   <p> This may not equal capacity - available. <p> This metric is derived from the <a
    *   href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#VolumeStats">VolumeStats.UsedBytes</a>
    *   field of the <a href="https://pkg.go.dev/k8s.io/kubelet@v0.33.0/pkg/apis/stats/v1alpha1#PodStats">PodStats</a>
    *   of the Kubelet's stats API.
    */
  object PodVolumeUsage extends MetricSpec.Unsealed {

    val name: String = "k8s.pod.volume.usage"
    val description: String = "Pod volume usage."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the K8s volume.
        */
      val k8sVolumeName: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sVolumeName,
          List(
            "volume0",
          ),
          Requirement.required,
          Stability.development
        )

      /** The type of the K8s volume.
        */
      val k8sVolumeType: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sVolumeType,
          List(
            "emptyDir",
            "persistentVolumeClaim",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sVolumeName,
          k8sVolumeType,
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

  /** Deprecated, use `k8s.replicaset.pod.available` instead.
    *
    * @note
    *   <p> This metric aligns with the `availableReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#replicasetstatus-v1-apps">K8s
    *   ReplicaSetStatus</a>.
    */
  @deprecated("Replaced by `k8s.replicaset.pod.available`.", "")
  object ReplicasetAvailablePods extends MetricSpec.Unsealed {

    val name: String = "k8s.replicaset.available_pods"
    val description: String = "Deprecated, use `k8s.replicaset.pod.available` instead."
    val unit: String = "{pod}"
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

  /** Deprecated, use `k8s.replicaset.pod.desired` instead.
    *
    * @note
    *   <p> This metric aligns with the `replicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#replicasetspec-v1-apps">K8s
    *   ReplicaSetSpec</a>.
    */
  @deprecated("Replaced by `k8s.replicaset.pod.desired`.", "")
  object ReplicasetDesiredPods extends MetricSpec.Unsealed {

    val name: String = "k8s.replicaset.desired_pods"
    val description: String = "Deprecated, use `k8s.replicaset.pod.desired` instead."
    val unit: String = "{pod}"
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

  /** Total number of available replica pods (ready for at least minReadySeconds) targeted by this replicaset.
    *
    * @note
    *   <p> This metric aligns with the `availableReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#replicasetstatus-v1-apps">K8s
    *   ReplicaSetStatus</a>.
    */
  object ReplicasetPodAvailable extends MetricSpec.Unsealed {

    val name: String = "k8s.replicaset.pod.available"
    val description: String =
      "Total number of available replica pods (ready for at least minReadySeconds) targeted by this replicaset."
    val unit: String = "{pod}"
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

  /** Number of desired replica pods in this replicaset.
    *
    * @note
    *   <p> This metric aligns with the `replicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#replicasetspec-v1-apps">K8s
    *   ReplicaSetSpec</a>.
    */
  object ReplicasetPodDesired extends MetricSpec.Unsealed {

    val name: String = "k8s.replicaset.pod.desired"
    val description: String = "Number of desired replica pods in this replicaset."
    val unit: String = "{pod}"
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

  /** Deprecated, use `k8s.replicationcontroller.pod.available` instead.
    */
  @deprecated("Replaced by `k8s.replicationcontroller.pod.available`.", "")
  object ReplicationcontrollerAvailablePods extends MetricSpec.Unsealed {

    val name: String = "k8s.replicationcontroller.available_pods"
    val description: String = "Deprecated, use `k8s.replicationcontroller.pod.available` instead."
    val unit: String = "{pod}"
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

  /** Deprecated, use `k8s.replicationcontroller.pod.desired` instead.
    */
  @deprecated("Replaced by `k8s.replicationcontroller.pod.desired`.", "")
  object ReplicationcontrollerDesiredPods extends MetricSpec.Unsealed {

    val name: String = "k8s.replicationcontroller.desired_pods"
    val description: String = "Deprecated, use `k8s.replicationcontroller.pod.desired` instead."
    val unit: String = "{pod}"
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

  /** Total number of available replica pods (ready for at least minReadySeconds) targeted by this replication
    * controller.
    *
    * @note
    *   <p> This metric aligns with the `availableReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#replicationcontrollerstatus-v1-core">K8s
    *   ReplicationControllerStatus</a>
    */
  object ReplicationcontrollerPodAvailable extends MetricSpec.Unsealed {

    val name: String = "k8s.replicationcontroller.pod.available"
    val description: String =
      "Total number of available replica pods (ready for at least minReadySeconds) targeted by this replication controller."
    val unit: String = "{pod}"
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

  /** Number of desired replica pods in this replication controller.
    *
    * @note
    *   <p> This metric aligns with the `replicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#replicationcontrollerspec-v1-core">K8s
    *   ReplicationControllerSpec</a>
    */
  object ReplicationcontrollerPodDesired extends MetricSpec.Unsealed {

    val name: String = "k8s.replicationcontroller.pod.desired"
    val description: String = "Number of desired replica pods in this replication controller."
    val unit: String = "{pod}"
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

  /** The CPU limits in a specific namespace. The value represents the configured quota limit of the resource in the
    * namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>.
    */
  object ResourcequotaCpuLimitHard extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.cpu.limit.hard"
    val description: String =
      "The CPU limits in a specific namespace.The value represents the configured quota limit of the resource in the namespace."
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

  /** The CPU limits in a specific namespace. The value represents the current observed total usage of the resource in
    * the namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>.
    */
  object ResourcequotaCpuLimitUsed extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.cpu.limit.used"
    val description: String =
      "The CPU limits in a specific namespace.The value represents the current observed total usage of the resource in the namespace."
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

  /** The CPU requests in a specific namespace. The value represents the configured quota limit of the resource in the
    * namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>.
    */
  object ResourcequotaCpuRequestHard extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.cpu.request.hard"
    val description: String =
      "The CPU requests in a specific namespace.The value represents the configured quota limit of the resource in the namespace."
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

  /** The CPU requests in a specific namespace. The value represents the current observed total usage of the resource in
    * the namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>.
    */
  object ResourcequotaCpuRequestUsed extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.cpu.request.used"
    val description: String =
      "The CPU requests in a specific namespace.The value represents the current observed total usage of the resource in the namespace."
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

  /** The sum of local ephemeral storage limits in the namespace. The value represents the configured quota limit of the
    * resource in the namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>.
    */
  object ResourcequotaEphemeralStorageLimitHard extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.ephemeral_storage.limit.hard"
    val description: String =
      "The sum of local ephemeral storage limits in the namespace.The value represents the configured quota limit of the resource in the namespace."
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

  /** The sum of local ephemeral storage limits in the namespace. The value represents the current observed total usage
    * of the resource in the namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>.
    */
  object ResourcequotaEphemeralStorageLimitUsed extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.ephemeral_storage.limit.used"
    val description: String =
      "The sum of local ephemeral storage limits in the namespace.The value represents the current observed total usage of the resource in the namespace."
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

  /** The sum of local ephemeral storage requests in the namespace. The value represents the configured quota limit of
    * the resource in the namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>.
    */
  object ResourcequotaEphemeralStorageRequestHard extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.ephemeral_storage.request.hard"
    val description: String =
      "The sum of local ephemeral storage requests in the namespace.The value represents the configured quota limit of the resource in the namespace."
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

  /** The sum of local ephemeral storage requests in the namespace. The value represents the current observed total
    * usage of the resource in the namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>.
    */
  object ResourcequotaEphemeralStorageRequestUsed extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.ephemeral_storage.request.used"
    val description: String =
      "The sum of local ephemeral storage requests in the namespace.The value represents the current observed total usage of the resource in the namespace."
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

  /** The huge page requests in a specific namespace. The value represents the configured quota limit of the resource in
    * the namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>.
    */
  object ResourcequotaHugepageCountRequestHard extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.hugepage_count.request.hard"
    val description: String =
      "The huge page requests in a specific namespace.The value represents the configured quota limit of the resource in the namespace."
    val unit: String = "{hugepage}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The size (identifier) of the K8s huge page.
        */
      val k8sHugepageSize: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sHugepageSize,
          List(
            "2Mi",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sHugepageSize,
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

  /** The huge page requests in a specific namespace. The value represents the current observed total usage of the
    * resource in the namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>.
    */
  object ResourcequotaHugepageCountRequestUsed extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.hugepage_count.request.used"
    val description: String =
      "The huge page requests in a specific namespace.The value represents the current observed total usage of the resource in the namespace."
    val unit: String = "{hugepage}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The size (identifier) of the K8s huge page.
        */
      val k8sHugepageSize: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sHugepageSize,
          List(
            "2Mi",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sHugepageSize,
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

  /** The memory limits in a specific namespace. The value represents the configured quota limit of the resource in the
    * namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>.
    */
  object ResourcequotaMemoryLimitHard extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.memory.limit.hard"
    val description: String =
      "The memory limits in a specific namespace.The value represents the configured quota limit of the resource in the namespace."
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

  /** The memory limits in a specific namespace. The value represents the current observed total usage of the resource
    * in the namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>.
    */
  object ResourcequotaMemoryLimitUsed extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.memory.limit.used"
    val description: String =
      "The memory limits in a specific namespace.The value represents the current observed total usage of the resource in the namespace."
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

  /** The memory requests in a specific namespace. The value represents the configured quota limit of the resource in
    * the namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>.
    */
  object ResourcequotaMemoryRequestHard extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.memory.request.hard"
    val description: String =
      "The memory requests in a specific namespace.The value represents the configured quota limit of the resource in the namespace."
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

  /** The memory requests in a specific namespace. The value represents the current observed total usage of the resource
    * in the namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>.
    */
  object ResourcequotaMemoryRequestUsed extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.memory.request.used"
    val description: String =
      "The memory requests in a specific namespace.The value represents the current observed total usage of the resource in the namespace."
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

  /** The object count limits in a specific namespace. The value represents the configured quota limit of the resource
    * in the namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>.
    */
  object ResourcequotaObjectCountHard extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.object_count.hard"
    val description: String =
      "The object count limits in a specific namespace.The value represents the configured quota limit of the resource in the namespace."
    val unit: String = "{object}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the K8s resource a resource quota defines.
        *
        * @note
        *   <p> The value for this attribute can be either the full `count/<resource>[.<group>]` string (e.g.,
        *   count/deployments.apps, count/pods), or, for certain core Kubernetes resources, just the resource name
        *   (e.g., pods, services, configmaps). Both forms are supported by Kubernetes for object count quotas. See <a
        *   href="https://kubernetes.io/docs/concepts/policy/resource-quotas/#quota-on-object-count">Kubernetes Resource
        *   Quotas documentation</a> for more details.
        */
      val k8sResourcequotaResourceName: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sResourcequotaResourceName,
          List(
            "count/replicationcontrollers",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sResourcequotaResourceName,
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

  /** The object count limits in a specific namespace. The value represents the current observed total usage of the
    * resource in the namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>.
    */
  object ResourcequotaObjectCountUsed extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.object_count.used"
    val description: String =
      "The object count limits in a specific namespace.The value represents the current observed total usage of the resource in the namespace."
    val unit: String = "{object}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the K8s resource a resource quota defines.
        *
        * @note
        *   <p> The value for this attribute can be either the full `count/<resource>[.<group>]` string (e.g.,
        *   count/deployments.apps, count/pods), or, for certain core Kubernetes resources, just the resource name
        *   (e.g., pods, services, configmaps). Both forms are supported by Kubernetes for object count quotas. See <a
        *   href="https://kubernetes.io/docs/concepts/policy/resource-quotas/#quota-on-object-count">Kubernetes Resource
        *   Quotas documentation</a> for more details.
        */
      val k8sResourcequotaResourceName: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sResourcequotaResourceName,
          List(
            "count/replicationcontrollers",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sResourcequotaResourceName,
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

  /** The total number of PersistentVolumeClaims that can exist in the namespace. The value represents the configured
    * quota limit of the resource in the namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>. <p> The `k8s.storageclass.name` should be required when a resource quota is defined for
    *   a specific storage class.
    */
  object ResourcequotaPersistentvolumeclaimCountHard extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.persistentvolumeclaim_count.hard"
    val description: String =
      "The total number of PersistentVolumeClaims that can exist in the namespace.The value represents the configured quota limit of the resource in the namespace."
    val unit: String = "{persistentvolumeclaim}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of K8s <a
        * href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#storageclass-v1-storage-k8s-io">StorageClass</a>
        * object.
        */
      val k8sStorageclassName: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sStorageclassName,
          List(
            "gold.storageclass.storage.k8s.io",
          ),
          Requirement.conditionallyRequired(
            "The `k8s.storageclass.name` should be required when a resource quota is defined for a specificstorage class."
          ),
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sStorageclassName,
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

  /** The total number of PersistentVolumeClaims that can exist in the namespace. The value represents the current
    * observed total usage of the resource in the namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>. <p> The `k8s.storageclass.name` should be required when a resource quota is defined for
    *   a specific storage class.
    */
  object ResourcequotaPersistentvolumeclaimCountUsed extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.persistentvolumeclaim_count.used"
    val description: String =
      "The total number of PersistentVolumeClaims that can exist in the namespace.The value represents the current observed total usage of the resource in the namespace."
    val unit: String = "{persistentvolumeclaim}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of K8s <a
        * href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#storageclass-v1-storage-k8s-io">StorageClass</a>
        * object.
        */
      val k8sStorageclassName: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sStorageclassName,
          List(
            "gold.storageclass.storage.k8s.io",
          ),
          Requirement.conditionallyRequired(
            "The `k8s.storageclass.name` should be required when a resource quota is defined for a specificstorage class."
          ),
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sStorageclassName,
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

  /** The storage requests in a specific namespace. The value represents the configured quota limit of the resource in
    * the namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>. <p> The `k8s.storageclass.name` should be required when a resource quota is defined for
    *   a specific storage class.
    */
  object ResourcequotaStorageRequestHard extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.storage.request.hard"
    val description: String =
      "The storage requests in a specific namespace.The value represents the configured quota limit of the resource in the namespace."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of K8s <a
        * href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#storageclass-v1-storage-k8s-io">StorageClass</a>
        * object.
        */
      val k8sStorageclassName: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sStorageclassName,
          List(
            "gold.storageclass.storage.k8s.io",
          ),
          Requirement.conditionallyRequired(
            "The `k8s.storageclass.name` should be required when a resource quota is defined for a specificstorage class."
          ),
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sStorageclassName,
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

  /** The storage requests in a specific namespace. The value represents the current observed total usage of the
    * resource in the namespace.
    *
    * @note
    *   <p> This metric is retrieved from the `used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a>. <p> The `k8s.storageclass.name` should be required when a resource quota is defined for
    *   a specific storage class.
    */
  object ResourcequotaStorageRequestUsed extends MetricSpec.Unsealed {

    val name: String = "k8s.resourcequota.storage.request.used"
    val description: String =
      "The storage requests in a specific namespace.The value represents the current observed total usage of the resource in the namespace."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of K8s <a
        * href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#storageclass-v1-storage-k8s-io">StorageClass</a>
        * object.
        */
      val k8sStorageclassName: AttributeSpec[String] =
        AttributeSpec(
          K8sExperimentalAttributes.K8sStorageclassName,
          List(
            "gold.storageclass.storage.k8s.io",
          ),
          Requirement.conditionallyRequired(
            "The `k8s.storageclass.name` should be required when a resource quota is defined for a specificstorage class."
          ),
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          k8sStorageclassName,
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

  /** Deprecated, use `k8s.statefulset.pod.current` instead.
    *
    * @note
    *   <p> This metric aligns with the `currentReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#statefulsetstatus-v1-apps">K8s
    *   StatefulSetStatus</a>.
    */
  @deprecated("Replaced by `k8s.statefulset.pod.current`.", "")
  object StatefulsetCurrentPods extends MetricSpec.Unsealed {

    val name: String = "k8s.statefulset.current_pods"
    val description: String = "Deprecated, use `k8s.statefulset.pod.current` instead."
    val unit: String = "{pod}"
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

  /** Deprecated, use `k8s.statefulset.pod.desired` instead.
    *
    * @note
    *   <p> This metric aligns with the `replicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#statefulsetspec-v1-apps">K8s
    *   StatefulSetSpec</a>.
    */
  @deprecated("Replaced by `k8s.statefulset.pod.desired`.", "")
  object StatefulsetDesiredPods extends MetricSpec.Unsealed {

    val name: String = "k8s.statefulset.desired_pods"
    val description: String = "Deprecated, use `k8s.statefulset.pod.desired` instead."
    val unit: String = "{pod}"
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

  /** The number of replica pods created by the statefulset controller from the statefulset version indicated by
    * currentRevision.
    *
    * @note
    *   <p> This metric aligns with the `currentReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#statefulsetstatus-v1-apps">K8s
    *   StatefulSetStatus</a>.
    */
  object StatefulsetPodCurrent extends MetricSpec.Unsealed {

    val name: String = "k8s.statefulset.pod.current"
    val description: String =
      "The number of replica pods created by the statefulset controller from the statefulset version indicated by currentRevision."
    val unit: String = "{pod}"
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

  /** Number of desired replica pods in this statefulset.
    *
    * @note
    *   <p> This metric aligns with the `replicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#statefulsetspec-v1-apps">K8s
    *   StatefulSetSpec</a>.
    */
  object StatefulsetPodDesired extends MetricSpec.Unsealed {

    val name: String = "k8s.statefulset.pod.desired"
    val description: String = "Number of desired replica pods in this statefulset."
    val unit: String = "{pod}"
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

  /** The number of replica pods created for this statefulset with a Ready Condition.
    *
    * @note
    *   <p> This metric aligns with the `readyReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#statefulsetstatus-v1-apps">K8s
    *   StatefulSetStatus</a>.
    */
  object StatefulsetPodReady extends MetricSpec.Unsealed {

    val name: String = "k8s.statefulset.pod.ready"
    val description: String = "The number of replica pods created for this statefulset with a Ready Condition."
    val unit: String = "{pod}"
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

  /** Number of replica pods created by the statefulset controller from the statefulset version indicated by
    * updateRevision.
    *
    * @note
    *   <p> This metric aligns with the `updatedReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#statefulsetstatus-v1-apps">K8s
    *   StatefulSetStatus</a>.
    */
  object StatefulsetPodUpdated extends MetricSpec.Unsealed {

    val name: String = "k8s.statefulset.pod.updated"
    val description: String =
      "Number of replica pods created by the statefulset controller from the statefulset version indicated by updateRevision."
    val unit: String = "{pod}"
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

  /** Deprecated, use `k8s.statefulset.pod.ready` instead.
    *
    * @note
    *   <p> This metric aligns with the `readyReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#statefulsetstatus-v1-apps">K8s
    *   StatefulSetStatus</a>.
    */
  @deprecated("Replaced by `k8s.statefulset.pod.ready`.", "")
  object StatefulsetReadyPods extends MetricSpec.Unsealed {

    val name: String = "k8s.statefulset.ready_pods"
    val description: String = "Deprecated, use `k8s.statefulset.pod.ready` instead."
    val unit: String = "{pod}"
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

  /** Deprecated, use `k8s.statefulset.pod.updated` instead.
    *
    * @note
    *   <p> This metric aligns with the `updatedReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#statefulsetstatus-v1-apps">K8s
    *   StatefulSetStatus</a>.
    */
  @deprecated("Replaced by `k8s.statefulset.pod.updated`.", "")
  object StatefulsetUpdatedPods extends MetricSpec.Unsealed {

    val name: String = "k8s.statefulset.updated_pods"
    val description: String = "Deprecated, use `k8s.statefulset.pod.updated` instead."
    val unit: String = "{pod}"
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

}
