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
object ServiceExperimentalAttributes {

  /** The string ID of the service instance.
    *
    * @note
    *   - MUST be unique for each instance of the same
    *     `service.namespace,service.name` pair (in other words
    *     `service.namespace,service.name,service.instance.id` triplet MUST be
    *     globally unique). The ID helps to distinguish instances of the same
    *     service that exist at the same time (e.g. instances of a horizontally
    *     scaled service). It is preferable for the ID to be persistent and stay
    *     the same for the lifetime of the service instance, however it is
    *     acceptable that the ID is ephemeral and changes during important
    *     lifetime events for the service (e.g. service restarts). If the
    *     service has no inherent unique ID that can be used as the value of
    *     this attribute it is recommended to generate a random Version 1 or
    *     Version 4 RFC 4122 UUID (services aiming for reproducible UUIDs may
    *     also use Version 5, see RFC 4122 for more recommendations).
    */
  val ServiceInstanceId: AttributeKey[String] = string("service.instance.id")

  /** Logical name of the service.
    *
    * @note
    *   - MUST be the same for all instances of horizontally scaled services. If
    *     the value was not specified, SDKs MUST fallback to `unknown_service:`
    *     concatenated with <a
    *     href="process.md#process">`process.executable.name`</a>, e.g.
    *     `unknown_service:bash`. If `process.executable.name` is not available,
    *     the value MUST be set to `unknown_service`.
    */
  val ServiceName: AttributeKey[String] = string("service.name")

  /** A namespace for `service.name`.
    *
    * @note
    *   - A string value having a meaning that helps to distinguish a group of
    *     services, for example the team name that owns a group of services.
    *     `service.name` is expected to be unique within the same namespace. If
    *     `service.namespace` is not specified in the Resource then
    *     `service.name` is expected to be unique for all services that have no
    *     explicit namespace defined (so the empty/unspecified namespace is
    *     simply one more valid namespace). Zero-length namespace string is
    *     assumed equal to unspecified namespace.
    */
  val ServiceNamespace: AttributeKey[String] = string("service.namespace")

  /** The version string of the service API or implementation. The format is not
    * defined by these conventions.
    */
  val ServiceVersion: AttributeKey[String] = string("service.version")

}
