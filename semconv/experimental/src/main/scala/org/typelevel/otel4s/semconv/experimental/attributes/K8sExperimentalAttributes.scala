/*
 * Copyright 2023 Typelevel
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
package experimental.attributes

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/attributes/SemanticAttributes.scala.j2
object K8sExperimentalAttributes {

  /** The name of the cluster.
    */
  val K8sClusterName: AttributeKey[String] =
    AttributeKey("k8s.cluster.name")

  /** A pseudo-ID for the cluster, set to the UID of the `kube-system` namespace.
    *
    * @note
    *   <p> K8s doesn't have support for obtaining a cluster ID. If this is ever added, we will recommend collecting the
    *   `k8s.cluster.uid` through the official APIs. In the meantime, we are able to use the `uid` of the `kube-system`
    *   namespace as a proxy for cluster ID. Read on for the rationale. <p> Every object created in a K8s cluster is
    *   assigned a distinct UID. The `kube-system` namespace is used by Kubernetes itself and will exist for the
    *   lifetime of the cluster. Using the `uid` of the `kube-system` namespace is a reasonable proxy for the K8s
    *   ClusterID as it will only change if the cluster is rebuilt. Furthermore, Kubernetes UIDs are UUIDs as
    *   standardized by <a href="https://www.itu.int/ITU-T/studygroups/com17/oid.html">ISO/IEC 9834-8 and ITU-T
    *   X.667</a>. Which states: <blockquote> If generated according to one of the mechanisms defined in Rec. ITU-T
    *   X.667 | ISO/IEC 9834-8, a UUID is either guaranteed to be different from all other UUIDs generated before 3603
    *   A.D., or is extremely likely to be different (depending on the mechanism chosen).</blockquote> <p> Therefore,
    *   UIDs between clusters should be extremely unlikely to conflict.
    */
  val K8sClusterUid: AttributeKey[String] =
    AttributeKey("k8s.cluster.uid")

  /** The name of the Container from Pod specification, must be unique within a Pod. Container runtime usually uses
    * different globally unique name (`container.name`).
    */
  val K8sContainerName: AttributeKey[String] =
    AttributeKey("k8s.container.name")

  /** Number of times the container was restarted. This attribute can be used to identify a particular container
    * (running or stopped) within a container spec.
    */
  val K8sContainerRestartCount: AttributeKey[Long] =
    AttributeKey("k8s.container.restart_count")

  /** Last terminated reason of the Container.
    */
  val K8sContainerStatusLastTerminatedReason: AttributeKey[String] =
    AttributeKey("k8s.container.status.last_terminated_reason")

  /** The cronjob annotation placed on the CronJob, the `<key>` being the annotation name, the value being the
    * annotation value.
    *
    * @note
    *   <p> Examples: <ul> <li>An annotation `retries` with value `4` SHOULD be recorded as the
    *   `k8s.cronjob.annotation.retries` attribute with value `"4"`. <li>An annotation `data` with empty string value
    *   SHOULD be recorded as the `k8s.cronjob.annotation.data` attribute with value `""`. </ul>
    */
  val K8sCronjobAnnotation: AttributeKey[String] =
    AttributeKey("k8s.cronjob.annotation")

  /** The label placed on the CronJob, the `<key>` being the label name, the value being the label value.
    *
    * @note
    *   <p> Examples: <ul> <li>A label `type` with value `weekly` SHOULD be recorded as the `k8s.cronjob.label.type`
    *   attribute with value `"weekly"`. <li>A label `automated` with empty string value SHOULD be recorded as the
    *   `k8s.cronjob.label.automated` attribute with value `""`. </ul>
    */
  val K8sCronjobLabel: AttributeKey[String] =
    AttributeKey("k8s.cronjob.label")

  /** The name of the CronJob.
    */
  val K8sCronjobName: AttributeKey[String] =
    AttributeKey("k8s.cronjob.name")

  /** The UID of the CronJob.
    */
  val K8sCronjobUid: AttributeKey[String] =
    AttributeKey("k8s.cronjob.uid")

  /** The annotation key-value pairs placed on the DaemonSet.
    *
    * @note
    *   <p> The `<key>` being the annotation name, the value being the annotation value, even if the value is empty.
    */
  val K8sDaemonsetAnnotation: AttributeKey[String] =
    AttributeKey("k8s.daemonset.annotation")

  /** The label key-value pairs placed on the DaemonSet.
    *
    * @note
    *   <p> The `<key>` being the label name, the value being the label value, even if the value is empty.
    */
  val K8sDaemonsetLabel: AttributeKey[String] =
    AttributeKey("k8s.daemonset.label")

  /** The name of the DaemonSet.
    */
  val K8sDaemonsetName: AttributeKey[String] =
    AttributeKey("k8s.daemonset.name")

  /** The UID of the DaemonSet.
    */
  val K8sDaemonsetUid: AttributeKey[String] =
    AttributeKey("k8s.daemonset.uid")

  /** The annotation key-value pairs placed on the Deployment.
    *
    * @note
    *   <p> The `<key>` being the annotation name, the value being the annotation value, even if the value is empty.
    */
  val K8sDeploymentAnnotation: AttributeKey[String] =
    AttributeKey("k8s.deployment.annotation")

  /** The label key-value pairs placed on the Deployment.
    *
    * @note
    *   <p> The `<key>` being the label name, the value being the label value, even if the value is empty.
    */
  val K8sDeploymentLabel: AttributeKey[String] =
    AttributeKey("k8s.deployment.label")

  /** The name of the Deployment.
    */
  val K8sDeploymentName: AttributeKey[String] =
    AttributeKey("k8s.deployment.name")

  /** The UID of the Deployment.
    */
  val K8sDeploymentUid: AttributeKey[String] =
    AttributeKey("k8s.deployment.uid")

  /** The name of the horizontal pod autoscaler.
    */
  val K8sHpaName: AttributeKey[String] =
    AttributeKey("k8s.hpa.name")

  /** The UID of the horizontal pod autoscaler.
    */
  val K8sHpaUid: AttributeKey[String] =
    AttributeKey("k8s.hpa.uid")

  /** The annotation key-value pairs placed on the Job.
    *
    * @note
    *   <p> The `<key>` being the annotation name, the value being the annotation value, even if the value is empty.
    */
  val K8sJobAnnotation: AttributeKey[String] =
    AttributeKey("k8s.job.annotation")

  /** The label key-value pairs placed on the Job.
    *
    * @note
    *   <p> The `<key>` being the label name, the value being the label value, even if the value is empty.
    */
  val K8sJobLabel: AttributeKey[String] =
    AttributeKey("k8s.job.label")

  /** The name of the Job.
    */
  val K8sJobName: AttributeKey[String] =
    AttributeKey("k8s.job.name")

  /** The UID of the Job.
    */
  val K8sJobUid: AttributeKey[String] =
    AttributeKey("k8s.job.uid")

  /** The annotation key-value pairs placed on the Namespace.
    *
    * @note
    *   <p> The `<key>` being the annotation name, the value being the annotation value, even if the value is empty.
    */
  val K8sNamespaceAnnotation: AttributeKey[String] =
    AttributeKey("k8s.namespace.annotation")

  /** The label key-value pairs placed on the Namespace.
    *
    * @note
    *   <p> The `<key>` being the label name, the value being the label value, even if the value is empty.
    */
  val K8sNamespaceLabel: AttributeKey[String] =
    AttributeKey("k8s.namespace.label")

  /** The name of the namespace that the pod is running in.
    */
  val K8sNamespaceName: AttributeKey[String] =
    AttributeKey("k8s.namespace.name")

  /** The phase of the K8s namespace.
    *
    * @note
    *   <p> This attribute aligns with the `phase` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#namespacestatus-v1-core">K8s
    *   NamespaceStatus</a>
    */
  val K8sNamespacePhase: AttributeKey[String] =
    AttributeKey("k8s.namespace.phase")

  /** The annotation placed on the Node, the `<key>` being the annotation name, the value being the annotation value,
    * even if the value is empty.
    *
    * @note
    *   <p> Examples: <ul> <li>An annotation `node.alpha.kubernetes.io/ttl` with value `0` SHOULD be recorded as the
    *   `k8s.node.annotation.node.alpha.kubernetes.io/ttl` attribute with value `"0"`. <li>An annotation `data` with
    *   empty string value SHOULD be recorded as the `k8s.node.annotation.data` attribute with value `""`. </ul>
    */
  val K8sNodeAnnotation: AttributeKey[String] =
    AttributeKey("k8s.node.annotation")

  /** The label placed on the Node, the `<key>` being the label name, the value being the label value, even if the value
    * is empty.
    *
    * @note
    *   <p> Examples: <ul> <li>A label `kubernetes.io/arch` with value `arm64` SHOULD be recorded as the
    *   `k8s.node.label.kubernetes.io/arch` attribute with value `"arm64"`. <li>A label `data` with empty string value
    *   SHOULD be recorded as the `k8s.node.label.data` attribute with value `""`. </ul>
    */
  val K8sNodeLabel: AttributeKey[String] =
    AttributeKey("k8s.node.label")

  /** The name of the Node.
    */
  val K8sNodeName: AttributeKey[String] =
    AttributeKey("k8s.node.name")

  /** The UID of the Node.
    */
  val K8sNodeUid: AttributeKey[String] =
    AttributeKey("k8s.node.uid")

  /** The annotation placed on the Pod, the `<key>` being the annotation name, the value being the annotation value.
    *
    * @note
    *   <p> Examples: <ul> <li>An annotation `kubernetes.io/enforce-mountable-secrets` with value `true` SHOULD be
    *   recorded as the `k8s.pod.annotation.kubernetes.io/enforce-mountable-secrets` attribute with value `"true"`.
    *   <li>An annotation `mycompany.io/arch` with value `x64` SHOULD be recorded as the
    *   `k8s.pod.annotation.mycompany.io/arch` attribute with value `"x64"`. <li>An annotation `data` with empty string
    *   value SHOULD be recorded as the `k8s.pod.annotation.data` attribute with value `""`. </ul>
    */
  val K8sPodAnnotation: AttributeKey[String] =
    AttributeKey("k8s.pod.annotation")

  /** The label placed on the Pod, the `<key>` being the label name, the value being the label value.
    *
    * @note
    *   <p> Examples: <ul> <li>A label `app` with value `my-app` SHOULD be recorded as the `k8s.pod.label.app` attribute
    *   with value `"my-app"`. <li>A label `mycompany.io/arch` with value `x64` SHOULD be recorded as the
    *   `k8s.pod.label.mycompany.io/arch` attribute with value `"x64"`. <li>A label `data` with empty string value
    *   SHOULD be recorded as the `k8s.pod.label.data` attribute with value `""`. </ul>
    */
  val K8sPodLabel: AttributeKey[String] =
    AttributeKey("k8s.pod.label")

  /** Deprecated, use `k8s.pod.label` instead.
    */
  @deprecated("Replaced by `k8s.pod.label`.", "")
  val K8sPodLabels: AttributeKey[String] =
    AttributeKey("k8s.pod.labels")

  /** The name of the Pod.
    */
  val K8sPodName: AttributeKey[String] =
    AttributeKey("k8s.pod.name")

  /** The UID of the Pod.
    */
  val K8sPodUid: AttributeKey[String] =
    AttributeKey("k8s.pod.uid")

  /** The annotation key-value pairs placed on the ReplicaSet.
    *
    * @note
    *   <p> The `<key>` being the annotation name, the value being the annotation value, even if the value is empty.
    */
  val K8sReplicasetAnnotation: AttributeKey[String] =
    AttributeKey("k8s.replicaset.annotation")

  /** The label key-value pairs placed on the ReplicaSet.
    *
    * @note
    *   <p> The `<key>` being the label name, the value being the label value, even if the value is empty.
    */
  val K8sReplicasetLabel: AttributeKey[String] =
    AttributeKey("k8s.replicaset.label")

  /** The name of the ReplicaSet.
    */
  val K8sReplicasetName: AttributeKey[String] =
    AttributeKey("k8s.replicaset.name")

  /** The UID of the ReplicaSet.
    */
  val K8sReplicasetUid: AttributeKey[String] =
    AttributeKey("k8s.replicaset.uid")

  /** The name of the replication controller.
    */
  val K8sReplicationcontrollerName: AttributeKey[String] =
    AttributeKey("k8s.replicationcontroller.name")

  /** The UID of the replication controller.
    */
  val K8sReplicationcontrollerUid: AttributeKey[String] =
    AttributeKey("k8s.replicationcontroller.uid")

  /** The name of the resource quota.
    */
  val K8sResourcequotaName: AttributeKey[String] =
    AttributeKey("k8s.resourcequota.name")

  /** The UID of the resource quota.
    */
  val K8sResourcequotaUid: AttributeKey[String] =
    AttributeKey("k8s.resourcequota.uid")

  /** The annotation key-value pairs placed on the StatefulSet.
    *
    * @note
    *   <p> The `<key>` being the annotation name, the value being the annotation value, even if the value is empty.
    */
  val K8sStatefulsetAnnotation: AttributeKey[String] =
    AttributeKey("k8s.statefulset.annotation")

  /** The label key-value pairs placed on the StatefulSet.
    *
    * @note
    *   <p> The `<key>` being the label name, the value being the label value, even if the value is empty.
    */
  val K8sStatefulsetLabel: AttributeKey[String] =
    AttributeKey("k8s.statefulset.label")

  /** The name of the StatefulSet.
    */
  val K8sStatefulsetName: AttributeKey[String] =
    AttributeKey("k8s.statefulset.name")

  /** The UID of the StatefulSet.
    */
  val K8sStatefulsetUid: AttributeKey[String] =
    AttributeKey("k8s.statefulset.uid")

  /** The name of the K8s volume.
    */
  val K8sVolumeName: AttributeKey[String] =
    AttributeKey("k8s.volume.name")

  /** The type of the K8s volume.
    */
  val K8sVolumeType: AttributeKey[String] =
    AttributeKey("k8s.volume.type")

  /** Values for [[K8sNamespacePhase]].
    */
  abstract class K8sNamespacePhaseValue(val value: String)
  object K8sNamespacePhaseValue {

    /** Active namespace phase as described by <a
      * href="https://pkg.go.dev/k8s.io/api@v0.31.3/core/v1#NamespacePhase">K8s API</a>
      */
    case object Active extends K8sNamespacePhaseValue("active")

    /** Terminating namespace phase as described by <a
      * href="https://pkg.go.dev/k8s.io/api@v0.31.3/core/v1#NamespacePhase">K8s API</a>
      */
    case object Terminating extends K8sNamespacePhaseValue("terminating")
  }

  /** Values for [[K8sVolumeType]].
    */
  abstract class K8sVolumeTypeValue(val value: String)
  object K8sVolumeTypeValue {

    /** A <a
      * href="https://v1-30.docs.kubernetes.io/docs/concepts/storage/volumes/#persistentvolumeclaim">persistentVolumeClaim</a>
      * volume
      */
    case object PersistentVolumeClaim extends K8sVolumeTypeValue("persistentVolumeClaim")

    /** A <a href="https://v1-30.docs.kubernetes.io/docs/concepts/storage/volumes/#configmap">configMap</a> volume
      */
    case object ConfigMap extends K8sVolumeTypeValue("configMap")

    /** A <a href="https://v1-30.docs.kubernetes.io/docs/concepts/storage/volumes/#downwardapi">downwardAPI</a> volume
      */
    case object DownwardApi extends K8sVolumeTypeValue("downwardAPI")

    /** An <a href="https://v1-30.docs.kubernetes.io/docs/concepts/storage/volumes/#emptydir">emptyDir</a> volume
      */
    case object EmptyDir extends K8sVolumeTypeValue("emptyDir")

    /** A <a href="https://v1-30.docs.kubernetes.io/docs/concepts/storage/volumes/#secret">secret</a> volume
      */
    case object Secret extends K8sVolumeTypeValue("secret")

    /** A <a href="https://v1-30.docs.kubernetes.io/docs/concepts/storage/volumes/#local">local</a> volume
      */
    case object Local extends K8sVolumeTypeValue("local")
  }

}
