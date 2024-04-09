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
    *     scaled service).
    *   - Implementations, such as SDKs, are recommended to generate a random
    *     Version 1 or Version 4 <a
    *     href="https://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a> UUID, but are
    *     free to use an inherent unique ID as the source of this value if
    *     stability is desirable. In that case, the ID SHOULD be used as source
    *     of a UUID Version 5 and SHOULD use the following UUID as the
    *     namespace: `4d63009a-8d0f-11ee-aad7-4c796ed8e320`.
    *   - UUIDs are typically recommended, as only an opaque value for the
    *     purposes of identifying a service instance is needed. Similar to what
    *     can be seen in the man page for the <a
    *     href="https://www.freedesktop.org/software/systemd/man/machine-id.html">`/etc/machine-id`</a>
    *     file, the underlying data, such as pod name and namespace should be
    *     treated as confidential, being the user's choice to expose it or not
    *     via another resource attribute.
    *   - For applications running behind an application server (like unicorn),
    *     we do not recommend using one identifier for all processes
    *     participating in the application. Instead, it's recommended each
    *     division (e.g. a worker thread in unicorn) to have its own
    *     instance.id.
    *   - It's not recommended for a Collector to set `service.instance.id` if
    *     it can't unambiguously determine the service instance that is
    *     generating that telemetry. For instance, creating an UUID based on
    *     `pod.name` will likely be wrong, as the Collector might not know from
    *     which container within that pod the telemetry originated. However,
    *     Collectors can set the `service.instance.id` if they can unambiguously
    *     determine the service instance for that telemetry. This is typically
    *     the case for scraping receivers, as they know the target address and
    *     port.
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
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.ServiceAttributes.ServiceName` instead.",
    "0.5.0"
  )
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
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.ServiceAttributes.ServiceVersion` instead.",
    "0.5.0"
  )
  val ServiceVersion: AttributeKey[String] = string("service.version")

}
