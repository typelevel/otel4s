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
object ContainerAttributes {

  /** Container ID. Usually a UUID, as for example used to <a
    * href="https://docs.docker.com/engine/containers/run/#container-identification">identify Docker containers</a>. The
    * UUID might be abbreviated.
    */
  val ContainerId: AttributeKey[String] =
    AttributeKey("container.id")

  /** Name of the image the container was built on.
    */
  val ContainerImageName: AttributeKey[String] =
    AttributeKey("container.image.name")

  /** Repo digests of the container image as provided by the container runtime.
    *
    * @note
    *   <p> <a
    *   href="https://docs.docker.com/reference/api/engine/version/v1.52/#tag/Image/operation/ImageInspect">Docker</a>
    *   and <a
    *   href="https://github.com/kubernetes/cri-api/blob/c75ef5b473bbe2d0a4fc92f82235efd665ea8e9f/pkg/apis/runtime/v1/api.proto#L1237-L1238">CRI</a>
    *   report those under the `RepoDigests` field.
    */
  val ContainerImageRepoDigests: AttributeKey[Seq[String]] =
    AttributeKey("container.image.repo_digests")

  /** Container image tags. An example can be found in <a
    * href="https://docs.docker.com/reference/api/engine/version/v1.52/#tag/Image/operation/ImageInspect">Docker Image
    * Inspect</a>. Should be only the `<tag>` section of the full name for example from
    * `registry.example.com/my-org/my-image:<tag>`.
    */
  val ContainerImageTags: AttributeKey[Seq[String]] =
    AttributeKey("container.image.tags")

}
