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

package org.typelevel.otel4s.semconv.attributes

import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.AttributeKey._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/semantic-convention/templates/SemanticAttributes.scala.j2
object UrlAttributes {

  /** The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.5">URI
    * fragment</a> component
    */
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
    *     reconstructed). Sensitive content provided in `url.full` SHOULD be
    *     scrubbed when instrumentations can identify it.
    */
  val UrlFull: AttributeKey[String] = string("url.full")

  /** The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.3">URI
    * path</a> component
    *
    * @note
    *   - Sensitive content provided in `url.path` SHOULD be scrubbed when
    *     instrumentations can identify it.
    */
  val UrlPath: AttributeKey[String] = string("url.path")

  /** The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.4">URI
    * query</a> component
    *
    * @note
    *   - Sensitive content provided in `url.query` SHOULD be scrubbed when
    *     instrumentations can identify it.
    */
  val UrlQuery: AttributeKey[String] = string("url.query")

  /** The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.1">URI
    * scheme</a> component identifying the used protocol.
    */
  val UrlScheme: AttributeKey[String] = string("url.scheme")

}
