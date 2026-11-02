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
object ServerExperimentalAttributes {

  /** Server domain name if available without reverse DNS lookup; otherwise, IP address or UNIX domain socket name.
    *
    * @note
    *   <p> When observed from the client side, and when communicating through an intermediary, `server.address` SHOULD
    *   represent the server address behind any intermediaries, for example proxies, if it's available.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.ServerAttributes.ServerAddress` instead.",
    ""
  )
  val ServerAddress: AttributeKey[String] =
    AttributeKey("server.address")

  /** The <a href="https://www.iana.org/assignments/as-numbers/as-numbers.xhtml">Autonomous System Number (ASN)</a>
    * associated with the server IP address.
    *
    * @note
    *   <p> This attribute SHOULD only be set when the server address is an IP address and the ASN is available from
    *   routing, enrichment, or lookup data. The value SHOULD be represented as an integer without the `AS` prefix.
    */
  val ServerAsNumber: AttributeKey[Long] =
    AttributeKey("server.as.number")

  /** The name of the organization associated with the autonomous system of the server IP address.
    *
    * @note
    *   <p> This attribute SHOULD only be set when `server.as.number` is set and the organization name is available. The
    *   value SHOULD be the organization name as provided by the lookup or enrichment source, not the AS name or short
    *   code.
    */
  val ServerAsOrganization: AttributeKey[String] =
    AttributeKey("server.as.organization")

  /** Server Media Access Control (MAC) address.
    *
    * @note
    *   <p> This attribute MUST identify a MAC address associated with the logical server endpoint, including when that
    *   endpoint is behind intermediaries. If the server's MAC address is unknown, this attribute MUST NOT be set. The
    *   MAC address of a proxy, gateway, or next-hop router MUST NOT be used as a substitute. <p> MAC addresses MUST be
    *   represented in <a href="https://standards.ieee.org/wp-content/uploads/import/documents/tutorials/eui.pdf">IEEE
    *   RA hexadecimal form</a>: as hyphen-separated octets in uppercase hexadecimal form from most to least
    *   significant. <blockquote> [!WARNING] <p> This attribute may contain sensitive (PII) information.</blockquote>
    */
  val ServerMacAddress: AttributeKey[String] =
    AttributeKey("server.mac.address")

  /** Server port number.
    *
    * @note
    *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
    *   represent the server port behind any intermediaries, for example proxies, if it's available.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.ServerAttributes.ServerPort` instead.",
    ""
  )
  val ServerPort: AttributeKey[Long] =
    AttributeKey("server.port")

  /** The IP prefix associated with the server IP address, in CIDR notation.
    *
    * @note
    *   <blockquote> [!WARNING] <p> This attribute may contain sensitive (PII) information. A `/32` IPv4 prefix or
    *   `/128` IPv6 prefix preserves the complete endpoint IP address. Using CIDR notation does not guarantee that the
    *   data is anonymous.</blockquote> <p> The value MUST be an IPv4 or IPv6 network prefix in CIDR notation, as
    *   defined in <a href="https://www.rfc-editor.org/rfc/rfc4632.html">RFC 4632</a> for IPv4 and <a
    *   href="https://www.rfc-editor.org/rfc/rfc4291.html#section-2.3">RFC 4291</a> for IPv6. IPv4 prefixes MUST use a
    *   dotted-decimal address followed by `/` and a decimal prefix length from 0 to 32. IPv6 prefixes MUST use the
    *   canonical address text representation defined in <a href="https://www.rfc-editor.org/rfc/rfc5952.html">RFC
    *   5952</a>, followed by `/` and a decimal prefix length from 0 to 128. All host bits in the address MUST be set to
    *   zero. The value MUST NOT contain a domain name, UNIX domain socket name, or IPv6 zone identifier. <p>
    *   Instrumentation or Collector receivers that populate this attribute MUST convert native representations, such as
    *   an address and subnet mask or an address and prefix length, to equivalent CIDR notation before emitting it. <p>
    *   This attribute MUST NOT be set unless `server.address` is set to an IPv4 or IPv6 address. The address MUST use
    *   the same IP version as this prefix and be contained in it.
    */
  val ServerPrefix: AttributeKey[String] =
    AttributeKey("server.prefix")

  /** Logical name of the service acting as the server in a connection-based network interaction.
    *
    * @note
    *   <p> SHOULD be equal to the actual <a href="/docs/resource/README.md#service">`service.name`</a> resource
    *   attribute of the logical server, if any. This identifies the server service whether the telemetry is recorded by
    *   the client, the server, or another observer.
    */
  val ServerServiceName: AttributeKey[String] =
    AttributeKey("server.service.name")

  /** Logical namespace of the service acting as the server in a connection-based network interaction.
    *
    * @note
    *   <p> SHOULD be equal to the actual <a href="/docs/resource/README.md#service">`service.namespace`</a> resource
    *   attribute of the logical server, if any. This identifies the server service's namespace whether the telemetry is
    *   recorded by the client, the server, or another observer. Together with `server.service.name`, this identifies
    *   the server service. An empty namespace is equivalent to an unspecified namespace.
    */
  val ServerServiceNamespace: AttributeKey[String] =
    AttributeKey("server.service.namespace")

}
