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

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/attributes/experimental/SemanticAttributes.scala.j2
object SourceExperimentalAttributes {

  /** Source address - domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket
    * name. <p>
    * @note
    *   <p> When observed from the destination side, and when communicating through an intermediary, `source.address`
    *   SHOULD represent the source address behind any intermediaries, for example proxies, if it's available.
    */
  val SourceAddress: AttributeKey[String] =
    AttributeKey("source.address")

  /** Source port number
    */
  val SourcePort: AttributeKey[Long] =
    AttributeKey("source.port")

}
