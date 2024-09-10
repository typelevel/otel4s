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

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/attributes/experimental/SemanticAttributes.scala.j2
object GcpExperimentalAttributes {

  /** Identifies the Google Cloud service for which the official client library is intended. <p>
    * @note
    *   <p> Intended to be a stable identifier for Google Cloud client libraries that is uniform across implementation
    *   languages. The value should be derived from the canonical service domain for the service; for example,
    *   'foo.googleapis.com' should result in a value of 'foo'.
    */
  val GcpClientService: AttributeKey[String] =
    AttributeKey("gcp.client.service")

  /** The name of the Cloud Run <a href="https://cloud.google.com/run/docs/managing/job-executions">execution</a> being
    * run for the Job, as set by the <a
    * href="https://cloud.google.com/run/docs/container-contract#jobs-env-vars">`CLOUD_RUN_EXECUTION`</a> environment
    * variable.
    */
  val GcpCloudRunJobExecution: AttributeKey[String] =
    AttributeKey("gcp.cloud_run.job.execution")

  /** The index for a task within an execution as provided by the <a
    * href="https://cloud.google.com/run/docs/container-contract#jobs-env-vars">`CLOUD_RUN_TASK_INDEX`</a> environment
    * variable.
    */
  val GcpCloudRunJobTaskIndex: AttributeKey[Long] =
    AttributeKey("gcp.cloud_run.job.task_index")

  /** The hostname of a GCE instance. This is the full value of the default or <a
    * href="https://cloud.google.com/compute/docs/instances/custom-hostname-vm">custom hostname</a>.
    */
  val GcpGceInstanceHostname: AttributeKey[String] =
    AttributeKey("gcp.gce.instance.hostname")

  /** The instance name of a GCE instance. This is the value provided by `host.name`, the visible name of the instance
    * in the Cloud Console UI, and the prefix for the default hostname of the instance as defined by the <a
    * href="https://cloud.google.com/compute/docs/internal-dns#instance-fully-qualified-domain-names">default internal
    * DNS name</a>.
    */
  val GcpGceInstanceName: AttributeKey[String] =
    AttributeKey("gcp.gce.instance.name")

}
