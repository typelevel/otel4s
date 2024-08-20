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
object HttpExperimentalAttributes {

  /**
  * Deprecated, use `client.address` instead.
  */
  @deprecated("Use `client.address` instead", "0.5.0")
  val HttpClientIp: AttributeKey[String] = string("http.client_ip")

  /**
  * State of the HTTP connection in the HTTP connection pool.
  */
  val HttpConnectionState: AttributeKey[String] = string("http.connection.state")

  /**
  * Deprecated, use `network.protocol.name` instead.
  */
  @deprecated("Use `network.protocol.name` instead", "0.5.0")
  val HttpFlavor: AttributeKey[String] = string("http.flavor")

  /**
  * Deprecated, use one of `server.address`, `client.address` or `http.request.header.host` instead, depending on the usage.
  */
  @deprecated("Use one of `server.address`, `client.address` or `http.request.header.host` instead, depending on the usage", "0.5.0")
  val HttpHost: AttributeKey[String] = string("http.host")

  /**
  * Deprecated, use `http.request.method` instead.
  */
  @deprecated("Use `http.request.method` instead", "0.5.0")
  val HttpMethod: AttributeKey[String] = string("http.method")

  /**
  * The size of the request payload body in bytes. This is the number of bytes transferred excluding headers and is often, but not always, present as the <a href="https://www.rfc-editor.org/rfc/rfc9110.html#field.content-length">Content-Length</a> header. For requests using transport encoding, this should be the compressed size.
  */
  val HttpRequestBodySize: AttributeKey[Long] = long("http.request.body.size")

  /**
  * HTTP request headers, `<key>` being the normalized HTTP Header name (lowercase), the value being the header values.
  *
  * @note 
  *  - Instrumentations SHOULD require an explicit configuration of which headers are to be captured. Including all request headers can be a security risk - explicit configuration helps avoid leaking sensitive information.
The `User-Agent` header is already captured in the `user_agent.original` attribute. Users MAY explicitly configure instrumentations to capture them even though it is not recommended.
The attribute value MUST consist of either multiple header values as an array of strings or a single-item array containing a possibly comma-concatenated string, depending on the way the HTTP library provides access to headers.
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.HttpAttributes.HttpRequestHeader` instead.", "0.5.0")
  val HttpRequestHeader: AttributeKey[Seq[String]] = stringSeq("http.request.header")

  /**
  * HTTP request method.
  *
  * @note 
  *  - HTTP request method value SHOULD be &quot;known&quot; to the instrumentation.
By default, this convention defines &quot;known&quot; methods as the ones listed in <a href="https://www.rfc-editor.org/rfc/rfc9110.html#name-methods">RFC9110</a>
and the PATCH method defined in <a href="https://www.rfc-editor.org/rfc/rfc5789.html">RFC5789</a>.
  *  - If the HTTP request method is not known to instrumentation, it MUST set the `http.request.method` attribute to `_OTHER`.
  *  - If the HTTP instrumentation could end up converting valid HTTP request methods to `_OTHER`, then it MUST provide a way to override
the list of known HTTP methods. If this override is done via environment variable, then the environment variable MUST be named
OTEL_INSTRUMENTATION_HTTP_KNOWN_METHODS and support a comma-separated list of case-sensitive known HTTP methods
(this list MUST be a full override of the default known method, it is not a list of known methods in addition to the defaults).
  *  - HTTP method names are case-sensitive and `http.request.method` attribute value MUST match a known HTTP method name exactly.
Instrumentations for specific web frameworks that consider HTTP methods to be case insensitive, SHOULD populate a canonical equivalent.
Tracing instrumentations that do so, MUST also set `http.request.method_original` to the original value.
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.HttpAttributes.HttpRequestMethod` instead.", "0.5.0")
  val HttpRequestMethod: AttributeKey[String] = string("http.request.method")

  /**
  * Original HTTP method sent by the client in the request line.
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.HttpAttributes.HttpRequestMethodOriginal` instead.", "0.5.0")
  val HttpRequestMethodOriginal: AttributeKey[String] = string("http.request.method_original")

  /**
  * The ordinal number of request resending attempt (for any reason, including redirects).
  *
  * @note 
  *  - The resend count SHOULD be updated each time an HTTP request gets resent by the client, regardless of what was the cause of the resending (e.g. redirection, authorization failure, 503 Server Unavailable, network issues, or any other).
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.HttpAttributes.HttpRequestResendCount` instead.", "0.5.0")
  val HttpRequestResendCount: AttributeKey[Long] = long("http.request.resend_count")

  /**
  * The total size of the request in bytes. This should be the total number of bytes sent over the wire, including the request line (HTTP/1.1), framing (HTTP/2 and HTTP/3), headers, and request body if any.
  */
  val HttpRequestSize: AttributeKey[Long] = long("http.request.size")

  /**
  * Deprecated, use `http.request.header.content-length` instead.
  */
  @deprecated("Use `http.request.header.content-length` instead", "0.5.0")
  val HttpRequestContentLength: AttributeKey[Long] = long("http.request_content_length")

  /**
  * Deprecated, use `http.request.body.size` instead.
  */
  @deprecated("Use `http.request.body.size` instead", "0.5.0")
  val HttpRequestContentLengthUncompressed: AttributeKey[Long] = long("http.request_content_length_uncompressed")

  /**
  * The size of the response payload body in bytes. This is the number of bytes transferred excluding headers and is often, but not always, present as the <a href="https://www.rfc-editor.org/rfc/rfc9110.html#field.content-length">Content-Length</a> header. For requests using transport encoding, this should be the compressed size.
  */
  val HttpResponseBodySize: AttributeKey[Long] = long("http.response.body.size")

  /**
  * HTTP response headers, `<key>` being the normalized HTTP Header name (lowercase), the value being the header values.
  *
  * @note 
  *  - Instrumentations SHOULD require an explicit configuration of which headers are to be captured. Including all response headers can be a security risk - explicit configuration helps avoid leaking sensitive information.
Users MAY explicitly configure instrumentations to capture them even though it is not recommended.
The attribute value MUST consist of either multiple header values as an array of strings or a single-item array containing a possibly comma-concatenated string, depending on the way the HTTP library provides access to headers.
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.HttpAttributes.HttpResponseHeader` instead.", "0.5.0")
  val HttpResponseHeader: AttributeKey[Seq[String]] = stringSeq("http.response.header")

  /**
  * The total size of the response in bytes. This should be the total number of bytes sent over the wire, including the status line (HTTP/1.1), framing (HTTP/2 and HTTP/3), headers, and response body and trailers if any.
  */
  val HttpResponseSize: AttributeKey[Long] = long("http.response.size")

  /**
  * <a href="https://tools.ietf.org/html/rfc7231#section-6">HTTP response status code</a>.
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.HttpAttributes.HttpResponseStatusCode` instead.", "0.5.0")
  val HttpResponseStatusCode: AttributeKey[Long] = long("http.response.status_code")

  /**
  * Deprecated, use `http.response.header.content-length` instead.
  */
  @deprecated("Use `http.response.header.content-length` instead", "0.5.0")
  val HttpResponseContentLength: AttributeKey[Long] = long("http.response_content_length")

  /**
  * Deprecated, use `http.response.body.size` instead.
  */
  @deprecated("Use `http.response.body.size` instead", "0.5.0")
  val HttpResponseContentLengthUncompressed: AttributeKey[Long] = long("http.response_content_length_uncompressed")

  /**
  * The matched route, that is, the path template in the format used by the respective server framework.
  *
  * @note 
  *  - MUST NOT be populated when this is not supported by the HTTP server framework as the route attribute should have low-cardinality and the URI path can NOT substitute it.
SHOULD include the <a href="/docs/http/http-spans.md#http-server-definitions">application root</a> if there is one.
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.HttpAttributes.HttpRoute` instead.", "0.5.0")
  val HttpRoute: AttributeKey[String] = string("http.route")

  /**
  * Deprecated, use `url.scheme` instead.
  */
  @deprecated("Use `url.scheme` instead", "0.5.0")
  val HttpScheme: AttributeKey[String] = string("http.scheme")

  /**
  * Deprecated, use `server.address` instead.
  */
  @deprecated("Use `server.address` instead", "0.5.0")
  val HttpServerName: AttributeKey[String] = string("http.server_name")

  /**
  * Deprecated, use `http.response.status_code` instead.
  */
  @deprecated("Use `http.response.status_code` instead", "0.5.0")
  val HttpStatusCode: AttributeKey[Long] = long("http.status_code")

  /**
  * Deprecated, use `url.path` and `url.query` instead.
  */
  @deprecated("Use `url.path` and `url.query` instead", "0.5.0")
  val HttpTarget: AttributeKey[String] = string("http.target")

  /**
  * Deprecated, use `url.full` instead.
  */
  @deprecated("Use `url.full` instead", "0.5.0")
  val HttpUrl: AttributeKey[String] = string("http.url")

  /**
  * Deprecated, use `user_agent.original` instead.
  */
  @deprecated("Use `user_agent.original` instead", "0.5.0")
  val HttpUserAgent: AttributeKey[String] = string("http.user_agent")
  // Enum definitions
  
  /**
   * Values for [[HttpConnectionState]].
   */
  abstract class HttpConnectionStateValue(val value: String)
  object HttpConnectionStateValue {
    /** active state. */
    case object Active extends HttpConnectionStateValue("active")
    /** idle state. */
    case object Idle extends HttpConnectionStateValue("idle")
  }
  /**
   * Values for [[HttpFlavor]].
   */
  @deprecated("Use `network.protocol.name` instead", "0.5.0")
  abstract class HttpFlavorValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object HttpFlavorValue {
    /** HTTP/1.0. */
    case object Http10 extends HttpFlavorValue("1.0")
    /** HTTP/1.1. */
    case object Http11 extends HttpFlavorValue("1.1")
    /** HTTP/2. */
    case object Http20 extends HttpFlavorValue("2.0")
    /** HTTP/3. */
    case object Http30 extends HttpFlavorValue("3.0")
    /** SPDY protocol. */
    case object Spdy extends HttpFlavorValue("SPDY")
    /** QUIC protocol. */
    case object Quic extends HttpFlavorValue("QUIC")
  }
  /**
   * Values for [[HttpRequestMethod]].
   */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.HttpAttributes.HttpRequestMethodValue` instead.", "0.5.0")
  abstract class HttpRequestMethodValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object HttpRequestMethodValue {
    /** CONNECT method. */
    case object Connect extends HttpRequestMethodValue("CONNECT")
    /** DELETE method. */
    case object Delete extends HttpRequestMethodValue("DELETE")
    /** GET method. */
    case object Get extends HttpRequestMethodValue("GET")
    /** HEAD method. */
    case object Head extends HttpRequestMethodValue("HEAD")
    /** OPTIONS method. */
    case object Options extends HttpRequestMethodValue("OPTIONS")
    /** PATCH method. */
    case object Patch extends HttpRequestMethodValue("PATCH")
    /** POST method. */
    case object Post extends HttpRequestMethodValue("POST")
    /** PUT method. */
    case object Put extends HttpRequestMethodValue("PUT")
    /** TRACE method. */
    case object Trace extends HttpRequestMethodValue("TRACE")
    /** Any HTTP method that the instrumentation has no prior knowledge of. */
    case object Other extends HttpRequestMethodValue("_OTHER")
  }

}