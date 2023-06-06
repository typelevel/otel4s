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
  final val SchemaUrl = "https://opentelemetry.io/schemas/1.19.0"

  /** Array of brand name and version separated by a space
    *
    * <p>Notes: <ul> <li>This value is intended to be taken from the <a
    * href="https://wicg.github.io/ua-client-hints/#interface">UA client hints
    * API</a> ({@code navigator.userAgentData.brands}).</li> </ul>
    */
  val BrowserBrands: AttributeKey[List[String]] = stringList("browser.brands")

  /** The platform on which the browser is running
    *
    * <p>Notes: <ul> <li>This value is intended to be taken from the <a
    * href="https://wicg.github.io/ua-client-hints/#interface">UA client hints
    * API</a> ({@code navigator.userAgentData.platform}). If unavailable, the
    * legacy {@code navigator.platform} API SHOULD NOT be used instead and this
    * attribute SHOULD be left unset in order for the values to be consistent.
    * The list of possible values is defined in the <a
    * href="https://wicg.github.io/ua-client-hints/#sec-ch-ua-platform">W3C
    * User-Agent Client Hints specification</a>. Note that some (but not all) of
    * these values can overlap with values in the <a href="./os.md">{@code
    * os.type} and {@code os.name} attributes</a>. However, for consistency, the
    * values in the {@code browser.platform} attribute should capture the exact
    * value that the user agent provides.</li> </ul>
    */
  val BrowserPlatform: AttributeKey[String] = string("browser.platform")

  /** A boolean that is true if the browser is running on a mobile device
    *
    * <p>Notes: <ul> <li>This value is intended to be taken from the <a
    * href="https://wicg.github.io/ua-client-hints/#interface">UA client hints
    * API</a> ({@code navigator.userAgentData.mobile}). If unavailable, this
    * attribute SHOULD be left unset.</li> </ul>
    */
  val BrowserMobile: AttributeKey[Boolean] = boolean("browser.mobile")

  /** Preferred language of the user using the browser
    *
    * <p>Notes: <ul> <li>This value is intended to be taken from the Navigator
    * API {@code navigator.language}.</li> </ul>
    */
  val BrowserLanguage: AttributeKey[String] = string("browser.language")

  /** Name of the cloud provider.
    */
  val CloudProvider: AttributeKey[String] = string("cloud.provider")

  /** The cloud account ID the resource is assigned to.
    */
  val CloudAccountId: AttributeKey[String] = string("cloud.account.id")

  /** The geographical region the resource is running.
    *
    * <p>Notes: <ul> <li>Refer to your provider's docs to see the available
    * regions, for example <a
    * href="https://www.alibabacloud.com/help/doc-detail/40654.htm">Alibaba
    * Cloud regions</a>, <a
    * href="https://aws.amazon.com/about-aws/global-infrastructure/regions_az/">AWS
    * regions</a>, <a
    * href="https://azure.microsoft.com/en-us/global-infrastructure/geographies/">Azure
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
    * href="https://learn.microsoft.com/en-us/rest/api/resources/resources/get-by-id">fully
    * qualified resource ID</a> on Azure, a <a
    * href="https://cloud.google.com/apis/design/resource_names#full_resource_name">full
    * resource name</a> on GCP)
    *
    * <p>Notes: <ul> <li>On some cloud providers, it may not be possible to
    * determine the full ID at startup, so it may be necessary to set {@code
    * cloud.resource_id} as a span attribute instead.</li><li>The exact value to
    * use for {@code cloud.resource_id} depends on the cloud provider. The
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
    * href="https://docs.microsoft.com/en-us/rest/api/resources/resources/get-by-id">Fully
    * Qualified Resource ID</a> of the invoked function, <em>not</em> the
    * function app, having the form {@code
    * /subscriptions/<SUBSCIPTION_GUID>/resourceGroups/<RG>/providers/Microsoft.Web/sites/<FUNCAPP>/functions/<FUNC>}.
    * This means that a span attribute MUST be used, as an Azure function app
    * can host multiple functions that would usually share a
    * TracerProvider.</li> </ul>
    */
  val CloudResourceId: AttributeKey[String] = string("cloud.resource_id")

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
    * specified in {@code cloud.provider}.</li> </ul>
    */
  val CloudPlatform: AttributeKey[String] = string("cloud.platform")

  /** The Amazon Resource Name (ARN) of an <a
    * href="https://docs.aws.amazon.com/AmazonECS/latest/developerguide/ECS_instances.html">ECS
    * container instance</a>.
    */
  val AwsEcsContainerArn: AttributeKey[String] = string("aws.ecs.container.arn")

  /** The ARN of an <a
    * href="https://docs.aws.amazon.com/AmazonECS/latest/developerguide/clusters.html">ECS
    * cluster</a>.
    */
  val AwsEcsClusterArn: AttributeKey[String] = string("aws.ecs.cluster.arn")

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

  /** The name(s) of the AWS log group(s) an application is writing to.
    *
    * <p>Notes: <ul> <li>Multiple log groups must be supported for cases like
    * multi-container applications, where a single application has sidecar
    * containers, and each write to their own log group.</li> </ul>
    */
  val AwsLogGroupNames: AttributeKey[List[String]] = stringList(
    "aws.log.group.names"
  )

  /** The Amazon Resource Name(s) (ARN) of the AWS log group(s).
    *
    * <p>Notes: <ul> <li>See the <a
    * href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/iam-access-control-overview-cwl.html#CWL_ARN_Format">log
    * group ARN format documentation</a>.</li> </ul>
    */
  val AwsLogGroupArns: AttributeKey[List[String]] = stringList(
    "aws.log.group.arns"
  )

  /** The name(s) of the AWS log stream(s) an application is writing to.
    */
  val AwsLogStreamNames: AttributeKey[List[String]] = stringList(
    "aws.log.stream.names"
  )

  /** The ARN(s) of the AWS log stream(s).
    *
    * <p>Notes: <ul> <li>See the <a
    * href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/iam-access-control-overview-cwl.html#CWL_ARN_Format">log
    * stream ARN format documentation</a>. One log group can contain several log
    * streams, so these ARNs necessarily identify both a log group and a log
    * stream.</li> </ul>
    */
  val AwsLogStreamArns: AttributeKey[List[String]] = stringList(
    "aws.log.stream.arns"
  )

  /** Time and date the release was created
    */
  val HerokuReleaseCreationTimestamp: AttributeKey[String] = string(
    "heroku.release.creation_timestamp"
  )

  /** Commit hash for the current release
    */
  val HerokuReleaseCommit: AttributeKey[String] = string(
    "heroku.release.commit"
  )

  /** Unique identifier for the application
    */
  val HerokuAppId: AttributeKey[String] = string("heroku.app.id")

  /** Container name used by container runtime.
    */
  val ContainerName: AttributeKey[String] = string("container.name")

  /** Container ID. Usually a UUID, as for example used to <a
    * href="https://docs.docker.com/engine/reference/run/#container-identification">identify
    * Docker containers</a>. The UUID might be abbreviated.
    */
  val ContainerId: AttributeKey[String] = string("container.id")

  /** The container runtime managing this container.
    */
  val ContainerRuntime: AttributeKey[String] = string("container.runtime")

  /** Name of the image the container was built on.
    */
  val ContainerImageName: AttributeKey[String] = string("container.image.name")

  /** Container image tag.
    */
  val ContainerImageTag: AttributeKey[String] = string("container.image.tag")

  /** Name of the <a
    * href="https://en.wikipedia.org/wiki/Deployment_environment">deployment
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

  /** The name of the device manufacturer
    *
    * <p>Notes: <ul> <li>The Android OS provides this field via <a
    * href="https://developer.android.com/reference/android/os/Build#MANUFACTURER">Build</a>.
    * iOS apps SHOULD hardcode the value {@code Apple}.</li> </ul>
    */
  val DeviceManufacturer: AttributeKey[String] = string("device.manufacturer")

  /** The name of the single function that this runtime instance executes.
    *
    * <p>Notes: <ul> <li>This is the name of the function as configured/deployed
    * on the FaaS platform and is usually different from the name of the
    * callback function (which may be stored in the <a
    * href="../../trace/semantic_conventions/span-general.md#source-code-attributes">{@code
    * code.namespace}/{@code code.function}</a> span attributes).</li><li>For
    * some cloud providers, the above definition is ambiguous. The following
    * definition of function name MUST be used for this attribute (and
    * consequently the span name) for the listed cloud
    * providers/products:</li><li><strong>Azure:</strong> The full name {@code
    * <FUNCAPP>/<FUNC>}, i.e., function app name followed by a forward slash
    * followed by the function name (this form can also be seen in the resource
    * JSON for the function). This means that a span attribute MUST be used, as
    * an Azure function app can host multiple functions that would usually share
    * a TracerProvider (see also the {@code cloud.resource_id} attribute).</li>
    * </ul>
    */
  val FaasName: AttributeKey[String] = string("faas.name")

  /** The immutable version of the function being executed.
    *
    * <p>Notes: <ul> <li>Depending on the cloud provider and platform,
    * use:</li><li><strong>AWS Lambda:</strong> The <a
    * href="https://docs.aws.amazon.com/lambda/latest/dg/configuration-versions.html">function
    * version</a> (an integer represented as a decimal string).</li>
    * <li><strong>Google Cloud Run:</strong> The <a
    * href="https://cloud.google.com/run/docs/managing/revisions">revision</a>
    * (i.e., the function name plus the revision suffix).</li>
    * <li><strong>Google Cloud Functions:</strong> The value of the <a
    * href="https://cloud.google.com/functions/docs/env-var#runtime_environment_variables_set_automatically">{@code
    * K_REVISION} environment variable</a>.</li> <li><strong>Azure
    * Functions:</strong> Not applicable. Do not set this attribute.</li> </ul>
    */
  val FaasVersion: AttributeKey[String] = string("faas.version")

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
    * correctly. On AWS Lambda, the environment variable {@code
    * AWS_LAMBDA_FUNCTION_MEMORY_SIZE} provides this information (which must be
    * multiplied by 1,048,576).</li> </ul>
    */
  val FaasMaxMemory: AttributeKey[Long] = long("faas.max_memory")

  /** Unique host ID. For Cloud, this must be the instance_id assigned by the
    * cloud provider. For non-containerized systems, this should be the {@code
    * machine-id}. See the table below for the sources to use to determine the
    * {@code machine-id} based on operating system.
    */
  val HostId: AttributeKey[String] = string("host.id")

  /** Name of the host. On Unix systems, it may contain what the hostname
    * command returns, or the fully qualified hostname, or another name
    * specified by the user.
    */
  val HostName: AttributeKey[String] = string("host.name")

  /** Type of host. For Cloud, this must be the machine type.
    */
  val HostType: AttributeKey[String] = string("host.type")

  /** The CPU architecture the host system is running on.
    */
  val HostArch: AttributeKey[String] = string("host.arch")

  /** Name of the VM image or OS install the host was instantiated from.
    */
  val HostImageName: AttributeKey[String] = string("host.image.name")

  /** VM image ID. For Cloud, this value is from the provider.
    */
  val HostImageId: AttributeKey[String] = string("host.image.id")

  /** The version string of the VM image as defined in <a
    * href="README.md#version-attributes">Version Attributes</a>.
    */
  val HostImageVersion: AttributeKey[String] = string("host.image.version")

  /** The name of the cluster.
    */
  val K8sClusterName: AttributeKey[String] = string("k8s.cluster.name")

  /** The name of the Node.
    */
  val K8sNodeName: AttributeKey[String] = string("k8s.node.name")

  /** The UID of the Node.
    */
  val K8sNodeUid: AttributeKey[String] = string("k8s.node.uid")

  /** The name of the namespace that the pod is running in.
    */
  val K8sNamespaceName: AttributeKey[String] = string("k8s.namespace.name")

  /** The UID of the Pod.
    */
  val K8sPodUid: AttributeKey[String] = string("k8s.pod.uid")

  /** The name of the Pod.
    */
  val K8sPodName: AttributeKey[String] = string("k8s.pod.name")

  /** The name of the Container from Pod specification, must be unique within a
    * Pod. Container runtime usually uses different globally unique name ({@code
    * container.name}).
    */
  val K8sContainerName: AttributeKey[String] = string("k8s.container.name")

  /** Number of times the container was restarted. This attribute can be used to
    * identify a particular container (running or stopped) within a container
    * spec.
    */
  val K8sContainerRestartCount: AttributeKey[Long] = long(
    "k8s.container.restart_count"
  )

  /** The UID of the ReplicaSet.
    */
  val K8sReplicasetUid: AttributeKey[String] = string("k8s.replicaset.uid")

  /** The name of the ReplicaSet.
    */
  val K8sReplicasetName: AttributeKey[String] = string("k8s.replicaset.name")

  /** The UID of the Deployment.
    */
  val K8sDeploymentUid: AttributeKey[String] = string("k8s.deployment.uid")

  /** The name of the Deployment.
    */
  val K8sDeploymentName: AttributeKey[String] = string("k8s.deployment.name")

  /** The UID of the StatefulSet.
    */
  val K8sStatefulsetUid: AttributeKey[String] = string("k8s.statefulset.uid")

  /** The name of the StatefulSet.
    */
  val K8sStatefulsetName: AttributeKey[String] = string("k8s.statefulset.name")

  /** The UID of the DaemonSet.
    */
  val K8sDaemonsetUid: AttributeKey[String] = string("k8s.daemonset.uid")

  /** The name of the DaemonSet.
    */
  val K8sDaemonsetName: AttributeKey[String] = string("k8s.daemonset.name")

  /** The UID of the Job.
    */
  val K8sJobUid: AttributeKey[String] = string("k8s.job.uid")

  /** The name of the Job.
    */
  val K8sJobName: AttributeKey[String] = string("k8s.job.name")

  /** The UID of the CronJob.
    */
  val K8sCronjobUid: AttributeKey[String] = string("k8s.cronjob.uid")

  /** The name of the CronJob.
    */
  val K8sCronjobName: AttributeKey[String] = string("k8s.cronjob.name")

  /** The operating system type.
    */
  val OsType: AttributeKey[String] = string("os.type")

  /** Human readable (not intended to be parsed) OS version information, like
    * e.g. reported by {@code ver} or {@code lsb_release -a} commands.
    */
  val OsDescription: AttributeKey[String] = string("os.description")

  /** Human readable operating system name.
    */
  val OsName: AttributeKey[String] = string("os.name")

  /** The version string of the operating system as defined in <a
    * href="../../resource/semantic_conventions/README.md#version-attributes">Version
    * Attributes</a>.
    */
  val OsVersion: AttributeKey[String] = string("os.version")

  /** Process identifier (PID).
    */
  val ProcessPid: AttributeKey[Long] = long("process.pid")

  /** Parent Process identifier (PID).
    */
  val ProcessParentPid: AttributeKey[Long] = long("process.parent_pid")

  /** The name of the process executable. On Linux based systems, can be set to
    * the {@code Name} in {@code proc/[pid]/status}. On Windows, can be set to
    * the base name of {@code GetProcessImageFileNameW}.
    */
  val ProcessExecutableName: AttributeKey[String] = string(
    "process.executable.name"
  )

  /** The full path to the process executable. On Linux based systems, can be
    * set to the target of {@code proc/[pid]/exe}. On Windows, can be set to the
    * result of {@code GetProcessImageFileNameW}.
    */
  val ProcessExecutablePath: AttributeKey[String] = string(
    "process.executable.path"
  )

  /** The command used to launch the process (i.e. the command name). On Linux
    * based systems, can be set to the zeroth string in {@code
    * proc/[pid]/cmdline}. On Windows, can be set to the first parameter
    * extracted from {@code GetCommandLineW}.
    */
  val ProcessCommand: AttributeKey[String] = string("process.command")

  /** The full command used to launch the process as a single string
    * representing the full command. On Windows, can be set to the result of
    * {@code GetCommandLineW}. Do not set this if you have to assemble it just
    * for monitoring; use {@code process.command_args} instead.
    */
  val ProcessCommandLine: AttributeKey[String] = string("process.command_line")

  /** All the command arguments (including the command/executable itself) as
    * received by the process. On Linux-based systems (and some other Unixoid
    * systems supporting procfs), can be set according to the list of
    * null-delimited strings extracted from {@code proc/[pid]/cmdline}. For
    * libc-based executables, this would be the full argv vector passed to
    * {@code main}.
    */
  val ProcessCommandArgs: AttributeKey[List[String]] = stringList(
    "process.command_args"
  )

  /** The username of the user that owns the process.
    */
  val ProcessOwner: AttributeKey[String] = string("process.owner")

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

  /** An additional description about the runtime of the process, for example a
    * specific vendor customization of the runtime environment.
    */
  val ProcessRuntimeDescription: AttributeKey[String] = string(
    "process.runtime.description"
  )

  /** Logical name of the service.
    *
    * <p>Notes: <ul> <li>MUST be the same for all instances of horizontally
    * scaled services. If the value was not specified, SDKs MUST fallback to
    * {@code unknown_service:} concatenated with <a
    * href="process.md#process">{@code process.executable.name}</a>, e.g. {@code
    * unknown_service:bash}. If {@code process.executable.name} is not
    * available, the value MUST be set to {@code unknown_service}.</li> </ul>
    */
  val ServiceName: AttributeKey[String] = string("service.name")

  /** A namespace for {@code service.name}.
    *
    * <p>Notes: <ul> <li>A string value having a meaning that helps to
    * distinguish a group of services, for example the team name that owns a
    * group of services. {@code service.name} is expected to be unique within
    * the same namespace. If {@code service.namespace} is not specified in the
    * Resource then {@code service.name} is expected to be unique for all
    * services that have no explicit namespace defined (so the empty/unspecified
    * namespace is simply one more valid namespace). Zero-length namespace
    * string is assumed equal to unspecified namespace.</li> </ul>
    */
  val ServiceNamespace: AttributeKey[String] = string("service.namespace")

  /** The string ID of the service instance.
    *
    * <p>Notes: <ul> <li>MUST be unique for each instance of the same {@code
    * service.namespace,service.name} pair (in other words {@code
    * service.namespace,service.name,service.instance.id} triplet MUST be
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

  /** The version string of the service API or implementation.
    */
  val ServiceVersion: AttributeKey[String] = string("service.version")

  /** The name of the telemetry SDK as defined above.
    */
  val TelemetrySdkName: AttributeKey[String] = string("telemetry.sdk.name")

  /** The language of the telemetry SDK.
    */
  val TelemetrySdkLanguage: AttributeKey[String] = string(
    "telemetry.sdk.language"
  )

  /** The version string of the telemetry SDK.
    */
  val TelemetrySdkVersion: AttributeKey[String] = string(
    "telemetry.sdk.version"
  )

  /** The version string of the auto instrumentation agent, if used.
    */
  val TelemetryAutoVersion: AttributeKey[String] = string(
    "telemetry.auto.version"
  )

  /** The name of the web engine.
    */
  val WebengineName: AttributeKey[String] = string("webengine.name")

  /** The version of the web engine.
    */
  val WebengineVersion: AttributeKey[String] = string("webengine.version")

  /** Additional description of the web engine (e.g. detailed version and
    * edition information).
    */
  val WebengineDescription: AttributeKey[String] = string(
    "webengine.description"
  )

  /** The name of the instrumentation scope - ({@code InstrumentationScope.Name}
    * in OTLP).
    */
  val OtelScopeName: AttributeKey[String] = string("otel.scope.name")

  /** The version of the instrumentation scope - ({@code
    * InstrumentationScope.Version} in OTLP).
    */
  val OtelScopeVersion: AttributeKey[String] = string("otel.scope.version")

  /** Deprecated, use the {@code otel.scope.name} attribute.
    *
    * @deprecated
    *   Deprecated, use the `otel.scope.name` attribute.
    */
  @deprecated("Use the `otel.scope.name` attribute", "")
  val OtelLibraryName: AttributeKey[String] = string("otel.library.name")

  /** Deprecated, use the {@code otel.scope.version} attribute.
    *
    * @deprecated
    *   Deprecated, use the `otel.scope.version` attribute.
    */
  @deprecated("Use the `otel.scope.version` attribute", "")
  val OtelLibraryVersion: AttributeKey[String] = string("otel.library.version")

  // Enum definitions
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

    /** webjs. */
    case object Webjs extends TelemetrySdkLanguageValue("webjs")

    /** swift. */
    case object Swift extends TelemetrySdkLanguageValue("swift")

    /** scala. */
    case object Scala extends TelemetrySdkLanguageValue("scala")

  }

  /** Red Hat OpenShift on Google Cloud.
    * @deprecated
    *   This item has been removed as of 1.18.0 of the semantic conventions. Use
    *   {@link ResourceAttributes.CloudPlatform#GcpOpenshift} instead.
    */
  @deprecated("Use ResourceAttributes.CloudPlatform.GcpOpenshift instead", "")
  val GcpOpenshift = string("gcp_openshift")

  /** Full user-agent string provided by the browser
    *
    * <p>Notes:
    *
    * <ul> <li>The user-agent value SHOULD be provided only from browsers that
    * do not have a mechanism to retrieve brands and platform individually from
    * the User-Agent Client Hints API. To retrieve the value, the legacy {@code
    * navigator.userAgent} API can be used. </ul>
    * @deprecated
    *   This item has been renamed in 1.19.0 version of the semantic
    *   conventions. Use {@link
    *   org.typelevel.otel4s.semconv.trace.attributes.SemanticAttributes#UserAgentOriginal}
    *   instead.
    */
  @deprecated("Use SemanticAttributes.UserAgentOriginal instead", "")
  val BrowserUserAgent = string("browser.user_agent")

  /** The unique ID of the single function that this runtime instance executes.
    *
    * <p>Notes:
    *
    * <ul> <li>On some cloud providers, it may not be possible to determine the
    * full ID at startup, so consider setting {@code faas.id} as a span
    * attribute instead. <li>The exact value to use for {@code faas.id} depends
    * on the cloud provider: <li><strong>AWS Lambda:</strong> The function <a
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
    * function app, having the form {@code
    * /subscriptions/<SUBSCIPTION_GUID>/resourceGroups/<RG>/providers/Microsoft.Web/sites/<FUNCAPP>/functions/<FUNC>}.
    * This means that a span attribute MUST be used, as an Azure function app
    * can host multiple functions that would usually share a TracerProvider.
    * </ul>
    * @deprecated
    *   This item has been removed in 1.19.0 version of the semantic
    *   conventions. Use {@link ResourceAttributes#CloudResourceId} instead.
    */
  @deprecated("Use ResourceAttributes.CloudResourceId instead", "")
  val FaasId = string("faas.id")

}
