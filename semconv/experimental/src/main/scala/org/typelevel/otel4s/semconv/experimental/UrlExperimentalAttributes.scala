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

  /**
  * Domain extracted from the `url.full`, such as &quot;opentelemetry.io&quot;.
  *
  * @note 
  *  - In some cases a URL may refer to an IP and/or port directly, without a domain name. In this case, the IP address would go to the domain field. If the URL contains a <a href="https://www.rfc-editor.org/rfc/rfc2732#section-2">literal IPv6 address</a> enclosed by `[` and `]`, the `[` and `]` characters should also be captured in the domain field.
  */
  val UrlDomain: AttributeKey[String] = string("url.domain")

  /**
  * The file extension extracted from the `url.full`, excluding the leading dot.
  *
  * @note 
  *  - The file extension is only set if it exists, as not every url has a file extension. When the file name has multiple extensions `example.tar.gz`, only the last one should be captured `gz`, not `tar.gz`.
  */
  val UrlExtension: AttributeKey[String] = string("url.extension")

  /**
  * The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.5">URI fragment</a> component
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.UrlAttributes.UrlFragment` instead.", "0.5.0")
  val UrlFragment: AttributeKey[String] = string("url.fragment")

  /**
  * Absolute URL describing a network resource according to <a href="https://www.rfc-editor.org/rfc/rfc3986">RFC3986</a>
  *
  * @note 
  *  - For network calls, URL usually has `scheme://host[:port][path][?query][#fragment]` format, where the fragment is not transmitted over HTTP, but if it is known, it SHOULD be included nevertheless.
`url.full` MUST NOT contain credentials passed via URL in form of `https://username:password@www.example.com/`. In such case username and password SHOULD be redacted and attribute's value SHOULD be `https://REDACTED:REDACTED@www.example.com/`.
`url.full` SHOULD capture the absolute URL when it is available (or can be reconstructed). Sensitive content provided in `url.full` SHOULD be scrubbed when instrumentations can identify it.
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.UrlAttributes.UrlFull` instead.", "0.5.0")
  val UrlFull: AttributeKey[String] = string("url.full")

  /**
  * Unmodified original URL as seen in the event source.
  *
  * @note 
  *  - In network monitoring, the observed URL may be a full URL, whereas in access logs, the URL is often just represented as a path. This field is meant to represent the URL as it was observed, complete or not.
`url.original` might contain credentials passed via URL in form of `https://username:password@www.example.com/`. In such case password and username SHOULD NOT be redacted and attribute's value SHOULD remain the same.
  */
  val UrlOriginal: AttributeKey[String] = string("url.original")

  /**
  * The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.3">URI path</a> component
  *
  * @note 
  *  - Sensitive content provided in `url.path` SHOULD be scrubbed when instrumentations can identify it.
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.UrlAttributes.UrlPath` instead.", "0.5.0")
  val UrlPath: AttributeKey[String] = string("url.path")

  /**
  * Port extracted from the `url.full`
  */
  val UrlPort: AttributeKey[Long] = long("url.port")

  /**
  * The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.4">URI query</a> component
  *
  * @note 
  *  - Sensitive content provided in `url.query` SHOULD be scrubbed when instrumentations can identify it.
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.UrlAttributes.UrlQuery` instead.", "0.5.0")
  val UrlQuery: AttributeKey[String] = string("url.query")

  /**
  * The highest registered url domain, stripped of the subdomain.
  *
  * @note 
  *  - This value can be determined precisely with the <a href="http://publicsuffix.org">public suffix list</a>. For example, the registered domain for `foo.example.com` is `example.com`. Trying to approximate this by simply taking the last two labels will not work well for TLDs such as `co.uk`.
  */
  val UrlRegisteredDomain: AttributeKey[String] = string("url.registered_domain")

  /**
  * The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.1">URI scheme</a> component identifying the used protocol.
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.UrlAttributes.UrlScheme` instead.", "0.5.0")
  val UrlScheme: AttributeKey[String] = string("url.scheme")

  /**
  * The subdomain portion of a fully qualified domain name includes all of the names except the host name under the registered_domain. In a partially qualified domain, or if the qualification level of the full name cannot be determined, subdomain contains all of the names below the registered domain.
  *
  * @note 
  *  - The subdomain portion of `www.east.mydomain.co.uk` is `east`. If the domain has multiple levels of subdomain, such as `sub2.sub1.example.com`, the subdomain field should contain `sub2.sub1`, with no trailing period.
  */
  val UrlSubdomain: AttributeKey[String] = string("url.subdomain")

  /**
  * The effective top level domain (eTLD), also known as the domain suffix, is the last part of the domain name. For example, the top level domain for example.com is `com`.
  *
  * @note 
  *  - This value can be determined precisely with the <a href="http://publicsuffix.org">public suffix list</a>.
  */
  val UrlTopLevelDomain: AttributeKey[String] = string("url.top_level_domain")

}