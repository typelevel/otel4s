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
object HttpExperimentalAttributes {

  /** Deprecated, use `client.address` instead.
    */
  @deprecated("Replaced by `client.address`.", "")
  val HttpClientIp: AttributeKey[String] =
    AttributeKey("http.client_ip")

  /** State of the HTTP connection in the HTTP connection pool.
    */
  val HttpConnectionState: AttributeKey[String] =
    AttributeKey("http.connection.state")

  /** Deprecated, use `network.protocol.name` and `network.protocol.version` instead.
    */
  @deprecated("Split into `network.protocol.name` and `network.protocol.version`", "")
  val HttpFlavor: AttributeKey[String] =
    AttributeKey("http.flavor")

  /** Deprecated, use one of `server.address`, `client.address` or `http.request.header.host` instead, depending on the
    * usage.
    */
  @deprecated(
    "Replaced by one of `server.address`, `client.address` or `http.request.header.host`, depending on the usage.",
    ""
  )
  val HttpHost: AttributeKey[String] =
    AttributeKey("http.host")

  /** Deprecated, use `http.request.method` instead.
    */
  @deprecated("Replaced by `http.request.method`.", "")
  val HttpMethod: AttributeKey[String] =
    AttributeKey("http.method")

  /** The size of the request payload body in bytes. This is the number of bytes transferred excluding headers and is
    * often, but not always, present as the <a
    * href="https://www.rfc-editor.org/rfc/rfc9110.html#field.content-length">Content-Length</a> header. For requests
    * using transport encoding, this should be the compressed size.
    */
  val HttpRequestBodySize: AttributeKey[Long] =
    AttributeKey("http.request.body.size")

  /** HTTP request headers, `<key>` being the normalized HTTP Header name (lowercase), the value being the header
    * values.
    *
    * @note
    *   <p> Instrumentations SHOULD require an explicit configuration of which headers are to be captured. Including all
    *   request headers can be a security risk - explicit configuration helps avoid leaking sensitive information. <p>
    *   The `User-Agent` header is already captured in the `user_agent.original` attribute. Users MAY explicitly
    *   configure instrumentations to capture them even though it is not recommended. <p> The attribute value MUST
    *   consist of either multiple header values as an array of strings or a single-item array containing a possibly
    *   comma-concatenated string, depending on the way the HTTP library provides access to headers. <p> Examples: <ul>
    *   <li>A header `Content-Type: application/json` SHOULD be recorded as the `http.request.header.content-type`
    *   attribute with value `["application/json"]`. <li>A header `X-Forwarded-For: 1.2.3.4, 1.2.3.5` SHOULD be recorded
    *   as the `http.request.header.x-forwarded-for` attribute with value `["1.2.3.4", "1.2.3.5"]` or
    *   `["1.2.3.4, 1.2.3.5"]` depending on the HTTP library. </ul>
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.HttpAttributes.HttpRequestHeader` instead.",
    ""
  )
  val HttpRequestHeader: AttributeKey[Seq[String]] =
    AttributeKey("http.request.header")

  /** HTTP request method.
    *
    * @note
    *   <p> HTTP request method value SHOULD be "known" to the instrumentation. By default, this convention defines
    *   "known" methods as the ones listed in <a
    *   href="https://www.rfc-editor.org/rfc/rfc9110.html#name-methods">RFC9110</a>, the PATCH method defined in <a
    *   href="https://www.rfc-editor.org/rfc/rfc5789.html">RFC5789</a> and the QUERY method defined in <a
    *   href="https://datatracker.ietf.org/doc/draft-ietf-httpbis-safe-method-w-body/?include_text=1">httpbis-safe-method-w-body</a>.
    *   <p> If the HTTP request method is not known to instrumentation, it MUST set the `http.request.method` attribute
    *   to `_OTHER`. <p> If the HTTP instrumentation could end up converting valid HTTP request methods to `_OTHER`,
    *   then it MUST provide a way to override the list of known HTTP methods. If this override is done via environment
    *   variable, then the environment variable MUST be named OTEL_INSTRUMENTATION_HTTP_KNOWN_METHODS and support a
    *   comma-separated list of case-sensitive known HTTP methods. <p>
    *
    * If this override is done via declarative configuration, then the list MUST be configurable via the `known_methods`
    * property (an array of case-sensitive strings with minimum items 0) under
    * `.instrumentation/development.general.http.client` and/or `.instrumentation/development.general.http.server`. <p>
    * In either case, this list MUST be a full override of the default known methods, it is not a list of known methods
    * in addition to the defaults. <p> HTTP method names are case-sensitive and `http.request.method` attribute value
    * MUST match a known HTTP method name exactly. Instrumentations for specific web frameworks that consider HTTP
    * methods to be case insensitive, SHOULD populate a canonical equivalent. Tracing instrumentations that do so, MUST
    * also set `http.request.method_original` to the original value.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.HttpAttributes.HttpRequestMethod` instead.",
    ""
  )
  val HttpRequestMethod: AttributeKey[String] =
    AttributeKey("http.request.method")

  /** Original HTTP method sent by the client in the request line.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.HttpAttributes.HttpRequestMethodOriginal` instead.",
    ""
  )
  val HttpRequestMethodOriginal: AttributeKey[String] =
    AttributeKey("http.request.method_original")

  /** The ordinal number of request resending attempt (for any reason, including redirects).
    *
    * @note
    *   <p> The resend count SHOULD be updated each time an HTTP request gets resent by the client, regardless of what
    *   was the cause of the resending (e.g. redirection, authorization failure, 503 Server Unavailable, network issues,
    *   or any other).
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.HttpAttributes.HttpRequestResendCount` instead.",
    ""
  )
  val HttpRequestResendCount: AttributeKey[Long] =
    AttributeKey("http.request.resend_count")

  /** The total size of the request in bytes. This should be the total number of bytes sent over the wire, including the
    * request line (HTTP/1.1), framing (HTTP/2 and HTTP/3), headers, and request body if any.
    */
  val HttpRequestSize: AttributeKey[Long] =
    AttributeKey("http.request.size")

  /** Deprecated, use `http.request.header.content-length` instead.
    */
  @deprecated("Replaced by `http.request.header.content-length`.", "")
  val HttpRequestContentLength: AttributeKey[Long] =
    AttributeKey("http.request_content_length")

  /** Deprecated, use `http.request.body.size` instead.
    */
  @deprecated("Replaced by `http.request.body.size`.", "")
  val HttpRequestContentLengthUncompressed: AttributeKey[Long] =
    AttributeKey("http.request_content_length_uncompressed")

  /** The size of the response payload body in bytes. This is the number of bytes transferred excluding headers and is
    * often, but not always, present as the <a
    * href="https://www.rfc-editor.org/rfc/rfc9110.html#field.content-length">Content-Length</a> header. For requests
    * using transport encoding, this should be the compressed size.
    */
  val HttpResponseBodySize: AttributeKey[Long] =
    AttributeKey("http.response.body.size")

  /** HTTP response headers, `<key>` being the normalized HTTP Header name (lowercase), the value being the header
    * values.
    *
    * @note
    *   <p> Instrumentations SHOULD require an explicit configuration of which headers are to be captured. Including all
    *   response headers can be a security risk - explicit configuration helps avoid leaking sensitive information. <p>
    *   Users MAY explicitly configure instrumentations to capture them even though it is not recommended. <p> The
    *   attribute value MUST consist of either multiple header values as an array of strings or a single-item array
    *   containing a possibly comma-concatenated string, depending on the way the HTTP library provides access to
    *   headers. <p> Examples: <ul> <li>A header `Content-Type: application/json` header SHOULD be recorded as the
    *   `http.request.response.content-type` attribute with value `["application/json"]`. <li>A header
    *   `My-custom-header: abc, def` header SHOULD be recorded as the `http.response.header.my-custom-header` attribute
    *   with value `["abc", "def"]` or `["abc, def"]` depending on the HTTP library. </ul>
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.HttpAttributes.HttpResponseHeader` instead.",
    ""
  )
  val HttpResponseHeader: AttributeKey[Seq[String]] =
    AttributeKey("http.response.header")

  /** The total size of the response in bytes. This should be the total number of bytes sent over the wire, including
    * the status line (HTTP/1.1), framing (HTTP/2 and HTTP/3), headers, and response body and trailers if any.
    */
  val HttpResponseSize: AttributeKey[Long] =
    AttributeKey("http.response.size")

  /** <a href="https://tools.ietf.org/html/rfc7231#section-6">HTTP response status code</a>.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.HttpAttributes.HttpResponseStatusCode` instead.",
    ""
  )
  val HttpResponseStatusCode: AttributeKey[Long] =
    AttributeKey("http.response.status_code")

  /** Deprecated, use `http.response.header.content-length` instead.
    */
  @deprecated("Replaced by `http.response.header.content-length`.", "")
  val HttpResponseContentLength: AttributeKey[Long] =
    AttributeKey("http.response_content_length")

  /** Deprecated, use `http.response.body.size` instead.
    */
  @deprecated("Replaced by `http.response.body.size`.", "")
  val HttpResponseContentLengthUncompressed: AttributeKey[Long] =
    AttributeKey("http.response_content_length_uncompressed")

  /** The matched route template for the request. This MUST be low-cardinality and include all static path segments,
    * with dynamic path segments represented with placeholders.
    *
    * @note
    *   <p> MUST NOT be populated when this is not supported by the HTTP server framework as the route attribute should
    *   have low-cardinality and the URI path can NOT substitute it. SHOULD include the <a
    *   href="/docs/http/http-spans.md#http-server-definitions">application root</a> if there is one. <p> A static path
    *   segment is a part of the route template with a fixed, low-cardinality value. This includes literal strings like
    *   `/users/` and placeholders that are constrained to a finite, predefined set of values, e.g. `{controller}` or
    *   `{action}`. <p> A dynamic path segment is a placeholder for a value that can have high cardinality and is not
    *   constrained to a predefined list like static path segments. <p> Instrumentations SHOULD use routing information
    *   provided by the corresponding web framework. They SHOULD pick the most precise source of routing information and
    *   MAY support custom route formatting. Instrumentations SHOULD document the format and the API used to obtain the
    *   route string.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.HttpAttributes.HttpRoute` instead.",
    ""
  )
  val HttpRoute: AttributeKey[String] =
    AttributeKey("http.route")

  /** Deprecated, use `url.scheme` instead.
    */
  @deprecated("Replaced by `url.scheme`.", "")
  val HttpScheme: AttributeKey[String] =
    AttributeKey("http.scheme")

  /** Deprecated, use `server.address` instead.
    */
  @deprecated("Replaced by `server.address`.", "")
  val HttpServerName: AttributeKey[String] =
    AttributeKey("http.server_name")

  /** Deprecated, use `http.response.status_code` instead.
    */
  @deprecated("Replaced by `http.response.status_code`.", "")
  val HttpStatusCode: AttributeKey[Long] =
    AttributeKey("http.status_code")

  /** Deprecated, use `url.path` and `url.query` instead.
    */
  @deprecated("Split to `url.path` and `url.query`.", "")
  val HttpTarget: AttributeKey[String] =
    AttributeKey("http.target")

  /** Deprecated, use `url.full` instead.
    */
  @deprecated("Replaced by `url.full`.", "")
  val HttpUrl: AttributeKey[String] =
    AttributeKey("http.url")

  /** Deprecated, use `user_agent.original` instead.
    */
  @deprecated("Replaced by `user_agent.original`.", "")
  val HttpUserAgent: AttributeKey[String] =
    AttributeKey("http.user_agent")

  /** Values for [[HttpConnectionState]].
    */
  abstract class HttpConnectionStateValue(val value: String)
  object HttpConnectionStateValue {
    implicit val attributeFromHttpConnectionStateValue: Attribute.From[HttpConnectionStateValue, String] = _.value

    /** active state.
      */
    case object Active extends HttpConnectionStateValue("active")

    /** idle state.
      */
    case object Idle extends HttpConnectionStateValue("idle")
  }

  /** Values for [[HttpFlavor]].
    */
  @deprecated("Split into `network.protocol.name` and `network.protocol.version`", "")
  abstract class HttpFlavorValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object HttpFlavorValue {
    implicit val attributeFromHttpFlavorValue: Attribute.From[HttpFlavorValue, String] = _.value

    /** HTTP/1.0
      */
    case object Http10 extends HttpFlavorValue("1.0")

    /** HTTP/1.1
      */
    case object Http11 extends HttpFlavorValue("1.1")

    /** HTTP/2
      */
    case object Http20 extends HttpFlavorValue("2.0")

    /** HTTP/3
      */
    case object Http30 extends HttpFlavorValue("3.0")

    /** SPDY protocol.
      */
    case object Spdy extends HttpFlavorValue("SPDY")

    /** QUIC protocol.
      */
    case object Quic extends HttpFlavorValue("QUIC")
  }

  /** Values for [[HttpRequestMethod]].
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.HttpAttributes.HttpRequestMethod` instead.",
    ""
  )
  abstract class HttpRequestMethodValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object HttpRequestMethodValue {
    implicit val attributeFromHttpRequestMethodValue: Attribute.From[HttpRequestMethodValue, String] = _.value

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

    /** QUERY method.
      */
    case object Query extends HttpRequestMethodValue("QUERY")

    /** Any HTTP method that the instrumentation has no prior knowledge of.
      */
    case object Other extends HttpRequestMethodValue("_OTHER")
  }

}
