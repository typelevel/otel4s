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
package attributes

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/attributes/SemanticAttributes.scala.j2
object K8sAttributes {

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

  /** The annotation placed on the DaemonSet, the `<key>` being the annotation name, the value being the annotation
    * value, even if the value is empty.
    *
    * @note
    *   <p> Examples: <ul> <li>A label `replicas` with value `1` SHOULD be recorded as the
    *   `k8s.daemonset.annotation.replicas` attribute with value `"1"`. <li>A label `data` with empty string value
    *   SHOULD be recorded as the `k8s.daemonset.annotation.data` attribute with value `""`. </ul>
    */
  val K8sDaemonsetAnnotation: AttributeKey[String] =
    AttributeKey("k8s.daemonset.annotation")

  /** The label placed on the DaemonSet, the `<key>` being the label name, the value being the label value, even if the
    * value is empty.
    *
    * @note
    *   <p> Examples: <ul> <li>A label `app` with value `guestbook` SHOULD be recorded as the `k8s.daemonset.label.app`
    *   attribute with value `"guestbook"`. <li>A label `data` with empty string value SHOULD be recorded as the
    *   `k8s.daemonset.label.injected` attribute with value `""`. </ul>
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

  /** The annotation placed on the Deployment, the `<key>` being the annotation name, the value being the annotation
    * value, even if the value is empty.
    *
    * @note
    *   <p> Examples: <ul> <li>A label `replicas` with value `1` SHOULD be recorded as the
    *   `k8s.deployment.annotation.replicas` attribute with value `"1"`. <li>A label `data` with empty string value
    *   SHOULD be recorded as the `k8s.deployment.annotation.data` attribute with value `""`. </ul>
    */
  val K8sDeploymentAnnotation: AttributeKey[String] =
    AttributeKey("k8s.deployment.annotation")

  /** The label placed on the Deployment, the `<key>` being the label name, the value being the label value, even if the
    * value is empty.
    *
    * @note
    *   <p> Examples: <ul> <li>A label `replicas` with value `0` SHOULD be recorded as the `k8s.deployment.label.app`
    *   attribute with value `"guestbook"`. <li>A label `injected` with empty string value SHOULD be recorded as the
    *   `k8s.deployment.label.injected` attribute with value `""`. </ul>
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

  /** The annotation placed on the Job, the `<key>` being the annotation name, the value being the annotation value,
    * even if the value is empty.
    *
    * @note
    *   <p> Examples: <ul> <li>A label `number` with value `1` SHOULD be recorded as the `k8s.job.annotation.number`
    *   attribute with value `"1"`. <li>A label `data` with empty string value SHOULD be recorded as the
    *   `k8s.job.annotation.data` attribute with value `""`. </ul>
    */
  val K8sJobAnnotation: AttributeKey[String] =
    AttributeKey("k8s.job.annotation")

  /** The label placed on the Job, the `<key>` being the label name, the value being the label value, even if the value
    * is empty.
    *
    * @note
    *   <p> Examples: <ul> <li>A label `jobtype` with value `ci` SHOULD be recorded as the `k8s.job.label.jobtype`
    *   attribute with value `"ci"`. <li>A label `data` with empty string value SHOULD be recorded as the
    *   `k8s.job.label.automated` attribute with value `""`. </ul>
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

  /** The annotation placed on the Namespace, the `<key>` being the annotation name, the value being the annotation
    * value, even if the value is empty.
    *
    * @note
    *   <p> Examples: <ul> <li>A label `ttl` with value `0` SHOULD be recorded as the `k8s.namespace.annotation.ttl`
    *   attribute with value `"0"`. <li>A label `data` with empty string value SHOULD be recorded as the
    *   `k8s.namespace.annotation.data` attribute with value `""`. </ul>
    */
  val K8sNamespaceAnnotation: AttributeKey[String] =
    AttributeKey("k8s.namespace.annotation")

  /** The label placed on the Namespace, the `<key>` being the label name, the value being the label value, even if the
    * value is empty.
    *
    * @note
    *   <p> Examples: <ul> <li>A label `kubernetes.io/metadata.name` with value `default` SHOULD be recorded as the
    *   `k8s.namespace.label.kubernetes.io/metadata.name` attribute with value `"default"`. <li>A label `data` with
    *   empty string value SHOULD be recorded as the `k8s.namespace.label.data` attribute with value `""`. </ul>
    */
  val K8sNamespaceLabel: AttributeKey[String] =
    AttributeKey("k8s.namespace.label")

  /** The name of the namespace that the pod is running in.
    */
  val K8sNamespaceName: AttributeKey[String] =
    AttributeKey("k8s.namespace.name")

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

  /** Specifies the hostname of the Pod.
    *
    * @note
    *   <p> The K8s Pod spec has an optional hostname field, which can be used to specify a hostname. Refer to <a
    *   href="https://kubernetes.io/docs/concepts/services-networking/dns-pod-service/#pod-hostname-and-subdomain-field">K8s
    *   docs</a> for more information about this field. <p> This attribute aligns with the `hostname` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.34/#podspec-v1-core">K8s PodSpec</a>.
    */
  val K8sPodHostname: AttributeKey[String] =
    AttributeKey("k8s.pod.hostname")

  /** IP address allocated to the Pod.
    *
    * @note
    *   <p> This attribute aligns with the `podIP` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.34/#podstatus-v1-core">K8s PodStatus</a>.
    */
  val K8sPodIp: AttributeKey[String] =
    AttributeKey("k8s.pod.ip")

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

  /** The name of the Pod.
    */
  val K8sPodName: AttributeKey[String] =
    AttributeKey("k8s.pod.name")

  /** The start timestamp of the Pod.
    *
    * @note
    *   <p> Date and time at which the object was acknowledged by the Kubelet. This is before the Kubelet pulled the
    *   container image(s) for the pod. <p> This attribute aligns with the `startTime` field of the <a
    *   href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.34/#podstatus-v1-core">K8s PodStatus</a>,
    *   in ISO 8601 (RFC 3339 compatible) format.
    */
  val K8sPodStartTime: AttributeKey[String] =
    AttributeKey("k8s.pod.start_time")

  /** The UID of the Pod.
    */
  val K8sPodUid: AttributeKey[String] =
    AttributeKey("k8s.pod.uid")

  /** The annotation placed on the ReplicaSet, the `<key>` being the annotation name, the value being the annotation
    * value, even if the value is empty.
    *
    * @note
    *   <p> Examples: <ul> <li>A label `replicas` with value `0` SHOULD be recorded as the
    *   `k8s.replicaset.annotation.replicas` attribute with value `"0"`. <li>A label `data` with empty string value
    *   SHOULD be recorded as the `k8s.replicaset.annotation.data` attribute with value `""`. </ul>
    */
  val K8sReplicasetAnnotation: AttributeKey[String] =
    AttributeKey("k8s.replicaset.annotation")

  /** The label placed on the ReplicaSet, the `<key>` being the label name, the value being the label value, even if the
    * value is empty.
    *
    * @note
    *   <p> Examples: <ul> <li>A label `app` with value `guestbook` SHOULD be recorded as the `k8s.replicaset.label.app`
    *   attribute with value `"guestbook"`. <li>A label `injected` with empty string value SHOULD be recorded as the
    *   `k8s.replicaset.label.injected` attribute with value `""`. </ul>
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

  /** The annotation placed on the StatefulSet, the `<key>` being the annotation name, the value being the annotation
    * value, even if the value is empty.
    *
    * @note
    *   <p> Examples: <ul> <li>A label `replicas` with value `1` SHOULD be recorded as the
    *   `k8s.statefulset.annotation.replicas` attribute with value `"1"`. <li>A label `data` with empty string value
    *   SHOULD be recorded as the `k8s.statefulset.annotation.data` attribute with value `""`. </ul>
    */
  val K8sStatefulsetAnnotation: AttributeKey[String] =
    AttributeKey("k8s.statefulset.annotation")

  /** The label placed on the StatefulSet, the `<key>` being the label name, the value being the label value, even if
    * the value is empty.
    *
    * @note
    *   <p> Examples: <ul> <li>A label `replicas` with value `0` SHOULD be recorded as the `k8s.statefulset.label.app`
    *   attribute with value `"guestbook"`. <li>A label `injected` with empty string value SHOULD be recorded as the
    *   `k8s.statefulset.label.injected` attribute with value `""`. </ul>
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

}
