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

  /** The container within GCP where the AppHub destination application is defined.
    */
  val GcpApphubDestinationApplicationContainer: AttributeKey[String] =
    AttributeKey("gcp.apphub_destination.application.container")

  /** The name of the destination application as configured in AppHub.
    */
  val GcpApphubDestinationApplicationId: AttributeKey[String] =
    AttributeKey("gcp.apphub_destination.application.id")

  /** The GCP zone or region where the destination application is defined.
    */
  val GcpApphubDestinationApplicationLocation: AttributeKey[String] =
    AttributeKey("gcp.apphub_destination.application.location")

  /** Criticality of a destination workload indicates its importance to the business as specified in <a
    * href="https://cloud.google.com/app-hub/docs/reference/rest/v1/Attributes#type">AppHub type enum</a>
    */
  val GcpApphubDestinationServiceCriticalityType: AttributeKey[String] =
    AttributeKey("gcp.apphub_destination.service.criticality_type")

  /** Software lifecycle stage of a destination service as defined <a
    * href="https://cloud.google.com/app-hub/docs/reference/rest/v1/Attributes#type_1">AppHub environment type</a>
    */
  val GcpApphubDestinationServiceEnvironmentType: AttributeKey[String] =
    AttributeKey("gcp.apphub_destination.service.environment_type")

  /** The name of the destination service as configured in AppHub.
    */
  val GcpApphubDestinationServiceId: AttributeKey[String] =
    AttributeKey("gcp.apphub_destination.service.id")

  /** Criticality of a destination workload indicates its importance to the business as specified in <a
    * href="https://cloud.google.com/app-hub/docs/reference/rest/v1/Attributes#type">AppHub type enum</a>
    */
  val GcpApphubDestinationWorkloadCriticalityType: AttributeKey[String] =
    AttributeKey("gcp.apphub_destination.workload.criticality_type")

  /** Environment of a destination workload is the stage of a software lifecycle as provided in the <a
    * href="https://cloud.google.com/app-hub/docs/reference/rest/v1/Attributes#type_1">AppHub environment type</a>
    */
  val GcpApphubDestinationWorkloadEnvironmentType: AttributeKey[String] =
    AttributeKey("gcp.apphub_destination.workload.environment_type")

  /** The name of the destination workload as configured in AppHub.
    */
  val GcpApphubDestinationWorkloadId: AttributeKey[String] =
    AttributeKey("gcp.apphub_destination.workload.id")

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

  /** The name of the Instance Group Manager (IGM) that manages this VM, if any.
    */
  val GcpGceInstanceGroupManagerName: AttributeKey[String] =
    AttributeKey("gcp.gce.instance_group_manager.name")

  /** The region of a <strong>regional</strong> Instance Group Manager (e.g., `us-central1`). Set this
    * <strong>only</strong> when the IGM is regional.
    */
  val GcpGceInstanceGroupManagerRegion: AttributeKey[String] =
    AttributeKey("gcp.gce.instance_group_manager.region")

  /** The zone of a <strong>zonal</strong> Instance Group Manager (e.g., `us-central1-a`). Set this
    * <strong>only</strong> when the IGM is zonal.
    */
  val GcpGceInstanceGroupManagerZone: AttributeKey[String] =
    AttributeKey("gcp.gce.instance_group_manager.zone")

  /** Values for [[GcpApphubServiceCriticalityType]].
    */
  abstract class GcpApphubServiceCriticalityTypeValue(val value: String)
  object GcpApphubServiceCriticalityTypeValue {
    implicit val attributeFromGcpApphubServiceCriticalityTypeValue
        : Attribute.From[GcpApphubServiceCriticalityTypeValue, String] = _.value

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
    implicit val attributeFromGcpApphubServiceEnvironmentTypeValue
        : Attribute.From[GcpApphubServiceEnvironmentTypeValue, String] = _.value

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
    implicit val attributeFromGcpApphubWorkloadCriticalityTypeValue
        : Attribute.From[GcpApphubWorkloadCriticalityTypeValue, String] = _.value

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
    implicit val attributeFromGcpApphubWorkloadEnvironmentTypeValue
        : Attribute.From[GcpApphubWorkloadEnvironmentTypeValue, String] = _.value

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

  /** Values for [[GcpApphubDestinationServiceCriticalityType]].
    */
  abstract class GcpApphubDestinationServiceCriticalityTypeValue(val value: String)
  object GcpApphubDestinationServiceCriticalityTypeValue {
    implicit val attributeFromGcpApphubDestinationServiceCriticalityTypeValue
        : Attribute.From[GcpApphubDestinationServiceCriticalityTypeValue, String] = _.value

    /** Mission critical service.
      */
    case object MissionCritical extends GcpApphubDestinationServiceCriticalityTypeValue("MISSION_CRITICAL")

    /** High impact.
      */
    case object High extends GcpApphubDestinationServiceCriticalityTypeValue("HIGH")

    /** Medium impact.
      */
    case object Medium extends GcpApphubDestinationServiceCriticalityTypeValue("MEDIUM")

    /** Low impact.
      */
    case object Low extends GcpApphubDestinationServiceCriticalityTypeValue("LOW")
  }

  /** Values for [[GcpApphubDestinationServiceEnvironmentType]].
    */
  abstract class GcpApphubDestinationServiceEnvironmentTypeValue(val value: String)
  object GcpApphubDestinationServiceEnvironmentTypeValue {
    implicit val attributeFromGcpApphubDestinationServiceEnvironmentTypeValue
        : Attribute.From[GcpApphubDestinationServiceEnvironmentTypeValue, String] = _.value

    /** Production environment.
      */
    case object Production extends GcpApphubDestinationServiceEnvironmentTypeValue("PRODUCTION")

    /** Staging environment.
      */
    case object Staging extends GcpApphubDestinationServiceEnvironmentTypeValue("STAGING")

    /** Test environment.
      */
    case object Test extends GcpApphubDestinationServiceEnvironmentTypeValue("TEST")

    /** Development environment.
      */
    case object Development extends GcpApphubDestinationServiceEnvironmentTypeValue("DEVELOPMENT")
  }

  /** Values for [[GcpApphubDestinationWorkloadCriticalityType]].
    */
  abstract class GcpApphubDestinationWorkloadCriticalityTypeValue(val value: String)
  object GcpApphubDestinationWorkloadCriticalityTypeValue {
    implicit val attributeFromGcpApphubDestinationWorkloadCriticalityTypeValue
        : Attribute.From[GcpApphubDestinationWorkloadCriticalityTypeValue, String] = _.value

    /** Mission critical service.
      */
    case object MissionCritical extends GcpApphubDestinationWorkloadCriticalityTypeValue("MISSION_CRITICAL")

    /** High impact.
      */
    case object High extends GcpApphubDestinationWorkloadCriticalityTypeValue("HIGH")

    /** Medium impact.
      */
    case object Medium extends GcpApphubDestinationWorkloadCriticalityTypeValue("MEDIUM")

    /** Low impact.
      */
    case object Low extends GcpApphubDestinationWorkloadCriticalityTypeValue("LOW")
  }

  /** Values for [[GcpApphubDestinationWorkloadEnvironmentType]].
    */
  abstract class GcpApphubDestinationWorkloadEnvironmentTypeValue(val value: String)
  object GcpApphubDestinationWorkloadEnvironmentTypeValue {
    implicit val attributeFromGcpApphubDestinationWorkloadEnvironmentTypeValue
        : Attribute.From[GcpApphubDestinationWorkloadEnvironmentTypeValue, String] = _.value

    /** Production environment.
      */
    case object Production extends GcpApphubDestinationWorkloadEnvironmentTypeValue("PRODUCTION")

    /** Staging environment.
      */
    case object Staging extends GcpApphubDestinationWorkloadEnvironmentTypeValue("STAGING")

    /** Test environment.
      */
    case object Test extends GcpApphubDestinationWorkloadEnvironmentTypeValue("TEST")

    /** Development environment.
      */
    case object Development extends GcpApphubDestinationWorkloadEnvironmentTypeValue("DEVELOPMENT")
  }

}
