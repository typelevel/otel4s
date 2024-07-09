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

package org.typelevel.otel4s.semconv.experimental.attributes

import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.AttributeKey._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/semantic-convention/templates/SemanticAttributes.scala.j2
object K8sExperimentalAttributes {

  /** The name of the cluster.
    */
  val K8sClusterName: AttributeKey[String] = string("k8s.cluster.name")

  /** A pseudo-ID for the cluster, set to the UID of the `kube-system`
    * namespace.
    *
    * @note
    *   - K8s doesn't have support for obtaining a cluster ID. If this is ever
    *     added, we will recommend collecting the `k8s.cluster.uid` through the
    *     official APIs. In the meantime, we are able to use the `uid` of the
    *     `kube-system` namespace as a proxy for cluster ID. Read on for the
    *     rationale.
    *   - Every object created in a K8s cluster is assigned a distinct UID. The
    *     `kube-system` namespace is used by Kubernetes itself and will exist
    *     for the lifetime of the cluster. Using the `uid` of the `kube-system`
    *     namespace is a reasonable proxy for the K8s ClusterID as it will only
    *     change if the cluster is rebuilt. Furthermore, Kubernetes UIDs are
    *     UUIDs as standardized by <a
    *     href="https://www.itu.int/ITU-T/studygroups/com17/oid.html">ISO/IEC
    *     9834-8 and ITU-T X.667</a>. Which states:<blockquote>
    *
    *   - If generated according to one of the mechanisms defined in
    *     Rec.</blockquote>
    *
    *   - ITU-T X.667 | ISO/IEC 9834-8, a UUID is either guaranteed to be
    * different from all other UUIDs generated before 3603 A.D., or is extremely
    * likely to be different (depending on the mechanism chosen).
    *   - Therefore, UIDs between clusters should be extremely unlikely to
    * conflict.
    */
  val K8sClusterUid: AttributeKey[String] = string("k8s.cluster.uid")

  /** The name of the Container from Pod specification, must be unique within a
    * Pod. Container runtime usually uses different globally unique name
    * (`container.name`).
    */
  val K8sContainerName: AttributeKey[String] = string("k8s.container.name")

  /** Number of times the container was restarted. This attribute can be used to
    * identify a particular container (running or stopped) within a container
    * spec.
    */
  val K8sContainerRestartCount: AttributeKey[Long] = long(
    "k8s.container.restart_count"
  )

  /** Last terminated reason of the Container.
    */
  val K8sContainerStatusLastTerminatedReason: AttributeKey[String] = string(
    "k8s.container.status.last_terminated_reason"
  )

  /** The name of the CronJob.
    */
  val K8sCronjobName: AttributeKey[String] = string("k8s.cronjob.name")

  /** The UID of the CronJob.
    */
  val K8sCronjobUid: AttributeKey[String] = string("k8s.cronjob.uid")

  /** The name of the DaemonSet.
    */
  val K8sDaemonsetName: AttributeKey[String] = string("k8s.daemonset.name")

  /** The UID of the DaemonSet.
    */
  val K8sDaemonsetUid: AttributeKey[String] = string("k8s.daemonset.uid")

  /** The name of the Deployment.
    */
  val K8sDeploymentName: AttributeKey[String] = string("k8s.deployment.name")

  /** The UID of the Deployment.
    */
  val K8sDeploymentUid: AttributeKey[String] = string("k8s.deployment.uid")

  /** The name of the Job.
    */
  val K8sJobName: AttributeKey[String] = string("k8s.job.name")

  /** The UID of the Job.
    */
  val K8sJobUid: AttributeKey[String] = string("k8s.job.uid")

  /** The name of the namespace that the pod is running in.
    */
  val K8sNamespaceName: AttributeKey[String] = string("k8s.namespace.name")

  /** The name of the Node.
    */
  val K8sNodeName: AttributeKey[String] = string("k8s.node.name")

  /** The UID of the Node.
    */
  val K8sNodeUid: AttributeKey[String] = string("k8s.node.uid")

  /** The annotation key-value pairs placed on the Pod, the `<key>` being the
    * annotation name, the value being the annotation value.
    */
  val K8sPodAnnotation: AttributeKey[String] = string("k8s.pod.annotation")

  /** The label key-value pairs placed on the Pod, the `<key>` being the label
    * name, the value being the label value.
    */
  val K8sPodLabel: AttributeKey[String] = string("k8s.pod.label")

  /** Deprecated, use `k8s.pod.label` instead.
    */
  @deprecated("Use `k8s.pod.label` instead", "0.5.0")
  val K8sPodLabels: AttributeKey[String] = string("k8s.pod.labels")

  /** The name of the Pod.
    */
  val K8sPodName: AttributeKey[String] = string("k8s.pod.name")

  /** The UID of the Pod.
    */
  val K8sPodUid: AttributeKey[String] = string("k8s.pod.uid")

  /** The name of the ReplicaSet.
    */
  val K8sReplicasetName: AttributeKey[String] = string("k8s.replicaset.name")

  /** The UID of the ReplicaSet.
    */
  val K8sReplicasetUid: AttributeKey[String] = string("k8s.replicaset.uid")

  /** The name of the StatefulSet.
    */
  val K8sStatefulsetName: AttributeKey[String] = string("k8s.statefulset.name")

  /** The UID of the StatefulSet.
    */
  val K8sStatefulsetUid: AttributeKey[String] = string("k8s.statefulset.uid")

}
