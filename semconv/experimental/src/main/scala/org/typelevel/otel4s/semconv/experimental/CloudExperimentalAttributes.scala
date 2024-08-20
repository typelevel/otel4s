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
object CloudExperimentalAttributes {

  /**
  * The cloud account ID the resource is assigned to.
  */
  val CloudAccountId: AttributeKey[String] = string("cloud.account.id")

  /**
  * Cloud regions often have multiple, isolated locations known as zones to increase availability. Availability zone represents the zone where the resource is running.
  *
  * @note 
  *  - Availability zones are called &quot;zones&quot; on Alibaba Cloud and Google Cloud.
  */
  val CloudAvailabilityZone: AttributeKey[String] = string("cloud.availability_zone")

  /**
  * The cloud platform in use.
  *
  * @note 
  *  - The prefix of the service SHOULD match the one specified in `cloud.provider`.
  */
  val CloudPlatform: AttributeKey[String] = string("cloud.platform")

  /**
  * Name of the cloud provider.
  */
  val CloudProvider: AttributeKey[String] = string("cloud.provider")

  /**
  * The geographical region the resource is running.
  *
  * @note 
  *  - Refer to your provider's docs to see the available regions, for example <a href="https://www.alibabacloud.com/help/doc-detail/40654.htm">Alibaba Cloud regions</a>, <a href="https://aws.amazon.com/about-aws/global-infrastructure/regions_az/">AWS regions</a>, <a href="https://azure.microsoft.com/global-infrastructure/geographies/">Azure regions</a>, <a href="https://cloud.google.com/about/locations">Google Cloud regions</a>, or <a href="https://www.tencentcloud.com/document/product/213/6091">Tencent Cloud regions</a>.
  */
  val CloudRegion: AttributeKey[String] = string("cloud.region")

  /**
  * Cloud provider-specific native identifier of the monitored cloud resource (e.g. an <a href="https://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html">ARN</a> on AWS, a <a href="https://learn.microsoft.com/rest/api/resources/resources/get-by-id">fully qualified resource ID</a> on Azure, a <a href="https://cloud.google.com/apis/design/resource_names#full_resource_name">full resource name</a> on GCP)
  *
  * @note 
  *  - On some cloud providers, it may not be possible to determine the full ID at startup,
so it may be necessary to set `cloud.resource_id` as a span attribute instead.
  *  - The exact value to use for `cloud.resource_id` depends on the cloud provider.
The following well-known definitions MUST be used if you set this attribute and they apply:<li><strong>AWS Lambda:</strong> The function <a href="https://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html">ARN</a>.
Take care not to use the &quot;invoked ARN&quot; directly but replace any
<a href="https://docs.aws.amazon.com/lambda/latest/dg/configuration-aliases.html">alias suffix</a>
with the resolved function version, as the same runtime instance may be invocable with
multiple different aliases.</li>
<li><strong>GCP:</strong> The <a href="https://cloud.google.com/iam/docs/full-resource-names">URI of the resource</a></li>
<li><strong>Azure:</strong> The <a href="https://docs.microsoft.com/rest/api/resources/resources/get-by-id">Fully Qualified Resource ID</a> of the invoked function,
<em>not</em> the function app, having the form
`/subscriptions/<SUBSCIPTION_GUID>/resourceGroups/<RG>/providers/Microsoft.Web/sites/<FUNCAPP>/functions/<FUNC>`.
This means that a span attribute MUST be used, as an Azure function app can host multiple functions that would usually share
a TracerProvider.</li>

  */
  val CloudResourceId: AttributeKey[String] = string("cloud.resource_id")
  // Enum definitions
  
  /**
   * Values for [[CloudPlatform]].
   */
  abstract class CloudPlatformValue(val value: String)
  object CloudPlatformValue {
    /** Alibaba Cloud Elastic Compute Service. */
    case object AlibabaCloudEcs extends CloudPlatformValue("alibaba_cloud_ecs")
    /** Alibaba Cloud Function Compute. */
    case object AlibabaCloudFc extends CloudPlatformValue("alibaba_cloud_fc")
    /** Red Hat OpenShift on Alibaba Cloud. */
    case object AlibabaCloudOpenshift extends CloudPlatformValue("alibaba_cloud_openshift")
    /** AWS Elastic Compute Cloud. */
    case object AwsEc2 extends CloudPlatformValue("aws_ec2")
    /** AWS Elastic Container Service. */
    case object AwsEcs extends CloudPlatformValue("aws_ecs")
    /** AWS Elastic Kubernetes Service. */
    case object AwsEks extends CloudPlatformValue("aws_eks")
    /** AWS Lambda. */
    case object AwsLambda extends CloudPlatformValue("aws_lambda")
    /** AWS Elastic Beanstalk. */
    case object AwsElasticBeanstalk extends CloudPlatformValue("aws_elastic_beanstalk")
    /** AWS App Runner. */
    case object AwsAppRunner extends CloudPlatformValue("aws_app_runner")
    /** Red Hat OpenShift on AWS (ROSA). */
    case object AwsOpenshift extends CloudPlatformValue("aws_openshift")
    /** Azure Virtual Machines. */
    case object AzureVm extends CloudPlatformValue("azure_vm")
    /** Azure Container Apps. */
    case object AzureContainerApps extends CloudPlatformValue("azure_container_apps")
    /** Azure Container Instances. */
    case object AzureContainerInstances extends CloudPlatformValue("azure_container_instances")
    /** Azure Kubernetes Service. */
    case object AzureAks extends CloudPlatformValue("azure_aks")
    /** Azure Functions. */
    case object AzureFunctions extends CloudPlatformValue("azure_functions")
    /** Azure App Service. */
    case object AzureAppService extends CloudPlatformValue("azure_app_service")
    /** Azure Red Hat OpenShift. */
    case object AzureOpenshift extends CloudPlatformValue("azure_openshift")
    /** Google Bare Metal Solution (BMS). */
    case object GcpBareMetalSolution extends CloudPlatformValue("gcp_bare_metal_solution")
    /** Google Cloud Compute Engine (GCE). */
    case object GcpComputeEngine extends CloudPlatformValue("gcp_compute_engine")
    /** Google Cloud Run. */
    case object GcpCloudRun extends CloudPlatformValue("gcp_cloud_run")
    /** Google Cloud Kubernetes Engine (GKE). */
    case object GcpKubernetesEngine extends CloudPlatformValue("gcp_kubernetes_engine")
    /** Google Cloud Functions (GCF). */
    case object GcpCloudFunctions extends CloudPlatformValue("gcp_cloud_functions")
    /** Google Cloud App Engine (GAE). */
    case object GcpAppEngine extends CloudPlatformValue("gcp_app_engine")
    /** Red Hat OpenShift on Google Cloud. */
    case object GcpOpenshift extends CloudPlatformValue("gcp_openshift")
    /** Red Hat OpenShift on IBM Cloud. */
    case object IbmCloudOpenshift extends CloudPlatformValue("ibm_cloud_openshift")
    /** Tencent Cloud Cloud Virtual Machine (CVM). */
    case object TencentCloudCvm extends CloudPlatformValue("tencent_cloud_cvm")
    /** Tencent Cloud Elastic Kubernetes Service (EKS). */
    case object TencentCloudEks extends CloudPlatformValue("tencent_cloud_eks")
    /** Tencent Cloud Serverless Cloud Function (SCF). */
    case object TencentCloudScf extends CloudPlatformValue("tencent_cloud_scf")
  }
  /**
   * Values for [[CloudProvider]].
   */
  abstract class CloudProviderValue(val value: String)
  object CloudProviderValue {
    /** Alibaba Cloud. */
    case object AlibabaCloud extends CloudProviderValue("alibaba_cloud")
    /** Amazon Web Services. */
    case object Aws extends CloudProviderValue("aws")
    /** Microsoft Azure. */
    case object Azure extends CloudProviderValue("azure")
    /** Google Cloud Platform. */
    case object Gcp extends CloudProviderValue("gcp")
    /** Heroku Platform as a Service. */
    case object Heroku extends CloudProviderValue("heroku")
    /** IBM Cloud. */
    case object IbmCloud extends CloudProviderValue("ibm_cloud")
    /** Tencent Cloud. */
    case object TencentCloud extends CloudProviderValue("tencent_cloud")
  }

}