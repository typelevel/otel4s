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
object PeerExperimentalAttributes {

  /** The <a href="/docs/resource/README.md#service">`service.name`</a> of the remote service. SHOULD be equal to the
    * actual `service.name` resource attribute of the remote service if any.
    *
    * @note
    *   <p> Examples of `peer.service` that users may specify: <ul> <li>A Redis cache of auth tokens as
    *   `peer.service="AuthTokenCache"`. <li>A gRPC service `rpc.service="io.opentelemetry.AuthService"` may be hosted
    *   in both a gateway, `peer.service="ExternalApiService"` and a backend, `peer.service="AuthService"`. </ul>
    */
  @deprecated("Replaced by `service.peer.name`.", "")
  val PeerService: AttributeKey[String] =
    AttributeKey("peer.service")

}
