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
object GcpExperimentalAttributes {

  /** The container within GCP where the AppHub application is defined.
    */
  val GcpApphubApplicationContainer: AttributeKey[String] =
    AttributeKey("gcp.apphub.application.container")

  /** The name of the application as configured in AppHub.
    */
  val GcpApphubApplicationId: AttributeKey[String] =
    AttributeKey("gcp.apphub.application.id")

  /** The GCP zone or region where the application is defined.
    */
  val GcpApphubApplicationLocation: AttributeKey[String] =
    AttributeKey("gcp.apphub.application.location")

  /** Criticality of a service indicates its importance to the business.
    *
    * @note
    *   <p> <a href="https://cloud.google.com/app-hub/docs/reference/rest/v1/Attributes#type">See AppHub type enum</a>
    */
  val GcpApphubServiceCriticalityType: AttributeKey[String] =
    AttributeKey("gcp.apphub.service.criticality_type")

  /** Environment of a service is the stage of a software lifecycle.
    *
    * @note
    *   <p> <a href="https://cloud.google.com/app-hub/docs/reference/rest/v1/Attributes#type_1">See AppHub environment
    *   type</a>
    */
  val GcpApphubServiceEnvironmentType: AttributeKey[String] =
    AttributeKey("gcp.apphub.service.environment_type")

  /** The name of the service as configured in AppHub.
    */
  val GcpApphubServiceId: AttributeKey[String] =
    AttributeKey("gcp.apphub.service.id")

  /** Criticality of a workload indicates its importance to the business.
    *
    * @note
    *   <p> <a href="https://cloud.google.com/app-hub/docs/reference/rest/v1/Attributes#type">See AppHub type enum</a>
    */
  val GcpApphubWorkloadCriticalityType: AttributeKey[String] =
    AttributeKey("gcp.apphub.workload.criticality_type")

  /** Environment of a workload is the stage of a software lifecycle.
    *
    * @note
    *   <p> <a href="https://cloud.google.com/app-hub/docs/reference/rest/v1/Attributes#type_1">See AppHub environment
    *   type</a>
    */
  val GcpApphubWorkloadEnvironmentType: AttributeKey[String] =
    AttributeKey("gcp.apphub.workload.environment_type")

  /** The name of the workload as configured in AppHub.
    */
  val GcpApphubWorkloadId: AttributeKey[String] =
    AttributeKey("gcp.apphub.workload.id")

  /** Identifies the Google Cloud service for which the official client library is intended.
    *
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

  /** Values for [[GcpApphubServiceCriticalityType]].
    */
  abstract class GcpApphubServiceCriticalityTypeValue(val value: String)
  object GcpApphubServiceCriticalityTypeValue {

    /** Mission critical service.
      */
    case object MissionCritical extends GcpApphubServiceCriticalityTypeValue("MISSION_CRITICAL")

    /** High impact.
      */
    case object High extends GcpApphubServiceCriticalityTypeValue("HIGH")

    /** Medium impact.
      */
    case object Medium extends GcpApphubServiceCriticalityTypeValue("MEDIUM")

    /** Low impact.
      */
    case object Low extends GcpApphubServiceCriticalityTypeValue("LOW")
  }

  /** Values for [[GcpApphubServiceEnvironmentType]].
    */
  abstract class GcpApphubServiceEnvironmentTypeValue(val value: String)
  object GcpApphubServiceEnvironmentTypeValue {

    /** Production environment.
      */
    case object Production extends GcpApphubServiceEnvironmentTypeValue("PRODUCTION")

    /** Staging environment.
      */
    case object Staging extends GcpApphubServiceEnvironmentTypeValue("STAGING")

    /** Test environment.
      */
    case object Test extends GcpApphubServiceEnvironmentTypeValue("TEST")

    /** Development environment.
      */
    case object Development extends GcpApphubServiceEnvironmentTypeValue("DEVELOPMENT")
  }

  /** Values for [[GcpApphubWorkloadCriticalityType]].
    */
  abstract class GcpApphubWorkloadCriticalityTypeValue(val value: String)
  object GcpApphubWorkloadCriticalityTypeValue {

    /** Mission critical service.
      */
    case object MissionCritical extends GcpApphubWorkloadCriticalityTypeValue("MISSION_CRITICAL")

    /** High impact.
      */
    case object High extends GcpApphubWorkloadCriticalityTypeValue("HIGH")

    /** Medium impact.
      */
    case object Medium extends GcpApphubWorkloadCriticalityTypeValue("MEDIUM")

    /** Low impact.
      */
    case object Low extends GcpApphubWorkloadCriticalityTypeValue("LOW")
  }

  /** Values for [[GcpApphubWorkloadEnvironmentType]].
    */
  abstract class GcpApphubWorkloadEnvironmentTypeValue(val value: String)
  object GcpApphubWorkloadEnvironmentTypeValue {

    /** Production environment.
      */
    case object Production extends GcpApphubWorkloadEnvironmentTypeValue("PRODUCTION")

    /** Staging environment.
      */
    case object Staging extends GcpApphubWorkloadEnvironmentTypeValue("STAGING")

    /** Test environment.
      */
    case object Test extends GcpApphubWorkloadEnvironmentTypeValue("TEST")

    /** Development environment.
      */
    case object Development extends GcpApphubWorkloadEnvironmentTypeValue("DEVELOPMENT")
  }

}
