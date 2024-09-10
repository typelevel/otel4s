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
object HttpAttributes {

  /** HTTP request headers, `<key>` being the normalized HTTP Header name
    * (lowercase), the value being the header values. <p>
    * @note
    *   <p> Instrumentations SHOULD require an explicit configuration of which
    *   headers are to be captured. Including all request headers can be a
    *   security risk - explicit configuration helps avoid leaking sensitive
    *   information. The `User-Agent` header is already captured in the
    *   `user_agent.original` attribute. Users MAY explicitly configure
    *   instrumentations to capture them even though it is not recommended. The
    *   attribute value MUST consist of either multiple header values as an
    *   array of strings or a single-item array containing a possibly
    *   comma-concatenated string, depending on the way the HTTP library
    *   provides access to headers.
    */
  val HttpRequestHeader: AttributeKey[Seq[String]] =
    AttributeKey("http.request.header")

  /** HTTP request method. <p>
    * @note
    *   <p> HTTP request method value SHOULD be "known" to the instrumentation.
    *   By default, this convention defines "known" methods as the ones listed
    *   in <a
    *   href="https://www.rfc-editor.org/rfc/rfc9110.html#name-methods">RFC9110</a>
    *   and the PATCH method defined in <a
    *   href="https://www.rfc-editor.org/rfc/rfc5789.html">RFC5789</a>. <p> If
    *   the HTTP request method is not known to instrumentation, it MUST set the
    *   `http.request.method` attribute to `_OTHER`. <p> If the HTTP
    *   instrumentation could end up converting valid HTTP request methods to
    *   `_OTHER`, then it MUST provide a way to override the list of known HTTP
    *   methods. If this override is done via environment variable, then the
    *   environment variable MUST be named
    *   OTEL_INSTRUMENTATION_HTTP_KNOWN_METHODS and support a comma-separated
    *   list of case-sensitive known HTTP methods (this list MUST be a full
    *   override of the default known method, it is not a list of known methods
    *   in addition to the defaults). <p> HTTP method names are case-sensitive
    *   and `http.request.method` attribute value MUST match a known HTTP method
    *   name exactly. Instrumentations for specific web frameworks that consider
    *   HTTP methods to be case insensitive, SHOULD populate a canonical
    *   equivalent. Tracing instrumentations that do so, MUST also set
    *   `http.request.method_original` to the original value.
    */
  val HttpRequestMethod: AttributeKey[String] =
    AttributeKey("http.request.method")

  /** Original HTTP method sent by the client in the request line.
    */
  val HttpRequestMethodOriginal: AttributeKey[String] =
    AttributeKey("http.request.method_original")

  /** The ordinal number of request resending attempt (for any reason, including
    * redirects). <p>
    * @note
    *   <p> The resend count SHOULD be updated each time an HTTP request gets
    *   resent by the client, regardless of what was the cause of the resending
    *   (e.g. redirection, authorization failure, 503 Server Unavailable,
    *   network issues, or any other).
    */
  val HttpRequestResendCount: AttributeKey[Long] =
    AttributeKey("http.request.resend_count")

  /** HTTP response headers, `<key>` being the normalized HTTP Header name
    * (lowercase), the value being the header values. <p>
    * @note
    *   <p> Instrumentations SHOULD require an explicit configuration of which
    *   headers are to be captured. Including all response headers can be a
    *   security risk - explicit configuration helps avoid leaking sensitive
    *   information. Users MAY explicitly configure instrumentations to capture
    *   them even though it is not recommended. The attribute value MUST consist
    *   of either multiple header values as an array of strings or a single-item
    *   array containing a possibly comma-concatenated string, depending on the
    *   way the HTTP library provides access to headers.
    */
  val HttpResponseHeader: AttributeKey[Seq[String]] =
    AttributeKey("http.response.header")

  /** <a href="https://tools.ietf.org/html/rfc7231#section-6">HTTP response
    * status code</a>.
    */
  val HttpResponseStatusCode: AttributeKey[Long] =
    AttributeKey("http.response.status_code")

  /** The matched route, that is, the path template in the format used by the
    * respective server framework. <p>
    * @note
    *   <p> MUST NOT be populated when this is not supported by the HTTP server
    *   framework as the route attribute should have low-cardinality and the URI
    *   path can NOT substitute it. SHOULD include the <a
    *   href="/docs/http/http-spans.md#http-server-definitions">application
    *   root</a> if there is one.
    */
  val HttpRoute: AttributeKey[String] =
    AttributeKey("http.route")

  /** Values for [[HttpRequestMethod]].
    */
  abstract class HttpRequestMethodValue(val value: String)
  object HttpRequestMethodValue {

    /** CONNECT method.
      */
    case object Connect extends HttpRequestMethodValue("CONNECT")

    /** DELETE method.
      */
    case object Delete extends HttpRequestMethodValue("DELETE")

    /** GET method.
      */
    case object Get extends HttpRequestMethodValue("GET")

    /** HEAD method.
      */
    case object Head extends HttpRequestMethodValue("HEAD")

    /** OPTIONS method.
      */
    case object Options extends HttpRequestMethodValue("OPTIONS")

    /** PATCH method.
      */
    case object Patch extends HttpRequestMethodValue("PATCH")

    /** POST method.
      */
    case object Post extends HttpRequestMethodValue("POST")

    /** PUT method.
      */
    case object Put extends HttpRequestMethodValue("PUT")

    /** TRACE method.
      */
    case object Trace extends HttpRequestMethodValue("TRACE")

    /** Any HTTP method that the instrumentation has no prior knowledge of.
      */
    case object Other extends HttpRequestMethodValue("_OTHER")
  }

}
