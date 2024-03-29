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
object UrlExperimentalAttributes {

  /** The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.5">URI
    * fragment</a> component
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.UrlAttributes.UrlFragment` instead.",
    "0.5.0"
  )
  val UrlFragment: AttributeKey[String] = string("url.fragment")

  /** Absolute URL describing a network resource according to <a
    * href="https://www.rfc-editor.org/rfc/rfc3986">RFC3986</a>
    *
    * @note
    *   - For network calls, URL usually has
    *     `scheme://host[:port][path][?query][#fragment]` format, where the
    *     fragment is not transmitted over HTTP, but if it is known, it SHOULD
    *     be included nevertheless. `url.full` MUST NOT contain credentials
    *     passed via URL in form of
    *     `https://username:password@www.example.com/`. In such case username
    *     and password SHOULD be redacted and attribute's value SHOULD be
    *     `https://REDACTED:REDACTED@www.example.com/`. `url.full` SHOULD
    *     capture the absolute URL when it is available (or can be
    *     reconstructed) and SHOULD NOT be validated or modified except for
    *     sanitizing purposes.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.UrlAttributes.UrlFull` instead.",
    "0.5.0"
  )
  val UrlFull: AttributeKey[String] = string("url.full")

  /** The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.3">URI
    * path</a> component
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.UrlAttributes.UrlPath` instead.",
    "0.5.0"
  )
  val UrlPath: AttributeKey[String] = string("url.path")

  /** The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.4">URI
    * query</a> component
    *
    * @note
    *   - Sensitive content provided in query string SHOULD be scrubbed when
    *     instrumentations can identify it.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.UrlAttributes.UrlQuery` instead.",
    "0.5.0"
  )
  val UrlQuery: AttributeKey[String] = string("url.query")

  /** The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.1">URI
    * scheme</a> component identifying the used protocol.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.UrlAttributes.UrlScheme` instead.",
    "0.5.0"
  )
  val UrlScheme: AttributeKey[String] = string("url.scheme")

}
