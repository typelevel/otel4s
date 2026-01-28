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
object OpenshiftExperimentalMetrics {

  val specs: List[MetricSpec] = List(
    ClusterquotaCpuLimitHard,
    ClusterquotaCpuLimitUsed,
    ClusterquotaCpuRequestHard,
    ClusterquotaCpuRequestUsed,
    ClusterquotaEphemeralStorageLimitHard,
    ClusterquotaEphemeralStorageLimitUsed,
    ClusterquotaEphemeralStorageRequestHard,
    ClusterquotaEphemeralStorageRequestUsed,
    ClusterquotaHugepageCountRequestHard,
    ClusterquotaHugepageCountRequestUsed,
    ClusterquotaMemoryLimitHard,
    ClusterquotaMemoryLimitUsed,
    ClusterquotaMemoryRequestHard,
    ClusterquotaMemoryRequestUsed,
    ClusterquotaObjectCountHard,
    ClusterquotaObjectCountUsed,
    ClusterquotaPersistentvolumeclaimCountHard,
    ClusterquotaPersistentvolumeclaimCountUsed,
    ClusterquotaStorageRequestHard,
    ClusterquotaStorageRequestUsed,
  )

  /** The enforced hard limit of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    */
  object ClusterquotaCpuLimitHard extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.cpu.limit.hard"
    val description: String = "The enforced hard limit of the resource across all projects."
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

  /** The current observed total usage of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    */
  object ClusterquotaCpuLimitUsed extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.cpu.limit.used"
    val description: String = "The current observed total usage of the resource across all projects."
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

  /** The enforced hard limit of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    */
  object ClusterquotaCpuRequestHard extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.cpu.request.hard"
    val description: String = "The enforced hard limit of the resource across all projects."
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

  /** The current observed total usage of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    */
  object ClusterquotaCpuRequestUsed extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.cpu.request.used"
    val description: String = "The current observed total usage of the resource across all projects."
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

  /** The enforced hard limit of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    */
  object ClusterquotaEphemeralStorageLimitHard extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.ephemeral_storage.limit.hard"
    val description: String = "The enforced hard limit of the resource across all projects."
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

  /** The current observed total usage of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    */
  object ClusterquotaEphemeralStorageLimitUsed extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.ephemeral_storage.limit.used"
    val description: String = "The current observed total usage of the resource across all projects."
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

  /** The enforced hard limit of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    */
  object ClusterquotaEphemeralStorageRequestHard extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.ephemeral_storage.request.hard"
    val description: String = "The enforced hard limit of the resource across all projects."
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

  /** The current observed total usage of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    */
  object ClusterquotaEphemeralStorageRequestUsed extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.ephemeral_storage.request.used"
    val description: String = "The current observed total usage of the resource across all projects."
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

  /** The enforced hard limit of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    */
  object ClusterquotaHugepageCountRequestHard extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.hugepage_count.request.hard"
    val description: String = "The enforced hard limit of the resource across all projects."
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

  /** The current observed total usage of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    */
  object ClusterquotaHugepageCountRequestUsed extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.hugepage_count.request.used"
    val description: String = "The current observed total usage of the resource across all projects."
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

  /** The enforced hard limit of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    */
  object ClusterquotaMemoryLimitHard extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.memory.limit.hard"
    val description: String = "The enforced hard limit of the resource across all projects."
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

  /** The current observed total usage of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    */
  object ClusterquotaMemoryLimitUsed extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.memory.limit.used"
    val description: String = "The current observed total usage of the resource across all projects."
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

  /** The enforced hard limit of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    */
  object ClusterquotaMemoryRequestHard extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.memory.request.hard"
    val description: String = "The enforced hard limit of the resource across all projects."
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

  /** The current observed total usage of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    */
  object ClusterquotaMemoryRequestUsed extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.memory.request.used"
    val description: String = "The current observed total usage of the resource across all projects."
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

  /** The enforced hard limit of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    */
  object ClusterquotaObjectCountHard extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.object_count.hard"
    val description: String = "The enforced hard limit of the resource across all projects."
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

  /** The current observed total usage of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    */
  object ClusterquotaObjectCountUsed extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.object_count.used"
    val description: String = "The current observed total usage of the resource across all projects."
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

  /** The enforced hard limit of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    *   <p> The `k8s.storageclass.name` should be required when a resource quota is defined for a specific storage
    *   class.
    */
  object ClusterquotaPersistentvolumeclaimCountHard extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.persistentvolumeclaim_count.hard"
    val description: String = "The enforced hard limit of the resource across all projects."
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

  /** The current observed total usage of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    *   <p> The `k8s.storageclass.name` should be required when a resource quota is defined for a specific storage
    *   class.
    */
  object ClusterquotaPersistentvolumeclaimCountUsed extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.persistentvolumeclaim_count.used"
    val description: String = "The current observed total usage of the resource across all projects."
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

  /** The enforced hard limit of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Hard` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    *   <p> The `k8s.storageclass.name` should be required when a resource quota is defined for a specific storage
    *   class.
    */
  object ClusterquotaStorageRequestHard extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.storage.request.hard"
    val description: String = "The enforced hard limit of the resource across all projects."
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

  /** The current observed total usage of the resource across all projects.
    *
    * @note
    *   <p> This metric is retrieved from the `Status.Total.Used` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.32/#resourcequotastatus-v1-core">K8s
    *   ResourceQuotaStatus</a> of the <a
    *   href="https://docs.redhat.com/en/documentation/openshift_container_platform/4.19/html/schedule_and_quota_apis/clusterresourcequota-quota-openshift-io-v1#status-total">ClusterResourceQuota</a>.
    *   <p> The `k8s.storageclass.name` should be required when a resource quota is defined for a specific storage
    *   class.
    */
  object ClusterquotaStorageRequestUsed extends MetricSpec.Unsealed {

    val name: String = "openshift.clusterquota.storage.request.used"
    val description: String = "The current observed total usage of the resource across all projects."
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

}
