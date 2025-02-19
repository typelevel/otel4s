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
object ContainerExperimentalAttributes {

  /** The command used to run the container (i.e. the command name). <p>
    * @note
    *   <p> If using embedded credentials or sensitive data, it is recommended to remove them to prevent potential
    *   leakage.
    */
  val ContainerCommand: AttributeKey[String] =
    AttributeKey("container.command")

  /** All the command arguments (including the command/executable itself) run by the container.
    */
  val ContainerCommandArgs: AttributeKey[Seq[String]] =
    AttributeKey("container.command_args")

  /** The full command run by the container as a single string representing the full command.
    */
  val ContainerCommandLine: AttributeKey[String] =
    AttributeKey("container.command_line")

  /** Deprecated, use `cpu.mode` instead.
    */
  @deprecated("Replaced by `cpu.mode`", "")
  val ContainerCpuState: AttributeKey[String] =
    AttributeKey("container.cpu.state")

  /** The name of the CSI (<a href="https://github.com/container-storage-interface/spec">Container Storage
    * Interface</a>) plugin used by the volume. <p>
    * @note
    *   <p> This can sometimes be referred to as a "driver" in CSI implementations. This should represent the `name`
    *   field of the GetPluginInfo RPC.
    */
  val ContainerCsiPluginName: AttributeKey[String] =
    AttributeKey("container.csi.plugin.name")

  /** The unique volume ID returned by the CSI (<a href="https://github.com/container-storage-interface/spec">Container
    * Storage Interface</a>) plugin. <p>
    * @note
    *   <p> This can sometimes be referred to as a "volume handle" in CSI implementations. This should represent the
    *   `Volume.volume_id` field in CSI spec.
    */
  val ContainerCsiVolumeId: AttributeKey[String] =
    AttributeKey("container.csi.volume.id")

  /** Container ID. Usually a UUID, as for example used to <a
    * href="https://docs.docker.com/engine/containers/run/#container-identification">identify Docker containers</a>. The
    * UUID might be abbreviated.
    */
  val ContainerId: AttributeKey[String] =
    AttributeKey("container.id")

  /** Runtime specific image identifier. Usually a hash algorithm followed by a UUID. <p>
    * @note
    *   <p> Docker defines a sha256 of the image id; `container.image.id` corresponds to the `Image` field from the
    *   Docker container inspect <a
    *   href="https://docs.docker.com/engine/api/v1.43/#tag/Container/operation/ContainerInspect">API</a> endpoint. K8s
    *   defines a link to the container registry repository with digest
    *   `"imageID": "registry.azurecr.io /namespace/service/dockerfile@sha256:bdeabd40c3a8a492eaf9e8e44d0ebbb84bac7ee25ac0cf8a7159d25f62555625"`.
    *   The ID is assigned by the container runtime and can vary in different environments. Consider using
    *   `oci.manifest.digest` if it is important to identify the same image in different environments/runtimes.
    */
  val ContainerImageId: AttributeKey[String] =
    AttributeKey("container.image.id")

  /** Name of the image the container was built on.
    */
  val ContainerImageName: AttributeKey[String] =
    AttributeKey("container.image.name")

  /** Repo digests of the container image as provided by the container runtime. <p>
    * @note
    *   <p> <a href="https://docs.docker.com/engine/api/v1.43/#tag/Image/operation/ImageInspect">Docker</a> and <a
    *   href="https://github.com/kubernetes/cri-api/blob/c75ef5b473bbe2d0a4fc92f82235efd665ea8e9f/pkg/apis/runtime/v1/api.proto#L1237-L1238">CRI</a>
    *   report those under the `RepoDigests` field.
    */
  val ContainerImageRepoDigests: AttributeKey[Seq[String]] =
    AttributeKey("container.image.repo_digests")

  /** Container image tags. An example can be found in <a
    * href="https://docs.docker.com/engine/api/v1.43/#tag/Image/operation/ImageInspect">Docker Image Inspect</a>. Should
    * be only the `<tag>` section of the full name for example from `registry.example.com/my-org/my-image:<tag>`.
    */
  val ContainerImageTags: AttributeKey[Seq[String]] =
    AttributeKey("container.image.tags")

  /** Container labels, `<key>` being the label name, the value being the label value.
    */
  val ContainerLabel: AttributeKey[String] =
    AttributeKey("container.label")

  /** Deprecated, use `container.label` instead.
    */
  @deprecated("Replaced by `container.label`.", "")
  val ContainerLabels: AttributeKey[String] =
    AttributeKey("container.labels")

  /** Container name used by container runtime.
    */
  val ContainerName: AttributeKey[String] =
    AttributeKey("container.name")

  /** The container runtime managing this container.
    */
  val ContainerRuntime: AttributeKey[String] =
    AttributeKey("container.runtime")

  /** Values for [[ContainerCpuState]].
    */
  @deprecated("Replaced by `cpu.mode`", "")
  abstract class ContainerCpuStateValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object ContainerCpuStateValue {

    /** When tasks of the cgroup are in user mode (Linux). When all container processes are in user mode (Windows).
      */
    case object User extends ContainerCpuStateValue("user")

    /** When CPU is used by the system (host OS)
      */
    case object System extends ContainerCpuStateValue("system")

    /** When tasks of the cgroup are in kernel mode (Linux). When all container processes are in kernel mode (Windows).
      */
    case object Kernel extends ContainerCpuStateValue("kernel")
  }

}
