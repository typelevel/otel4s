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

package org.typelevel.otel4s.semconv.resource.attributes

import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.AttributeKey._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/semantic-convention/templates/SemanticAttributes.scala.j2
object ResourceAttributes {

  /** The URL of the OpenTelemetry schema for these keys and values.
    */
  final val SchemaUrl = "https://opentelemetry.io/schemas/1.23.1"

  /** The cloud account ID the resource is assigned to.
    */
  val CloudAccountId: AttributeKey[String] = string("cloud.account.id")

  /** Cloud regions often have multiple, isolated locations known as zones to
    * increase availability. Availability zone represents the zone where the
    * resource is running.
    *
    * <p>Notes: <ul> <li>Availability zones are called &quot;zones&quot; on
    * Alibaba Cloud and Google Cloud.</li> </ul>
    */
  val CloudAvailabilityZone: AttributeKey[String] = string(
    "cloud.availability_zone"
  )

  /** The cloud platform in use.
    *
    * <p>Notes: <ul> <li>The prefix of the service SHOULD match the one
    * specified in `cloud.provider`.</li> </ul>
    */
  val CloudPlatform: AttributeKey[String] = string("cloud.platform")

  /** Name of the cloud provider.
    */
  val CloudProvider: AttributeKey[String] = string("cloud.provider")

  /** The geographical region the resource is running.
    *
    * <p>Notes: <ul> <li>Refer to your provider's docs to see the available
    * regions, for example <a
    * href="https://www.alibabacloud.com/help/doc-detail/40654.htm">Alibaba
    * Cloud regions</a>, <a
    * href="https://aws.amazon.com/about-aws/global-infrastructure/regions_az/">AWS
    * regions</a>, <a
    * href="https://azure.microsoft.com/global-infrastructure/geographies/">Azure
    * regions</a>, <a href="https://cloud.google.com/about/locations">Google
    * Cloud regions</a>, or <a
    * href="https://www.tencentcloud.com/document/product/213/6091">Tencent
    * Cloud regions</a>.</li> </ul>
    */
  val CloudRegion: AttributeKey[String] = string("cloud.region")

  /** Cloud provider-specific native identifier of the monitored cloud resource
    * (e.g. an <a
    * href="https://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html">ARN</a>
    * on AWS, a <a
    * href="https://learn.microsoft.com/rest/api/resources/resources/get-by-id">fully
    * qualified resource ID</a> on Azure, a <a
    * href="https://cloud.google.com/apis/design/resource_names#full_resource_name">full
    * resource name</a> on GCP)
    *
    * <p>Notes: <ul> <li>On some cloud providers, it may not be possible to
    * determine the full ID at startup, so it may be necessary to set
    * `cloud.resource_id` as a span attribute instead.</li><li>The exact value
    * to use for `cloud.resource_id` depends on the cloud provider. The
    * following well-known definitions MUST be used if you set this attribute
    * and they apply:</li><li><strong>AWS Lambda:</strong> The function <a
    * href="https://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html">ARN</a>.
    * Take care not to use the &quot;invoked ARN&quot; directly but replace any
    * <a
    * href="https://docs.aws.amazon.com/lambda/latest/dg/configuration-aliases.html">alias
    * suffix</a> with the resolved function version, as the same runtime
    * instance may be invokable with multiple different aliases.</li>
    * <li><strong>GCP:</strong> The <a
    * href="https://cloud.google.com/iam/docs/full-resource-names">URI of the
    * resource</a></li> <li><strong>Azure:</strong> The <a
    * href="https://docs.microsoft.com/rest/api/resources/resources/get-by-id">Fully
    * Qualified Resource ID</a> of the invoked function, <em>not</em> the
    * function app, having the form
    * `/subscriptions/<SUBSCIPTION_GUID>/resourceGroups/<RG>/providers/Microsoft.Web/sites/<FUNCAPP>/functions/<FUNC>`.
    * This means that a span attribute MUST be used, as an Azure function app
    * can host multiple functions that would usually share a
    * TracerProvider.</li> </ul>
    */
  val CloudResourceId: AttributeKey[String] = string("cloud.resource_id")

  /** The command used to run the container (i.e. the command name).
    *
    * <p>Notes: <ul> <li>If using embedded credentials or sensitive data, it is
    * recommended to remove them to prevent potential leakage.</li> </ul>
    */
  val ContainerCommand: AttributeKey[String] = string("container.command")

  /** All the command arguments (including the command/executable itself) run by
    * the container. [2]
    */
  val ContainerCommandArgs: AttributeKey[Seq[String]] = stringSeq(
    "container.command_args"
  )

  /** The full command run by the container as a single string representing the
    * full command. [2]
    */
  val ContainerCommandLine: AttributeKey[String] = string(
    "container.command_line"
  )

  /** Container ID. Usually a UUID, as for example used to <a
    * href="https://docs.docker.com/engine/reference/run/#container-identification">identify
    * Docker containers</a>. The UUID might be abbreviated.
    */
  val ContainerId: AttributeKey[String] = string("container.id")

  /** Runtime specific image identifier. Usually a hash algorithm followed by a
    * UUID.
    *
    * <p>Notes: <ul> <li>Docker defines a sha256 of the image id;
    * `container.image.id` corresponds to the `Image` field from the Docker
    * container inspect <a
    * href="https://docs.docker.com/engine/api/v1.43/#tag/Container/operation/ContainerInspect">API</a>
    * endpoint. K8s defines a link to the container registry repository with
    * digest `"imageID": "registry.azurecr.io
    * /namespace/service/dockerfile@sha256:bdeabd40c3a8a492eaf9e8e44d0ebbb84bac7ee25ac0cf8a7159d25f62555625"`.
    * The ID is assinged by the container runtime and can vary in different
    * environments. Consider using `oci.manifest.digest` if it is important to
    * identify the same image in different environments/runtimes.</li> </ul>
    */
  val ContainerImageId: AttributeKey[String] = string("container.image.id")

  /** Name of the image the container was built on.
    */
  val ContainerImageName: AttributeKey[String] = string("container.image.name")

  /** Repo digests of the container image as provided by the container runtime.
    *
    * <p>Notes: <ul> <li><a
    * href="https://docs.docker.com/engine/api/v1.43/#tag/Image/operation/ImageInspect">Docker</a>
    * and <a
    * href="https://github.com/kubernetes/cri-api/blob/c75ef5b473bbe2d0a4fc92f82235efd665ea8e9f/pkg/apis/runtime/v1/api.proto#L1237-L1238">CRI</a>
    * report those under the `RepoDigests` field.</li> </ul>
    */
  val ContainerImageRepoDigests: AttributeKey[Seq[String]] = stringSeq(
    "container.image.repo_digests"
  )

  /** Container image tags. An example can be found in <a
    * href="https://docs.docker.com/engine/api/v1.43/#tag/Image/operation/ImageInspect">Docker
    * Image Inspect</a>. Should be only the `<tag>` section of the full name for
    * example from `registry.example.com/my-org/my-image:<tag>`.
    */
  val ContainerImageTags: AttributeKey[Seq[String]] = stringSeq(
    "container.image.tags"
  )

  /** Container name used by container runtime.
    */
  val ContainerName: AttributeKey[String] = string("container.name")

  /** The container runtime managing this container.
    */
  val ContainerRuntime: AttributeKey[String] = string("container.runtime")

  /** The digest of the OCI image manifest. For container images specifically is
    * the digest by which the container image is known.
    *
    * <p>Notes: <ul> <li>Follows <a
    * href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">OCI
    * Image Manifest Specification</a>, and specifically the <a
    * href="https://github.com/opencontainers/image-spec/blob/main/descriptor.md#digests">Digest
    * property</a>. An example can be found in <a
    * href="https://docs.docker.com/registry/spec/manifest-v2-2/#example-image-manifest">Example
    * Image Manifest</a>.</li> </ul>
    */
  val OciManifestDigest: AttributeKey[String] = string("oci.manifest.digest")

  /** Uniquely identifies the framework API revision offered by a version
    * (`os.version`) of the android operating system. More information can be
    * found <a
    * href="https://developer.android.com/guide/topics/manifest/uses-sdk-element#ApiLevels">here</a>.
    */
  val AndroidOsApiLevel: AttributeKey[String] = string("android.os.api_level")

  /** Array of brand name and version separated by a space
    *
    * <p>Notes: <ul> <li>This value is intended to be taken from the <a
    * href="https://wicg.github.io/ua-client-hints/#interface">UA client hints
    * API</a> (`navigator.userAgentData.brands`).</li> </ul>
    */
  val BrowserBrands: AttributeKey[Seq[String]] = stringSeq("browser.brands")

  /** Preferred language of the user using the browser
    *
    * <p>Notes: <ul> <li>This value is intended to be taken from the Navigator
    * API `navigator.language`.</li> </ul>
    */
  val BrowserLanguage: AttributeKey[String] = string("browser.language")

  /** A boolean that is true if the browser is running on a mobile device
    *
    * <p>Notes: <ul> <li>This value is intended to be taken from the <a
    * href="https://wicg.github.io/ua-client-hints/#interface">UA client hints
    * API</a> (`navigator.userAgentData.mobile`). If unavailable, this attribute
    * SHOULD be left unset.</li> </ul>
    */
  val BrowserMobile: AttributeKey[Boolean] = boolean("browser.mobile")

  /** The platform on which the browser is running
    *
    * <p>Notes: <ul> <li>This value is intended to be taken from the <a
    * href="https://wicg.github.io/ua-client-hints/#interface">UA client hints
    * API</a> (`navigator.userAgentData.platform`). If unavailable, the legacy
    * `navigator.platform` API SHOULD NOT be used instead and this attribute
    * SHOULD be left unset in order for the values to be consistent. The list of
    * possible values is defined in the <a
    * href="https://wicg.github.io/ua-client-hints/#sec-ch-ua-platform">W3C
    * User-Agent Client Hints specification</a>. Note that some (but not all) of
    * these values can overlap with values in the <a href="./os.md">`os.type`
    * and `os.name` attributes</a>. However, for consistency, the values in the
    * `browser.platform` attribute should capture the exact value that the user
    * agent provides.</li> </ul>
    */
  val BrowserPlatform: AttributeKey[String] = string("browser.platform")

  /** The ARN of an <a
    * href="https://docs.aws.amazon.com/AmazonECS/latest/developerguide/clusters.html">ECS
    * cluster</a>.
    */
  val AwsEcsClusterArn: AttributeKey[String] = string("aws.ecs.cluster.arn")

  /** The Amazon Resource Name (ARN) of an <a
    * href="https://docs.aws.amazon.com/AmazonECS/latest/developerguide/ECS_instances.html">ECS
    * container instance</a>.
    */
  val AwsEcsContainerArn: AttributeKey[String] = string("aws.ecs.container.arn")

  /** The <a
    * href="https://docs.aws.amazon.com/AmazonECS/latest/developerguide/launch_types.html">launch
    * type</a> for an ECS task.
    */
  val AwsEcsLaunchtype: AttributeKey[String] = string("aws.ecs.launchtype")

  /** The ARN of an <a
    * href="https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task_definitions.html">ECS
    * task definition</a>.
    */
  val AwsEcsTaskArn: AttributeKey[String] = string("aws.ecs.task.arn")

  /** The task definition family this task definition is a member of.
    */
  val AwsEcsTaskFamily: AttributeKey[String] = string("aws.ecs.task.family")

  /** The revision for this task definition.
    */
  val AwsEcsTaskRevision: AttributeKey[String] = string("aws.ecs.task.revision")

  /** The ARN of an EKS cluster.
    */
  val AwsEksClusterArn: AttributeKey[String] = string("aws.eks.cluster.arn")

  /** The Amazon Resource Name(s) (ARN) of the AWS log group(s).
    *
    * <p>Notes: <ul> <li>See the <a
    * href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/iam-access-control-overview-cwl.html#CWL_ARN_Format">log
    * group ARN format documentation</a>.</li> </ul>
    */
  val AwsLogGroupArns: AttributeKey[Seq[String]] = stringSeq(
    "aws.log.group.arns"
  )

  /** The name(s) of the AWS log group(s) an application is writing to.
    *
    * <p>Notes: <ul> <li>Multiple log groups must be supported for cases like
    * multi-container applications, where a single application has sidecar
    * containers, and each write to their own log group.</li> </ul>
    */
  val AwsLogGroupNames: AttributeKey[Seq[String]] = stringSeq(
    "aws.log.group.names"
  )

  /** The ARN(s) of the AWS log stream(s).
    *
    * <p>Notes: <ul> <li>See the <a
    * href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/iam-access-control-overview-cwl.html#CWL_ARN_Format">log
    * stream ARN format documentation</a>. One log group can contain several log
    * streams, so these ARNs necessarily identify both a log group and a log
    * stream.</li> </ul>
    */
  val AwsLogStreamArns: AttributeKey[Seq[String]] = stringSeq(
    "aws.log.stream.arns"
  )

  /** The name(s) of the AWS log stream(s) an application is writing to.
    */
  val AwsLogStreamNames: AttributeKey[Seq[String]] = stringSeq(
    "aws.log.stream.names"
  )

  /** The name of the Cloud Run <a
    * href="https://cloud.google.com/run/docs/managing/job-executions">execution</a>
    * being run for the Job, as set by the <a
    * href="https://cloud.google.com/run/docs/container-contract#jobs-env-vars">`CLOUD_RUN_EXECUTION`</a>
    * environment variable.
    */
  val GcpCloudRunJobExecution: AttributeKey[String] = string(
    "gcp.cloud_run.job.execution"
  )

  /** The index for a task within an execution as provided by the <a
    * href="https://cloud.google.com/run/docs/container-contract#jobs-env-vars">`CLOUD_RUN_TASK_INDEX`</a>
    * environment variable.
    */
  val GcpCloudRunJobTaskIndex: AttributeKey[Long] = long(
    "gcp.cloud_run.job.task_index"
  )

  /** The hostname of a GCE instance. This is the full value of the default or
    * <a
    * href="https://cloud.google.com/compute/docs/instances/custom-hostname-vm">custom
    * hostname</a>.
    */
  val GcpGceInstanceHostname: AttributeKey[String] = string(
    "gcp.gce.instance.hostname"
  )

  /** The instance name of a GCE instance. This is the value provided by
    * `host.name`, the visible name of the instance in the Cloud Console UI, and
    * the prefix for the default hostname of the instance as defined by the <a
    * href="https://cloud.google.com/compute/docs/internal-dns#instance-fully-qualified-domain-names">default
    * internal DNS name</a>.
    */
  val GcpGceInstanceName: AttributeKey[String] = string("gcp.gce.instance.name")

  /** Unique identifier for the application
    */
  val HerokuAppId: AttributeKey[String] = string("heroku.app.id")

  /** Commit hash for the current release
    */
  val HerokuReleaseCommit: AttributeKey[String] = string(
    "heroku.release.commit"
  )

  /** Time and date the release was created
    */
  val HerokuReleaseCreationTimestamp: AttributeKey[String] = string(
    "heroku.release.creation_timestamp"
  )

  /** Name of the <a
    * href="https://wikipedia.org/wiki/Deployment_environment">deployment
    * environment</a> (aka deployment tier).
    */
  val DeploymentEnvironment: AttributeKey[String] = string(
    "deployment.environment"
  )

  /** A unique identifier representing the device
    *
    * <p>Notes: <ul> <li>The device identifier MUST only be defined using the
    * values outlined below. This value is not an advertising identifier and
    * MUST NOT be used as such. On iOS (Swift or Objective-C), this value MUST
    * be equal to the <a
    * href="https://developer.apple.com/documentation/uikit/uidevice/1620059-identifierforvendor">vendor
    * identifier</a>. On Android (Java or Kotlin), this value MUST be equal to
    * the Firebase Installation ID or a globally unique UUID which is persisted
    * across sessions in your application. More information can be found <a
    * href="https://developer.android.com/training/articles/user-data-ids">here</a>
    * on best practices and exact implementation details. Caution should be
    * taken when storing personal data or anything which can identify a user.
    * GDPR and data protection laws may apply, ensure you do your own due
    * diligence.</li> </ul>
    */
  val DeviceId: AttributeKey[String] = string("device.id")

  /** The name of the device manufacturer
    *
    * <p>Notes: <ul> <li>The Android OS provides this field via <a
    * href="https://developer.android.com/reference/android/os/Build#MANUFACTURER">Build</a>.
    * iOS apps SHOULD hardcode the value `Apple`.</li> </ul>
    */
  val DeviceManufacturer: AttributeKey[String] = string("device.manufacturer")

  /** The model identifier for the device
    *
    * <p>Notes: <ul> <li>It's recommended this value represents a machine
    * readable version of the model identifier rather than the market or
    * consumer-friendly name of the device.</li> </ul>
    */
  val DeviceModelIdentifier: AttributeKey[String] = string(
    "device.model.identifier"
  )

  /** The marketing name for the device model
    *
    * <p>Notes: <ul> <li>It's recommended this value represents a human readable
    * version of the device model rather than a machine readable
    * alternative.</li> </ul>
    */
  val DeviceModelName: AttributeKey[String] = string("device.model.name")

  /** The execution environment ID as a string, that will be potentially reused
    * for other invocations to the same function/function version.
    *
    * <p>Notes: <ul> <li><strong>AWS Lambda:</strong> Use the (full) log stream
    * name.</li> </ul>
    */
  val FaasInstance: AttributeKey[String] = string("faas.instance")

  /** The amount of memory available to the serverless function converted to
    * Bytes.
    *
    * <p>Notes: <ul> <li>It's recommended to set this attribute since e.g. too
    * little memory can easily stop a Java AWS Lambda function from working
    * correctly. On AWS Lambda, the environment variable
    * `AWS_LAMBDA_FUNCTION_MEMORY_SIZE` provides this information (which must be
    * multiplied by 1,048,576).</li> </ul>
    */
  val FaasMaxMemory: AttributeKey[Long] = long("faas.max_memory")

  /** The name of the single function that this runtime instance executes.
    *
    * <p>Notes: <ul> <li>This is the name of the function as configured/deployed
    * on the FaaS platform and is usually different from the name of the
    * callback function (which may be stored in the <a
    * href="/docs/general/attributes.md#source-code-attributes">`code.namespace`/`code.function`</a>
    * span attributes).</li><li>For some cloud providers, the above definition
    * is ambiguous. The following definition of function name MUST be used for
    * this attribute (and consequently the span name) for the listed cloud
    * providers/products:</li><li><strong>Azure:</strong> The full name
    * `<FUNCAPP>/<FUNC>`, i.e., function app name followed by a forward slash
    * followed by the function name (this form can also be seen in the resource
    * JSON for the function). This means that a span attribute MUST be used, as
    * an Azure function app can host multiple functions that would usually share
    * a TracerProvider (see also the `cloud.resource_id` attribute).</li> </ul>
    */
  val FaasName: AttributeKey[String] = string("faas.name")

  /** The immutable version of the function being executed.
    *
    * <p>Notes: <ul> <li>Depending on the cloud provider and platform,
    * use:</li><li><strong>AWS Lambda:</strong> The <a
    * href="https://docs.aws.amazon.com/lambda/latest/dg/configuration-versions.html">function
    * version</a> (an integer represented as a decimal string).</li>
    * <li><strong>Google Cloud Run (Services):</strong> The <a
    * href="https://cloud.google.com/run/docs/managing/revisions">revision</a>
    * (i.e., the function name plus the revision suffix).</li>
    * <li><strong>Google Cloud Functions:</strong> The value of the <a
    * href="https://cloud.google.com/functions/docs/env-var#runtime_environment_variables_set_automatically">`K_REVISION`
    * environment variable</a>.</li> <li><strong>Azure Functions:</strong> Not
    * applicable. Do not set this attribute.</li> </ul>
    */
  val FaasVersion: AttributeKey[String] = string("faas.version")

  /** The CPU architecture the host system is running on.
    */
  val HostArch: AttributeKey[String] = string("host.arch")

  /** Unique host ID. For Cloud, this must be the instance_id assigned by the
    * cloud provider. For non-containerized systems, this should be the
    * `machine-id`. See the table below for the sources to use to determine the
    * `machine-id` based on operating system.
    */
  val HostId: AttributeKey[String] = string("host.id")

  /** VM image ID or host OS image ID. For Cloud, this value is from the
    * provider.
    */
  val HostImageId: AttributeKey[String] = string("host.image.id")

  /** Name of the VM image or OS install the host was instantiated from.
    */
  val HostImageName: AttributeKey[String] = string("host.image.name")

  /** The version string of the VM image or host OS as defined in <a
    * href="README.md#version-attributes">Version Attributes</a>.
    */
  val HostImageVersion: AttributeKey[String] = string("host.image.version")

  /** Available IP addresses of the host, excluding loopback interfaces.
    *
    * <p>Notes: <ul> <li>IPv4 Addresses MUST be specified in dotted-quad
    * notation. IPv6 addresses MUST be specified in the <a
    * href="https://www.rfc-editor.org/rfc/rfc5952.html">RFC 5952</a>
    * format.</li> </ul>
    */
  val HostIp: AttributeKey[Seq[String]] = stringSeq("host.ip")

  /** Available MAC addresses of the host, excluding loopback interfaces.
    *
    * <p>Notes: <ul> <li>MAC Addresses MUST be represented in <a
    * href="https://standards.ieee.org/wp-content/uploads/import/documents/tutorials/eui.pdf">IEEE
    * RA hexadecimal form</a>: as hyphen-separated octets in uppercase
    * hexadecimal form from most to least significant.</li> </ul>
    */
  val HostMac: AttributeKey[Seq[String]] = stringSeq("host.mac")

  /** Name of the host. On Unix systems, it may contain what the hostname
    * command returns, or the fully qualified hostname, or another name
    * specified by the user.
    */
  val HostName: AttributeKey[String] = string("host.name")

  /** Type of host. For Cloud, this must be the machine type.
    */
  val HostType: AttributeKey[String] = string("host.type")

  /** The amount of level 2 memory cache available to the processor (in Bytes).
    */
  val HostCpuCacheL2Size: AttributeKey[Long] = long("host.cpu.cache.l2.size")

  /** Numeric value specifying the family or generation of the CPU.
    */
  val HostCpuFamily: AttributeKey[Long] = long("host.cpu.family")

  /** Model identifier. It provides more granular information about the CPU,
    * distinguishing it from other CPUs within the same family.
    */
  val HostCpuModelId: AttributeKey[Long] = long("host.cpu.model.id")

  /** Model designation of the processor.
    */
  val HostCpuModelName: AttributeKey[String] = string("host.cpu.model.name")

  /** Stepping or core revisions.
    */
  val HostCpuStepping: AttributeKey[Long] = long("host.cpu.stepping")

  /** Processor manufacturer identifier. A maximum 12-character string.
    *
    * <p>Notes: <ul> <li><a href="https://wiki.osdev.org/CPUID">CPUID</a>
    * command returns the vendor ID string in EBX, EDX and ECX registers.
    * Writing these to memory in this order results in a 12-character
    * string.</li> </ul>
    */
  val HostCpuVendorId: AttributeKey[String] = string("host.cpu.vendor.id")

  /** The name of the cluster.
    */
  val K8sClusterName: AttributeKey[String] = string("k8s.cluster.name")

  /** A pseudo-ID for the cluster, set to the UID of the `kube-system`
    * namespace.
    *
    * <p>Notes: <ul> <li>K8s doesn't have support for obtaining a cluster ID. If
    * this is ever added, we will recommend collecting the `k8s.cluster.uid`
    * through the official APIs. In the meantime, we are able to use the `uid`
    * of the `kube-system` namespace as a proxy for cluster ID. Read on for the
    * rationale.</li><li>Every object created in a K8s cluster is assigned a
    * distinct UID. The `kube-system` namespace is used by Kubernetes itself and
    * will exist for the lifetime of the cluster. Using the `uid` of the
    * `kube-system` namespace is a reasonable proxy for the K8s ClusterID as it
    * will only change if the cluster is rebuilt. Furthermore, Kubernetes UIDs
    * are UUIDs as standardized by <a
    * href="https://www.itu.int/ITU-T/studygroups/com17/oid.html">ISO/IEC 9834-8
    * and ITU-T X.667</a>. Which states:</li><blockquote> <li>If generated
    * according to one of the mechanisms defined in Rec.</li></blockquote>
    * <li>ITU-T X.667 | ISO/IEC 9834-8, a UUID is either guaranteed to be
    * different from all other UUIDs generated before 3603 A.D., or is extremely
    * likely to be different (depending on the mechanism
    * chosen).</li><li>Therefore, UIDs between clusters should be extremely
    * unlikely to conflict.</li> </ul>
    */
  val K8sClusterUid: AttributeKey[String] = string("k8s.cluster.uid")

  /** The name of the Node.
    */
  val K8sNodeName: AttributeKey[String] = string("k8s.node.name")

  /** The UID of the Node.
    */
  val K8sNodeUid: AttributeKey[String] = string("k8s.node.uid")

  /** The name of the namespace that the pod is running in.
    */
  val K8sNamespaceName: AttributeKey[String] = string("k8s.namespace.name")

  /** The name of the Pod.
    */
  val K8sPodName: AttributeKey[String] = string("k8s.pod.name")

  /** The UID of the Pod.
    */
  val K8sPodUid: AttributeKey[String] = string("k8s.pod.uid")

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

  /** The name of the ReplicaSet.
    */
  val K8sReplicasetName: AttributeKey[String] = string("k8s.replicaset.name")

  /** The UID of the ReplicaSet.
    */
  val K8sReplicasetUid: AttributeKey[String] = string("k8s.replicaset.uid")

  /** The name of the Deployment.
    */
  val K8sDeploymentName: AttributeKey[String] = string("k8s.deployment.name")

  /** The UID of the Deployment.
    */
  val K8sDeploymentUid: AttributeKey[String] = string("k8s.deployment.uid")

  /** The name of the StatefulSet.
    */
  val K8sStatefulsetName: AttributeKey[String] = string("k8s.statefulset.name")

  /** The UID of the StatefulSet.
    */
  val K8sStatefulsetUid: AttributeKey[String] = string("k8s.statefulset.uid")

  /** The name of the DaemonSet.
    */
  val K8sDaemonsetName: AttributeKey[String] = string("k8s.daemonset.name")

  /** The UID of the DaemonSet.
    */
  val K8sDaemonsetUid: AttributeKey[String] = string("k8s.daemonset.uid")

  /** The name of the Job.
    */
  val K8sJobName: AttributeKey[String] = string("k8s.job.name")

  /** The UID of the Job.
    */
  val K8sJobUid: AttributeKey[String] = string("k8s.job.uid")

  /** The name of the CronJob.
    */
  val K8sCronjobName: AttributeKey[String] = string("k8s.cronjob.name")

  /** The UID of the CronJob.
    */
  val K8sCronjobUid: AttributeKey[String] = string("k8s.cronjob.uid")

  /** Unique identifier for a particular build or compilation of the operating
    * system.
    */
  val OsBuildId: AttributeKey[String] = string("os.build_id")

  /** Human readable (not intended to be parsed) OS version information, like
    * e.g. reported by `ver` or `lsb_release -a` commands.
    */
  val OsDescription: AttributeKey[String] = string("os.description")

  /** Human readable operating system name.
    */
  val OsName: AttributeKey[String] = string("os.name")

  /** The operating system type.
    */
  val OsType: AttributeKey[String] = string("os.type")

  /** The version string of the operating system as defined in <a
    * href="/docs/resource/README.md#version-attributes">Version Attributes</a>.
    */
  val OsVersion: AttributeKey[String] = string("os.version")

  /** The command used to launch the process (i.e. the command name). On Linux
    * based systems, can be set to the zeroth string in `proc/[pid]/cmdline`. On
    * Windows, can be set to the first parameter extracted from
    * `GetCommandLineW`.
    */
  val ProcessCommand: AttributeKey[String] = string("process.command")

  /** All the command arguments (including the command/executable itself) as
    * received by the process. On Linux-based systems (and some other Unixoid
    * systems supporting procfs), can be set according to the list of
    * null-delimited strings extracted from `proc/[pid]/cmdline`. For libc-based
    * executables, this would be the full argv vector passed to `main`.
    */
  val ProcessCommandArgs: AttributeKey[Seq[String]] = stringSeq(
    "process.command_args"
  )

  /** The full command used to launch the process as a single string
    * representing the full command. On Windows, can be set to the result of
    * `GetCommandLineW`. Do not set this if you have to assemble it just for
    * monitoring; use `process.command_args` instead.
    */
  val ProcessCommandLine: AttributeKey[String] = string("process.command_line")

  /** The name of the process executable. On Linux based systems, can be set to
    * the `Name` in `proc/[pid]/status`. On Windows, can be set to the base name
    * of `GetProcessImageFileNameW`.
    */
  val ProcessExecutableName: AttributeKey[String] = string(
    "process.executable.name"
  )

  /** The full path to the process executable. On Linux based systems, can be
    * set to the target of `proc/[pid]/exe`. On Windows, can be set to the
    * result of `GetProcessImageFileNameW`.
    */
  val ProcessExecutablePath: AttributeKey[String] = string(
    "process.executable.path"
  )

  /** The username of the user that owns the process.
    */
  val ProcessOwner: AttributeKey[String] = string("process.owner")

  /** Parent Process identifier (PID).
    */
  val ProcessParentPid: AttributeKey[Long] = long("process.parent_pid")

  /** Process identifier (PID).
    */
  val ProcessPid: AttributeKey[Long] = long("process.pid")

  /** An additional description about the runtime of the process, for example a
    * specific vendor customization of the runtime environment.
    */
  val ProcessRuntimeDescription: AttributeKey[String] = string(
    "process.runtime.description"
  )

  /** The name of the runtime of this process. For compiled native binaries,
    * this SHOULD be the name of the compiler.
    */
  val ProcessRuntimeName: AttributeKey[String] = string("process.runtime.name")

  /** The version of the runtime of this process, as returned by the runtime
    * without modification.
    */
  val ProcessRuntimeVersion: AttributeKey[String] = string(
    "process.runtime.version"
  )

  /** Logical name of the service.
    *
    * <p>Notes: <ul> <li>MUST be the same for all instances of horizontally
    * scaled services. If the value was not specified, SDKs MUST fallback to
    * `unknown_service:` concatenated with <a
    * href="process.md#process">`process.executable.name`</a>, e.g.
    * `unknown_service:bash`. If `process.executable.name` is not available, the
    * value MUST be set to `unknown_service`.</li> </ul>
    */
  val ServiceName: AttributeKey[String] = string("service.name")

  /** The version string of the service API or implementation. The format is not
    * defined by these conventions.
    */
  val ServiceVersion: AttributeKey[String] = string("service.version")

  /** The string ID of the service instance.
    *
    * <p>Notes: <ul> <li>MUST be unique for each instance of the same
    * `service.namespace,service.name` pair (in other words
    * `service.namespace,service.name,service.instance.id` triplet MUST be
    * globally unique). The ID helps to distinguish instances of the same
    * service that exist at the same time (e.g. instances of a horizontally
    * scaled service). It is preferable for the ID to be persistent and stay the
    * same for the lifetime of the service instance, however it is acceptable
    * that the ID is ephemeral and changes during important lifetime events for
    * the service (e.g. service restarts). If the service has no inherent unique
    * ID that can be used as the value of this attribute it is recommended to
    * generate a random Version 1 or Version 4 RFC 4122 UUID (services aiming
    * for reproducible UUIDs may also use Version 5, see RFC 4122 for more
    * recommendations).</li> </ul>
    */
  val ServiceInstanceId: AttributeKey[String] = string("service.instance.id")

  /** A namespace for `service.name`.
    *
    * <p>Notes: <ul> <li>A string value having a meaning that helps to
    * distinguish a group of services, for example the team name that owns a
    * group of services. `service.name` is expected to be unique within the same
    * namespace. If `service.namespace` is not specified in the Resource then
    * `service.name` is expected to be unique for all services that have no
    * explicit namespace defined (so the empty/unspecified namespace is simply
    * one more valid namespace). Zero-length namespace string is assumed equal
    * to unspecified namespace.</li> </ul>
    */
  val ServiceNamespace: AttributeKey[String] = string("service.namespace")

  /** The language of the telemetry SDK.
    */
  val TelemetrySdkLanguage: AttributeKey[String] = string(
    "telemetry.sdk.language"
  )

  /** The name of the telemetry SDK as defined above.
    *
    * <p>Notes: <ul> <li>The OpenTelemetry SDK MUST set the `telemetry.sdk.name`
    * attribute to `opentelemetry`. If another SDK, like a fork or a
    * vendor-provided implementation, is used, this SDK MUST set the
    * `telemetry.sdk.name` attribute to the fully-qualified class or module name
    * of this SDK's main entry point or another suitable identifier depending on
    * the language. The identifier `opentelemetry` is reserved and MUST NOT be
    * used in this case. All custom identifiers SHOULD be stable across
    * different versions of an implementation.</li> </ul>
    */
  val TelemetrySdkName: AttributeKey[String] = string("telemetry.sdk.name")

  /** The version string of the telemetry SDK.
    */
  val TelemetrySdkVersion: AttributeKey[String] = string(
    "telemetry.sdk.version"
  )

  /** The name of the auto instrumentation agent or distribution, if used.
    *
    * <p>Notes: <ul> <li>Official auto instrumentation agents and distributions
    * SHOULD set the `telemetry.distro.name` attribute to a string starting with
    * `opentelemetry-`, e.g. `opentelemetry-java-instrumentation`.</li> </ul>
    */
  val TelemetryDistroName: AttributeKey[String] = string(
    "telemetry.distro.name"
  )

  /** The version string of the auto instrumentation agent or distribution, if
    * used.
    */
  val TelemetryDistroVersion: AttributeKey[String] = string(
    "telemetry.distro.version"
  )

  /** Additional description of the web engine (e.g. detailed version and
    * edition information).
    */
  val WebengineDescription: AttributeKey[String] = string(
    "webengine.description"
  )

  /** The name of the web engine.
    */
  val WebengineName: AttributeKey[String] = string("webengine.name")

  /** The version of the web engine.
    */
  val WebengineVersion: AttributeKey[String] = string("webengine.version")

  /** The name of the instrumentation scope - (`InstrumentationScope.Name` in
    * OTLP).
    */
  val OtelScopeName: AttributeKey[String] = string("otel.scope.name")

  /** The version of the instrumentation scope - (`InstrumentationScope.Version`
    * in OTLP).
    */
  val OtelScopeVersion: AttributeKey[String] = string("otel.scope.version")

  /** Deprecated, use the `otel.scope.name` attribute.
    */
  @deprecated("Use the `otel.scope.name` attribute", "0.3.0")
  val OtelLibraryName: AttributeKey[String] = string("otel.library.name")

  /** Deprecated, use the `otel.scope.version` attribute.
    */
  @deprecated("Use the `otel.scope.version` attribute", "0.3.0")
  val OtelLibraryVersion: AttributeKey[String] = string("otel.library.version")

  // Enum definitions
  abstract class CloudPlatformValue(val value: String)
  object CloudPlatformValue {

    /** Alibaba Cloud Elastic Compute Service. */
    case object AlibabaCloudEcs extends CloudPlatformValue("alibaba_cloud_ecs")

    /** Alibaba Cloud Function Compute. */
    case object AlibabaCloudFc extends CloudPlatformValue("alibaba_cloud_fc")

    /** Red Hat OpenShift on Alibaba Cloud. */
    case object AlibabaCloudOpenshift
        extends CloudPlatformValue("alibaba_cloud_openshift")

    /** AWS Elastic Compute Cloud. */
    case object AwsEc2 extends CloudPlatformValue("aws_ec2")

    /** AWS Elastic Container Service. */
    case object AwsEcs extends CloudPlatformValue("aws_ecs")

    /** AWS Elastic Kubernetes Service. */
    case object AwsEks extends CloudPlatformValue("aws_eks")

    /** AWS Lambda. */
    case object AwsLambda extends CloudPlatformValue("aws_lambda")

    /** AWS Elastic Beanstalk. */
    case object AwsElasticBeanstalk
        extends CloudPlatformValue("aws_elastic_beanstalk")

    /** AWS App Runner. */
    case object AwsAppRunner extends CloudPlatformValue("aws_app_runner")

    /** Red Hat OpenShift on AWS (ROSA). */
    case object AwsOpenshift extends CloudPlatformValue("aws_openshift")

    /** Azure Virtual Machines. */
    case object AzureVm extends CloudPlatformValue("azure_vm")

    /** Azure Container Instances. */
    case object AzureContainerInstances
        extends CloudPlatformValue("azure_container_instances")

    /** Azure Kubernetes Service. */
    case object AzureAks extends CloudPlatformValue("azure_aks")

    /** Azure Functions. */
    case object AzureFunctions extends CloudPlatformValue("azure_functions")

    /** Azure App Service. */
    case object AzureAppService extends CloudPlatformValue("azure_app_service")

    /** Azure Red Hat OpenShift. */
    case object AzureOpenshift extends CloudPlatformValue("azure_openshift")

    /** Google Bare Metal Solution (BMS). */
    case object GcpBareMetalSolution
        extends CloudPlatformValue("gcp_bare_metal_solution")

    /** Google Cloud Compute Engine (GCE). */
    case object GcpComputeEngine
        extends CloudPlatformValue("gcp_compute_engine")

    /** Google Cloud Run. */
    case object GcpCloudRun extends CloudPlatformValue("gcp_cloud_run")

    /** Google Cloud Kubernetes Engine (GKE). */
    case object GcpKubernetesEngine
        extends CloudPlatformValue("gcp_kubernetes_engine")

    /** Google Cloud Functions (GCF). */
    case object GcpCloudFunctions
        extends CloudPlatformValue("gcp_cloud_functions")

    /** Google Cloud App Engine (GAE). */
    case object GcpAppEngine extends CloudPlatformValue("gcp_app_engine")

    /** Red Hat OpenShift on Google Cloud. */
    case object GcpOpenshift extends CloudPlatformValue("gcp_openshift")

    /** Red Hat OpenShift on IBM Cloud. */
    case object IbmCloudOpenshift
        extends CloudPlatformValue("ibm_cloud_openshift")

    /** Tencent Cloud Cloud Virtual Machine (CVM). */
    case object TencentCloudCvm extends CloudPlatformValue("tencent_cloud_cvm")

    /** Tencent Cloud Elastic Kubernetes Service (EKS). */
    case object TencentCloudEks extends CloudPlatformValue("tencent_cloud_eks")

    /** Tencent Cloud Serverless Cloud Function (SCF). */
    case object TencentCloudScf extends CloudPlatformValue("tencent_cloud_scf")

  }

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

  abstract class AwsEcsLaunchtypeValue(val value: String)
  object AwsEcsLaunchtypeValue {

    /** ec2. */
    case object Ec2 extends AwsEcsLaunchtypeValue("ec2")

    /** fargate. */
    case object Fargate extends AwsEcsLaunchtypeValue("fargate")

  }

  abstract class HostArchValue(val value: String)
  object HostArchValue {

    /** AMD64. */
    case object Amd64 extends HostArchValue("amd64")

    /** ARM32. */
    case object Arm32 extends HostArchValue("arm32")

    /** ARM64. */
    case object Arm64 extends HostArchValue("arm64")

    /** Itanium. */
    case object Ia64 extends HostArchValue("ia64")

    /** 32-bit PowerPC. */
    case object Ppc32 extends HostArchValue("ppc32")

    /** 64-bit PowerPC. */
    case object Ppc64 extends HostArchValue("ppc64")

    /** IBM z/Architecture. */
    case object S390x extends HostArchValue("s390x")

    /** 32-bit x86. */
    case object X86 extends HostArchValue("x86")

  }

  abstract class OsTypeValue(val value: String)
  object OsTypeValue {

    /** Microsoft Windows. */
    case object Windows extends OsTypeValue("windows")

    /** Linux. */
    case object Linux extends OsTypeValue("linux")

    /** Apple Darwin. */
    case object Darwin extends OsTypeValue("darwin")

    /** FreeBSD. */
    case object Freebsd extends OsTypeValue("freebsd")

    /** NetBSD. */
    case object Netbsd extends OsTypeValue("netbsd")

    /** OpenBSD. */
    case object Openbsd extends OsTypeValue("openbsd")

    /** DragonFly BSD. */
    case object Dragonflybsd extends OsTypeValue("dragonflybsd")

    /** HP-UX (Hewlett Packard Unix). */
    case object Hpux extends OsTypeValue("hpux")

    /** AIX (Advanced Interactive eXecutive). */
    case object Aix extends OsTypeValue("aix")

    /** SunOS, Oracle Solaris. */
    case object Solaris extends OsTypeValue("solaris")

    /** IBM z/OS. */
    case object ZOs extends OsTypeValue("z_os")

  }

  abstract class TelemetrySdkLanguageValue(val value: String)
  object TelemetrySdkLanguageValue {

    /** cpp. */
    case object Cpp extends TelemetrySdkLanguageValue("cpp")

    /** dotnet. */
    case object Dotnet extends TelemetrySdkLanguageValue("dotnet")

    /** erlang. */
    case object Erlang extends TelemetrySdkLanguageValue("erlang")

    /** go. */
    case object Go extends TelemetrySdkLanguageValue("go")

    /** java. */
    case object Java extends TelemetrySdkLanguageValue("java")

    /** nodejs. */
    case object Nodejs extends TelemetrySdkLanguageValue("nodejs")

    /** php. */
    case object Php extends TelemetrySdkLanguageValue("php")

    /** python. */
    case object Python extends TelemetrySdkLanguageValue("python")

    /** ruby. */
    case object Ruby extends TelemetrySdkLanguageValue("ruby")

    /** rust. */
    case object Rust extends TelemetrySdkLanguageValue("rust")

    /** swift. */
    case object Swift extends TelemetrySdkLanguageValue("swift")

    /** webjs. */
    case object Webjs extends TelemetrySdkLanguageValue("webjs")

    /** scala. */
    case object Scala extends TelemetrySdkLanguageValue("scala")

  }

  /** Red Hat OpenShift on Google Cloud.
    * @deprecated
    *   This item has been removed as of 1.18.0 of the semantic conventions. Use
    *   [[org.typelevel.otel4s.semconv.resource.attributes.ResourceAttributes.CloudPlatform.GcpOpenshift ResourceAttributes.CloudPlatform.GcpOpenshift]]
    *   instead.
    */
  @deprecated(
    "Use ResourceAttributes.CloudPlatform.GcpOpenshift instead",
    "0.3.0"
  )
  val GcpOpenshift = string("gcp_openshift")

  /** Full user-agent string provided by the browser
    *
    * <p>Notes:
    *
    * <ul> <li>The user-agent value SHOULD be provided only from browsers that
    * do not have a mechanism to retrieve brands and platform individually from
    * the User-Agent Client Hints API. To retrieve the value, the legacy
    * `navigator.userAgent` API can be used. </ul>
    * @deprecated
    *   This item has been renamed in 1.19.0 version of the semantic
    *   conventions. Use
    *   [[org.typelevel.otel4s.semconv.trace.attributes.SemanticAttributes.UserAgentOriginal]]
    *   instead.
    */
  @deprecated("Use SemanticAttributes.UserAgentOriginal instead", "0.3.0")
  val BrowserUserAgent = string("browser.user_agent")

  /** The unique ID of the single function that this runtime instance executes.
    *
    * <p>Notes:
    *
    * <ul> <li>On some cloud providers, it may not be possible to determine the
    * full ID at startup, so consider setting `faas.id` as a span attribute
    * instead. <li>The exact value to use for `faas.id` depends on the cloud
    * provider: <li><strong>AWS Lambda:</strong> The function <a
    * href="https://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html">ARN</a>.
    * Take care not to use the &quot;invoked ARN&quot; directly but replace any
    * <a
    * href="https://docs.aws.amazon.com/lambda/latest/dg/configuration-aliases.html">alias
    * suffix</a> with the resolved function version, as the same runtime
    * instance may be invokable with multiple different aliases.
    * <li><strong>GCP:</strong> The <a
    * href="https://cloud.google.com/iam/docs/full-resource-names">URI of the
    * resource</a> <li><strong>Azure:</strong> The <a
    * href="https://docs.microsoft.com/en-us/rest/api/resources/resources/get-by-id">Fully
    * Qualified Resource ID</a> of the invoked function, <em>not</em> the
    * function app, having the form
    * `/subscriptions/<SUBSCIPTION_GUID>/resourceGroups/<RG>/providers/Microsoft.Web/sites/<FUNCAPP>/functions/<FUNC>}`.
    * This means that a span attribute MUST be used, as an Azure function app
    * can host multiple functions that would usually share a TracerProvider.
    * </ul>
    * @deprecated
    *   This item has been removed in 1.19.0 version of the semantic
    *   conventions. Use [[ResourceAttributes.CloudResourceId]] instead.
    */
  @deprecated("Use ResourceAttributes.CloudResourceId instead", "0.3.0")
  val FaasId = string("faas.id")

  /** The version string of the auto instrumentation agent, if used.
    *
    * @deprecated
    *   This item has been renamed in 1.22.0 of the semantic conventions. Use
    *   [[ResourceAttributes.TelemetryDistroVersion]] instead.
    */
  @deprecated("Use ResourceAttributes.TelemetryDistroVersion instead", "0.4.0")
  val TelemetryAutoVersion = string("telemetry.auto.version")

  /** Container image tag.
    *
    * @deprecated
    *   This item has been renamed in 1.22.0 of the semantic conventions. Use
    *   [[ResourceAttributes.ContainerImageTags]] instead.
    */
  @deprecated("Use ResourceAttributes.ContainerImageTags instead", "0.4.0")
  val ContainerImageTag = string("container.image.tag")

}
