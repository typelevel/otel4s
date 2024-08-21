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
object ClientExperimentalAttributes {

  /** Client address - domain name if available without reverse DNS lookup;
    * otherwise, IP address or Unix domain socket name.
    *
    * @note
    *   - When observed from the server side, and when communicating through an
    *     intermediary, `client.address` SHOULD represent the client address
    *     behind any intermediaries, for example proxies, if it's available.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.ClientAttributes.ClientAddress` instead.",
    "0.5.0"
  )
  val ClientAddress: AttributeKey[String] = string("client.address")

  /** Client port number.
    *
    * @note
    *   - When observed from the server side, and when communicating through an
    *     intermediary, `client.port` SHOULD represent the client port behind
    *     any intermediaries, for example proxies, if it's available.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.ClientAttributes.ClientPort` instead.",
    "0.5.0"
  )
  val ClientPort: AttributeKey[Long] = long("client.port")

}
