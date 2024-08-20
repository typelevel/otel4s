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
object ContainerExperimentalAttributes {

  /**
  * The command used to run the container (i.e. the command name).
  *
  * @note 
  *  - If using embedded credentials or sensitive data, it is recommended to remove them to prevent potential leakage.
  */
  val ContainerCommand: AttributeKey[String] = string("container.command")

  /**
  * All the command arguments (including the command/executable itself) run by the container. [2]
  */
  val ContainerCommandArgs: AttributeKey[Seq[String]] = stringSeq("container.command_args")

  /**
  * The full command run by the container as a single string representing the full command. [2]
  */
  val ContainerCommandLine: AttributeKey[String] = string("container.command_line")

  /**
  * Deprecated, use `cpu.mode` instead.
  */
  @deprecated("Use `cpu.mode` instead", "0.5.0")
  val ContainerCpuState: AttributeKey[String] = string("container.cpu.state")

  /**
  * Container ID. Usually a UUID, as for example used to <a href="https://docs.docker.com/engine/reference/run/#container-identification">identify Docker containers</a>. The UUID might be abbreviated.
  */
  val ContainerId: AttributeKey[String] = string("container.id")

  /**
  * Runtime specific image identifier. Usually a hash algorithm followed by a UUID.
  *
  * @note 
  *  - Docker defines a sha256 of the image id; `container.image.id` corresponds to the `Image` field from the Docker container inspect <a href="https://docs.docker.com/engine/api/v1.43/#tag/Container/operation/ContainerInspect">API</a> endpoint.
K8s defines a link to the container registry repository with digest `"imageID": "registry.azurecr.io /namespace/service/dockerfile@sha256:bdeabd40c3a8a492eaf9e8e44d0ebbb84bac7ee25ac0cf8a7159d25f62555625"`.
The ID is assigned by the container runtime and can vary in different environments. Consider using `oci.manifest.digest` if it is important to identify the same image in different environments/runtimes.
  */
  val ContainerImageId: AttributeKey[String] = string("container.image.id")

  /**
  * Name of the image the container was built on.
  */
  val ContainerImageName: AttributeKey[String] = string("container.image.name")

  /**
  * Repo digests of the container image as provided by the container runtime.
  *
  * @note 
  *  - <a href="https://docs.docker.com/engine/api/v1.43/#tag/Image/operation/ImageInspect">Docker</a> and <a href="https://github.com/kubernetes/cri-api/blob/c75ef5b473bbe2d0a4fc92f82235efd665ea8e9f/pkg/apis/runtime/v1/api.proto#L1237-L1238">CRI</a> report those under the `RepoDigests` field.
  */
  val ContainerImageRepoDigests: AttributeKey[Seq[String]] = stringSeq("container.image.repo_digests")

  /**
  * Container image tags. An example can be found in <a href="https://docs.docker.com/engine/api/v1.43/#tag/Image/operation/ImageInspect">Docker Image Inspect</a>. Should be only the `<tag>` section of the full name for example from `registry.example.com/my-org/my-image:<tag>`.
  */
  val ContainerImageTags: AttributeKey[Seq[String]] = stringSeq("container.image.tags")

  /**
  * Container labels, `<key>` being the label name, the value being the label value.
  */
  val ContainerLabel: AttributeKey[String] = string("container.label")

  /**
  * Deprecated, use `container.label` instead.
  */
  @deprecated("Use `container.label` instead", "0.5.0")
  val ContainerLabels: AttributeKey[String] = string("container.labels")

  /**
  * Container name used by container runtime.
  */
  val ContainerName: AttributeKey[String] = string("container.name")

  /**
  * The container runtime managing this container.
  */
  val ContainerRuntime: AttributeKey[String] = string("container.runtime")
  // Enum definitions
  
  /**
   * Values for [[ContainerCpuState]].
   */
  @deprecated("Use `cpu.mode` instead", "0.5.0")
  abstract class ContainerCpuStateValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object ContainerCpuStateValue {
    /** When tasks of the cgroup are in user mode (Linux). When all container processes are in user mode (Windows). */
    case object User extends ContainerCpuStateValue("user")
    /** When CPU is used by the system (host OS). */
    case object System extends ContainerCpuStateValue("system")
    /** When tasks of the cgroup are in kernel mode (Linux). When all container processes are in kernel mode (Windows). */
    case object Kernel extends ContainerCpuStateValue("kernel")
  }

}