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

  val specs: List[MetricSpec] = List(
    CronjobActiveJobs,
    DaemonsetCurrentScheduledNodes,
    DaemonsetDesiredScheduledNodes,
    DaemonsetMisscheduledNodes,
    DaemonsetReadyNodes,
    DeploymentAvailablePods,
    DeploymentDesiredPods,
    HpaCurrentPods,
    HpaDesiredPods,
    HpaMaxPods,
    HpaMinPods,
    JobActivePods,
    JobDesiredSuccessfulPods,
    JobFailedPods,
    JobMaxParallelPods,
    JobSuccessfulPods,
    NamespacePhase,
    NodeCpuTime,
    NodeCpuUsage,
    NodeMemoryUsage,
    NodeNetworkErrors,
    NodeNetworkIo,
    NodeUptime,
    PodCpuTime,
    PodCpuUsage,
    PodMemoryUsage,
    PodNetworkErrors,
    PodNetworkIo,
    PodUptime,
    ReplicasetAvailablePods,
    ReplicasetDesiredPods,
    ReplicationControllerAvailablePods,
    ReplicationControllerDesiredPods,
    StatefulsetCurrentPods,
    StatefulsetDesiredPods,
    StatefulsetReadyPods,
    StatefulsetUpdatedPods,
  )

  /** The number of actively running jobs for a cronjob
    *
    * @note
    *   <p> This metric aligns with the `active` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#cronjobstatus-v1-batch">K8s
    *   CronJobStatus</a>. <p> This metric SHOULD, at a minimum, be reported against a <a
    *   href="../resource/k8s.md#cronjob">`k8s.cronjob`</a> resource.
    */
  object CronjobActiveJobs extends MetricSpec {

    val name: String = "k8s.cronjob.active_jobs"
    val description: String = "The number of actively running jobs for a cronjob"
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

  /** Number of nodes that are running at least 1 daemon pod and are supposed to run the daemon pod
    *
    * @note
    *   <p> This metric aligns with the `currentNumberScheduled` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#daemonsetstatus-v1-apps">K8s
    *   DaemonSetStatus</a>. <p> This metric SHOULD, at a minimum, be reported against a <a
    *   href="../resource/k8s.md#daemonset">`k8s.daemonset`</a> resource.
    */
  object DaemonsetCurrentScheduledNodes extends MetricSpec {

    val name: String = "k8s.daemonset.current_scheduled_nodes"
    val description: String =
      "Number of nodes that are running at least 1 daemon pod and are supposed to run the daemon pod"
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

  /** Number of nodes that should be running the daemon pod (including nodes currently running the daemon pod)
    *
    * @note
    *   <p> This metric aligns with the `desiredNumberScheduled` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#daemonsetstatus-v1-apps">K8s
    *   DaemonSetStatus</a>. <p> This metric SHOULD, at a minimum, be reported against a <a
    *   href="../resource/k8s.md#daemonset">`k8s.daemonset`</a> resource.
    */
  object DaemonsetDesiredScheduledNodes extends MetricSpec {

    val name: String = "k8s.daemonset.desired_scheduled_nodes"
    val description: String =
      "Number of nodes that should be running the daemon pod (including nodes currently running the daemon pod)"
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

  /** Number of nodes that are running the daemon pod, but are not supposed to run the daemon pod
    *
    * @note
    *   <p> This metric aligns with the `numberMisscheduled` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#daemonsetstatus-v1-apps">K8s
    *   DaemonSetStatus</a>. <p> This metric SHOULD, at a minimum, be reported against a <a
    *   href="../resource/k8s.md#daemonset">`k8s.daemonset`</a> resource.
    */
  object DaemonsetMisscheduledNodes extends MetricSpec {

    val name: String = "k8s.daemonset.misscheduled_nodes"
    val description: String =
      "Number of nodes that are running the daemon pod, but are not supposed to run the daemon pod"
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

  /** Number of nodes that should be running the daemon pod and have one or more of the daemon pod running and ready
    *
    * @note
    *   <p> This metric aligns with the `numberReady` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#daemonsetstatus-v1-apps">K8s
    *   DaemonSetStatus</a>. <p> This metric SHOULD, at a minimum, be reported against a <a
    *   href="../resource/k8s.md#daemonset">`k8s.daemonset`</a> resource.
    */
  object DaemonsetReadyNodes extends MetricSpec {

    val name: String = "k8s.daemonset.ready_nodes"
    val description: String =
      "Number of nodes that should be running the daemon pod and have one or more of the daemon pod running and ready"
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

  /** Total number of available replica pods (ready for at least minReadySeconds) targeted by this deployment
    *
    * @note
    *   <p> This metric aligns with the `availableReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#deploymentstatus-v1-apps">K8s
    *   DeploymentStatus</a>. <p> This metric SHOULD, at a minimum, be reported against a <a
    *   href="../resource/k8s.md#deployment">`k8s.deployment`</a> resource.
    */
  object DeploymentAvailablePods extends MetricSpec {

    val name: String = "k8s.deployment.available_pods"
    val description: String =
      "Total number of available replica pods (ready for at least minReadySeconds) targeted by this deployment"
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

  /** Number of desired replica pods in this deployment
    *
    * @note
    *   <p> This metric aligns with the `replicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#deploymentspec-v1-apps">K8s
    *   DeploymentSpec</a>. <p> This metric SHOULD, at a minimum, be reported against a <a
    *   href="../resource/k8s.md#deployment">`k8s.deployment`</a> resource.
    */
  object DeploymentDesiredPods extends MetricSpec {

    val name: String = "k8s.deployment.desired_pods"
    val description: String = "Number of desired replica pods in this deployment"
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

  /** Current number of replica pods managed by this horizontal pod autoscaler, as last seen by the autoscaler
    *
    * @note
    *   <p> This metric aligns with the `currentReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#horizontalpodautoscalerstatus-v2-autoscaling">K8s
    *   HorizontalPodAutoscalerStatus</a>
    */
  object HpaCurrentPods extends MetricSpec {

    val name: String = "k8s.hpa.current_pods"
    val description: String =
      "Current number of replica pods managed by this horizontal pod autoscaler, as last seen by the autoscaler"
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

  /** Desired number of replica pods managed by this horizontal pod autoscaler, as last calculated by the autoscaler
    *
    * @note
    *   <p> This metric aligns with the `desiredReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#horizontalpodautoscalerstatus-v2-autoscaling">K8s
    *   HorizontalPodAutoscalerStatus</a>
    */
  object HpaDesiredPods extends MetricSpec {

    val name: String = "k8s.hpa.desired_pods"
    val description: String =
      "Desired number of replica pods managed by this horizontal pod autoscaler, as last calculated by the autoscaler"
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

  /** The upper limit for the number of replica pods to which the autoscaler can scale up
    *
    * @note
    *   <p> This metric aligns with the `maxReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#horizontalpodautoscalerspec-v2-autoscaling">K8s
    *   HorizontalPodAutoscalerSpec</a>
    */
  object HpaMaxPods extends MetricSpec {

    val name: String = "k8s.hpa.max_pods"
    val description: String = "The upper limit for the number of replica pods to which the autoscaler can scale up"
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

  /** The lower limit for the number of replica pods to which the autoscaler can scale down
    *
    * @note
    *   <p> This metric aligns with the `minReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#horizontalpodautoscalerspec-v2-autoscaling">K8s
    *   HorizontalPodAutoscalerSpec</a>
    */
  object HpaMinPods extends MetricSpec {

    val name: String = "k8s.hpa.min_pods"
    val description: String = "The lower limit for the number of replica pods to which the autoscaler can scale down"
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

  /** The number of pending and actively running pods for a job
    *
    * @note
    *   <p> This metric aligns with the `active` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#jobstatus-v1-batch">K8s
    *   JobStatus</a>. <p> This metric SHOULD, at a minimum, be reported against a <a
    *   href="../resource/k8s.md#job">`k8s.job`</a> resource.
    */
  object JobActivePods extends MetricSpec {

    val name: String = "k8s.job.active_pods"
    val description: String = "The number of pending and actively running pods for a job"
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

  /** The desired number of successfully finished pods the job should be run with
    *
    * @note
    *   <p> This metric aligns with the `completions` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#jobspec-v1-batch">K8s JobSpec</a>.
    *   <p> This metric SHOULD, at a minimum, be reported against a <a href="../resource/k8s.md#job">`k8s.job`</a>
    *   resource.
    */
  object JobDesiredSuccessfulPods extends MetricSpec {

    val name: String = "k8s.job.desired_successful_pods"
    val description: String = "The desired number of successfully finished pods the job should be run with"
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

  /** The number of pods which reached phase Failed for a job
    *
    * @note
    *   <p> This metric aligns with the `failed` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#jobstatus-v1-batch">K8s
    *   JobStatus</a>. <p> This metric SHOULD, at a minimum, be reported against a <a
    *   href="../resource/k8s.md#job">`k8s.job`</a> resource.
    */
  object JobFailedPods extends MetricSpec {

    val name: String = "k8s.job.failed_pods"
    val description: String = "The number of pods which reached phase Failed for a job"
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

  /** The max desired number of pods the job should run at any given time
    *
    * @note
    *   <p> This metric aligns with the `parallelism` field of the [K8s
    *   JobSpec](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#jobspec-v1-batch. <p> This metric
    *   SHOULD, at a minimum, be reported against a <a href="../resource/k8s.md#job">`k8s.job`</a> resource.
    */
  object JobMaxParallelPods extends MetricSpec {

    val name: String = "k8s.job.max_parallel_pods"
    val description: String = "The max desired number of pods the job should run at any given time"
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

  /** The number of pods which reached phase Succeeded for a job
    *
    * @note
    *   <p> This metric aligns with the `succeeded` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#jobstatus-v1-batch">K8s
    *   JobStatus</a>. <p> This metric SHOULD, at a minimum, be reported against a <a
    *   href="../resource/k8s.md#job">`k8s.job`</a> resource.
    */
  object JobSuccessfulPods extends MetricSpec {

    val name: String = "k8s.job.successful_pods"
    val description: String = "The number of pods which reached phase Succeeded for a job"
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
    *
    * @note
    *   <p> This metric SHOULD, at a minimum, be reported against a <a
    *   href="../resource/k8s.md#namespace">`k8s.namespace`</a> resource.
    */
  object NamespacePhase extends MetricSpec {

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

  /** Total CPU time consumed
    *
    * @note
    *   <p> Total CPU time consumed by the specific Node on all available CPU cores
    */
  object NodeCpuTime extends MetricSpec {

    val name: String = "k8s.node.cpu.time"
    val description: String = "Total CPU time consumed"
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

  /** Node's CPU usage, measured in cpus. Range from 0 to the number of allocatable CPUs
    *
    * @note
    *   <p> CPU usage of the specific Node on all available CPU cores, averaged over the sample window
    */
  object NodeCpuUsage extends MetricSpec {

    val name: String = "k8s.node.cpu.usage"
    val description: String = "Node's CPU usage, measured in cpus. Range from 0 to the number of allocatable CPUs"
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

  /** Memory usage of the Node
    *
    * @note
    *   <p> Total memory usage of the Node
    */
  object NodeMemoryUsage extends MetricSpec {

    val name: String = "k8s.node.memory.usage"
    val description: String = "Memory usage of the Node"
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

  /** Node network errors
    */
  object NodeNetworkErrors extends MetricSpec {

    val name: String = "k8s.node.network.errors"
    val description: String = "Node network errors"
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

  /** Network bytes for the Node
    */
  object NodeNetworkIo extends MetricSpec {

    val name: String = "k8s.node.network.io"
    val description: String = "Network bytes for the Node"
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

  /** The time the Node has been running
    *
    * @note
    *   <p> Instrumentations SHOULD use a gauge with type `double` and measure uptime in seconds as a floating point
    *   number with the highest precision available. The actual accuracy would depend on the instrumentation and
    *   operating system.
    */
  object NodeUptime extends MetricSpec {

    val name: String = "k8s.node.uptime"
    val description: String = "The time the Node has been running"
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

  /** Total CPU time consumed
    *
    * @note
    *   <p> Total CPU time consumed by the specific Pod on all available CPU cores
    */
  object PodCpuTime extends MetricSpec {

    val name: String = "k8s.pod.cpu.time"
    val description: String = "Total CPU time consumed"
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

  /** Pod's CPU usage, measured in cpus. Range from 0 to the number of allocatable CPUs
    *
    * @note
    *   <p> CPU usage of the specific Pod on all available CPU cores, averaged over the sample window
    */
  object PodCpuUsage extends MetricSpec {

    val name: String = "k8s.pod.cpu.usage"
    val description: String = "Pod's CPU usage, measured in cpus. Range from 0 to the number of allocatable CPUs"
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

  /** Memory usage of the Pod
    *
    * @note
    *   <p> Total memory usage of the Pod
    */
  object PodMemoryUsage extends MetricSpec {

    val name: String = "k8s.pod.memory.usage"
    val description: String = "Memory usage of the Pod"
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

  /** Pod network errors
    */
  object PodNetworkErrors extends MetricSpec {

    val name: String = "k8s.pod.network.errors"
    val description: String = "Pod network errors"
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

  /** Network bytes for the Pod
    */
  object PodNetworkIo extends MetricSpec {

    val name: String = "k8s.pod.network.io"
    val description: String = "Network bytes for the Pod"
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

  /** The time the Pod has been running
    *
    * @note
    *   <p> Instrumentations SHOULD use a gauge with type `double` and measure uptime in seconds as a floating point
    *   number with the highest precision available. The actual accuracy would depend on the instrumentation and
    *   operating system.
    */
  object PodUptime extends MetricSpec {

    val name: String = "k8s.pod.uptime"
    val description: String = "The time the Pod has been running"
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

  /** Total number of available replica pods (ready for at least minReadySeconds) targeted by this replicaset
    *
    * @note
    *   <p> This metric aligns with the `availableReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#replicasetstatus-v1-apps">K8s
    *   ReplicaSetStatus</a>. <p> This metric SHOULD, at a minimum, be reported against a <a
    *   href="../resource/k8s.md#replicaset">`k8s.replicaset`</a> resource.
    */
  object ReplicasetAvailablePods extends MetricSpec {

    val name: String = "k8s.replicaset.available_pods"
    val description: String =
      "Total number of available replica pods (ready for at least minReadySeconds) targeted by this replicaset"
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

  /** Number of desired replica pods in this replicaset
    *
    * @note
    *   <p> This metric aligns with the `replicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#replicasetspec-v1-apps">K8s
    *   ReplicaSetSpec</a>. <p> This metric SHOULD, at a minimum, be reported against a <a
    *   href="../resource/k8s.md#replicaset">`k8s.replicaset`</a> resource.
    */
  object ReplicasetDesiredPods extends MetricSpec {

    val name: String = "k8s.replicaset.desired_pods"
    val description: String = "Number of desired replica pods in this replicaset"
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
    * controller
    *
    * @note
    *   <p> This metric aligns with the `availableReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#replicationcontrollerstatus-v1-core">K8s
    *   ReplicationControllerStatus</a>
    */
  object ReplicationControllerAvailablePods extends MetricSpec {

    val name: String = "k8s.replication_controller.available_pods"
    val description: String =
      "Total number of available replica pods (ready for at least minReadySeconds) targeted by this replication controller"
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

  /** Number of desired replica pods in this replication controller
    *
    * @note
    *   <p> This metric aligns with the `replicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#replicationcontrollerspec-v1-core">K8s
    *   ReplicationControllerSpec</a>
    */
  object ReplicationControllerDesiredPods extends MetricSpec {

    val name: String = "k8s.replication_controller.desired_pods"
    val description: String = "Number of desired replica pods in this replication controller"
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
    * currentRevision
    *
    * @note
    *   <p> This metric aligns with the `currentReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#statefulsetstatus-v1-apps">K8s
    *   StatefulSetStatus</a>. <p> This metric SHOULD, at a minimum, be reported against a <a
    *   href="../resource/k8s.md#statefulset">`k8s.statefulset`</a> resource.
    */
  object StatefulsetCurrentPods extends MetricSpec {

    val name: String = "k8s.statefulset.current_pods"
    val description: String =
      "The number of replica pods created by the statefulset controller from the statefulset version indicated by currentRevision"
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

  /** Number of desired replica pods in this statefulset
    *
    * @note
    *   <p> This metric aligns with the `replicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#statefulsetspec-v1-apps">K8s
    *   StatefulSetSpec</a>. <p> This metric SHOULD, at a minimum, be reported against a <a
    *   href="../resource/k8s.md#statefulset">`k8s.statefulset`</a> resource.
    */
  object StatefulsetDesiredPods extends MetricSpec {

    val name: String = "k8s.statefulset.desired_pods"
    val description: String = "Number of desired replica pods in this statefulset"
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

  /** The number of replica pods created for this statefulset with a Ready Condition
    *
    * @note
    *   <p> This metric aligns with the `readyReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#statefulsetstatus-v1-apps">K8s
    *   StatefulSetStatus</a>. <p> This metric SHOULD, at a minimum, be reported against a <a
    *   href="../resource/k8s.md#statefulset">`k8s.statefulset`</a> resource.
    */
  object StatefulsetReadyPods extends MetricSpec {

    val name: String = "k8s.statefulset.ready_pods"
    val description: String = "The number of replica pods created for this statefulset with a Ready Condition"
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
    * updateRevision
    *
    * @note
    *   <p> This metric aligns with the `updatedReplicas` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#statefulsetstatus-v1-apps">K8s
    *   StatefulSetStatus</a>. <p> This metric SHOULD, at a minimum, be reported against a <a
    *   href="../resource/k8s.md#statefulset">`k8s.statefulset`</a> resource.
    */
  object StatefulsetUpdatedPods extends MetricSpec {

    val name: String = "k8s.statefulset.updated_pods"
    val description: String =
      "Number of replica pods created by the statefulset controller from the statefulset version indicated by updateRevision"
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
