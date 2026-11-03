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
object NetworkExperimentalAttributes {

  /** The ISO 3166-1 alpha-2 2-character country code associated with the mobile carrier network.
    */
  val NetworkCarrierIcc: AttributeKey[String] =
    AttributeKey("network.carrier.icc")

  /** The mobile carrier country code.
    */
  val NetworkCarrierMcc: AttributeKey[String] =
    AttributeKey("network.carrier.mcc")

  /** The mobile carrier network code.
    */
  val NetworkCarrierMnc: AttributeKey[String] =
    AttributeKey("network.carrier.mnc")

  /** The name of the mobile carrier.
    */
  val NetworkCarrierName: AttributeKey[String] =
    AttributeKey("network.carrier.name")

  /** The state of network connection
    *
    * @note
    *   <p> Connection states are defined as part of the <a
    *   href="https://datatracker.ietf.org/doc/html/rfc9293#section-3.3.2">rfc9293</a>
    */
  val NetworkConnectionState: AttributeKey[String] =
    AttributeKey("network.connection.state")

  /** This describes more details regarding the connection.type. It may be the type of cell technology connection, but
    * it could be used for describing details about a Wi-Fi connection.
    */
  val NetworkConnectionSubtype: AttributeKey[String] =
    AttributeKey("network.connection.subtype")

  /** The internet connection type.
    */
  val NetworkConnectionType: AttributeKey[String] =
    AttributeKey("network.connection.type")

  /** The network interface name.
    */
  val NetworkInterfaceName: AttributeKey[String] =
    AttributeKey("network.interface.name")

  /** The direction of traffic from the perspective of the observing host's physical or virtual network interface. It
    * should not be used to represent the logical direction of a stateful connection or network flow.
    */
  val NetworkIoDirection: AttributeKey[String] =
    AttributeKey("network.io.direction")

  /** Local address of the network connection - IP address or UNIX domain socket name.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkLocalAddress` instead.",
    ""
  )
  val NetworkLocalAddress: AttributeKey[String] =
    AttributeKey("network.local.address")

  /** The <a href="https://www.iana.org/assignments/as-numbers/as-numbers.xhtml">Autonomous System Number (ASN)</a>
    * associated with the local IP address.
    *
    * @note
    *   <p> This attribute SHOULD only be set when the local address is an IP address and the ASN is available from
    *   routing, enrichment, or lookup data. The value SHOULD be represented as an integer without the `AS` prefix.
    */
  val NetworkLocalAsNumber: AttributeKey[Long] =
    AttributeKey("network.local.as.number")

  /** The name of the organization associated with the autonomous system of the local IP address.
    *
    * @note
    *   <p> This attribute SHOULD only be set when `network.local.as.number` is set and the organization name is
    *   available. The value SHOULD be the organization name as provided by the lookup or enrichment source, not the AS
    *   name or short code.
    */
  val NetworkLocalAsOrganization: AttributeKey[String] =
    AttributeKey("network.local.as.organization")

  /** Local Media Access Control (MAC) address of the network connection.
    *
    * @note
    *   <p> MAC addresses MUST be represented in <a
    *   href="https://standards.ieee.org/wp-content/uploads/import/documents/tutorials/eui.pdf">IEEE RA hexadecimal
    *   form</a>: as hyphen-separated octets in uppercase hexadecimal form from most to least significant. <blockquote>
    *   [!WARNING] <p> This attribute may contain sensitive (PII) information.</blockquote>
    */
  val NetworkLocalMacAddress: AttributeKey[String] =
    AttributeKey("network.local.mac.address")

  /** Local port number of the network connection.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkLocalPort` instead.",
    ""
  )
  val NetworkLocalPort: AttributeKey[Long] =
    AttributeKey("network.local.port")

  /** The IP prefix associated with the local IP address, in CIDR notation.
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
    *   This attribute MAY be set whether or not `network.local.address` is set. If `network.local.address` is also set
    *   to an IP address, it MUST use the same IP version and be contained in this prefix.
    */
  val NetworkLocalPrefix: AttributeKey[String] =
    AttributeKey("network.local.prefix")

  /** Peer address of the network connection - IP address or UNIX domain socket name.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkPeerAddress` instead.",
    ""
  )
  val NetworkPeerAddress: AttributeKey[String] =
    AttributeKey("network.peer.address")

  /** The <a href="https://www.iana.org/assignments/as-numbers/as-numbers.xhtml">Autonomous System Number (ASN)</a>
    * associated with the peer IP address.
    *
    * @note
    *   <p> This attribute SHOULD only be set when the peer address is an IP address and the ASN is available from
    *   routing, enrichment, or lookup data. The value SHOULD be represented as an integer without the `AS` prefix.
    */
  val NetworkPeerAsNumber: AttributeKey[Long] =
    AttributeKey("network.peer.as.number")

  /** The name of the organization associated with the autonomous system of the peer IP address.
    *
    * @note
    *   <p> This attribute SHOULD only be set when `network.peer.as.number` is set and the organization name is
    *   available. The value SHOULD be the organization name as provided by the lookup or enrichment source, not the AS
    *   name or short code.
    */
  val NetworkPeerAsOrganization: AttributeKey[String] =
    AttributeKey("network.peer.as.organization")

  /** Peer Media Access Control (MAC) address of the network connection.
    *
    * @note
    *   <p> MAC addresses MUST be represented in <a
    *   href="https://standards.ieee.org/wp-content/uploads/import/documents/tutorials/eui.pdf">IEEE RA hexadecimal
    *   form</a>: as hyphen-separated octets in uppercase hexadecimal form from most to least significant. <blockquote>
    *   [!WARNING] <p> This attribute may contain sensitive (PII) information.</blockquote>
    */
  val NetworkPeerMacAddress: AttributeKey[String] =
    AttributeKey("network.peer.mac.address")

  /** Peer port number of the network connection.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkPeerPort` instead.",
    ""
  )
  val NetworkPeerPort: AttributeKey[Long] =
    AttributeKey("network.peer.port")

  /** The IP prefix associated with the peer IP address, in CIDR notation.
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
    *   This attribute MAY be set whether or not `network.peer.address` is set. If `network.peer.address` is also set to
    *   an IP address, it MUST use the same IP version and be contained in this prefix.
    */
  val NetworkPeerPrefix: AttributeKey[String] =
    AttributeKey("network.peer.prefix")

  /** <a href="https://wikipedia.org/wiki/Application_layer">OSI application layer</a> or non-OSI equivalent.
    *
    * @note
    *   <p> The value SHOULD be normalized to lowercase.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkProtocolName` instead.",
    ""
  )
  val NetworkProtocolName: AttributeKey[String] =
    AttributeKey("network.protocol.name")

  /** The actual version of the protocol used for network communication.
    *
    * @note
    *   <p> If protocol version is subject to negotiation (for example using <a
    *   href="https://www.rfc-editor.org/rfc/rfc7301.html">ALPN</a>), this attribute SHOULD be set to the negotiated
    *   version. If the actual protocol version is not known, this attribute SHOULD NOT be set.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkProtocolVersion` instead.",
    ""
  )
  val NetworkProtocolVersion: AttributeKey[String] =
    AttributeKey("network.protocol.version")

  /** <a href="https://wikipedia.org/wiki/Transport_layer">OSI transport layer</a> or <a
    * href="https://wikipedia.org/wiki/Inter-process_communication">inter-process communication method</a>.
    *
    * @note
    *   <p> The value SHOULD be normalized to lowercase. <p> Consider always setting the transport when setting a port
    *   number, since a port number is ambiguous without knowing the transport. For example different processes could be
    *   listening on TCP port 12345 and UDP port 12345.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkTransport` instead.",
    ""
  )
  val NetworkTransport: AttributeKey[String] =
    AttributeKey("network.transport")

  /** <a href="https://wikipedia.org/wiki/Network_layer">OSI network layer</a> or non-OSI equivalent.
    *
    * @note
    *   <p> The value SHOULD be normalized to lowercase.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkType` instead.",
    ""
  )
  val NetworkType: AttributeKey[String] =
    AttributeKey("network.type")

  /** Values for [[NetworkConnectionState]].
    */
  abstract class NetworkConnectionStateValue(val value: String)
  object NetworkConnectionStateValue {
    implicit val attributeFromNetworkConnectionStateValue: Attribute.From[NetworkConnectionStateValue, String] = _.value

    /** closed.
      */
    case object Closed extends NetworkConnectionStateValue("closed")

    /** close_wait.
      */
    case object CloseWait extends NetworkConnectionStateValue("close_wait")

    /** closing.
      */
    case object Closing extends NetworkConnectionStateValue("closing")

    /** established.
      */
    case object Established extends NetworkConnectionStateValue("established")

    /** fin_wait_1.
      */
    case object FinWait1 extends NetworkConnectionStateValue("fin_wait_1")

    /** fin_wait_2.
      */
    case object FinWait2 extends NetworkConnectionStateValue("fin_wait_2")

    /** last_ack.
      */
    case object LastAck extends NetworkConnectionStateValue("last_ack")

    /** listen.
      */
    case object Listen extends NetworkConnectionStateValue("listen")

    /** syn_received.
      */
    case object SynReceived extends NetworkConnectionStateValue("syn_received")

    /** syn_sent.
      */
    case object SynSent extends NetworkConnectionStateValue("syn_sent")

    /** time_wait.
      */
    case object TimeWait extends NetworkConnectionStateValue("time_wait")
  }

  /** Values for [[NetworkConnectionSubtype]].
    */
  abstract class NetworkConnectionSubtypeValue(val value: String)
  object NetworkConnectionSubtypeValue {
    implicit val attributeFromNetworkConnectionSubtypeValue: Attribute.From[NetworkConnectionSubtypeValue, String] =
      _.value

    /** GPRS
      */
    case object Gprs extends NetworkConnectionSubtypeValue("gprs")

    /** EDGE
      */
    case object Edge extends NetworkConnectionSubtypeValue("edge")

    /** UMTS
      */
    case object Umts extends NetworkConnectionSubtypeValue("umts")

    /** CDMA
      */
    case object Cdma extends NetworkConnectionSubtypeValue("cdma")

    /** EVDO Rel. 0
      */
    case object Evdo0 extends NetworkConnectionSubtypeValue("evdo_0")

    /** EVDO Rev. A
      */
    case object EvdoA extends NetworkConnectionSubtypeValue("evdo_a")

    /** CDMA2000 1XRTT
      */
    case object Cdma20001xrtt extends NetworkConnectionSubtypeValue("cdma2000_1xrtt")

    /** HSDPA
      */
    case object Hsdpa extends NetworkConnectionSubtypeValue("hsdpa")

    /** HSUPA
      */
    case object Hsupa extends NetworkConnectionSubtypeValue("hsupa")

    /** HSPA
      */
    case object Hspa extends NetworkConnectionSubtypeValue("hspa")

    /** IDEN
      */
    case object Iden extends NetworkConnectionSubtypeValue("iden")

    /** EVDO Rev. B
      */
    case object EvdoB extends NetworkConnectionSubtypeValue("evdo_b")

    /** LTE
      */
    case object Lte extends NetworkConnectionSubtypeValue("lte")

    /** EHRPD
      */
    case object Ehrpd extends NetworkConnectionSubtypeValue("ehrpd")

    /** HSPAP
      */
    case object Hspap extends NetworkConnectionSubtypeValue("hspap")

    /** GSM
      */
    case object Gsm extends NetworkConnectionSubtypeValue("gsm")

    /** TD-SCDMA
      */
    case object TdScdma extends NetworkConnectionSubtypeValue("td_scdma")

    /** IWLAN
      */
    case object Iwlan extends NetworkConnectionSubtypeValue("iwlan")

    /** 5G NR (New Radio)
      */
    case object Nr extends NetworkConnectionSubtypeValue("nr")

    /** 5G NRNSA (New Radio Non-Standalone)
      */
    case object Nrnsa extends NetworkConnectionSubtypeValue("nrnsa")

    /** LTE CA
      */
    case object LteCa extends NetworkConnectionSubtypeValue("lte_ca")
  }

  /** Values for [[NetworkConnectionType]].
    */
  abstract class NetworkConnectionTypeValue(val value: String)
  object NetworkConnectionTypeValue {
    implicit val attributeFromNetworkConnectionTypeValue: Attribute.From[NetworkConnectionTypeValue, String] = _.value

    /** Wi-Fi
      */
    case object Wifi extends NetworkConnectionTypeValue("wifi")

    /** wired.
      */
    case object Wired extends NetworkConnectionTypeValue("wired")

    /** cell.
      */
    case object Cell extends NetworkConnectionTypeValue("cell")

    /** unavailable.
      */
    case object Unavailable extends NetworkConnectionTypeValue("unavailable")

    /** unknown.
      */
    case object Unknown extends NetworkConnectionTypeValue("unknown")
  }

  /** Values for [[NetworkIoDirection]].
    */
  abstract class NetworkIoDirectionValue(val value: String)
  object NetworkIoDirectionValue {
    implicit val attributeFromNetworkIoDirectionValue: Attribute.From[NetworkIoDirectionValue, String] = _.value

    /** transmit.
      */
    case object Transmit extends NetworkIoDirectionValue("transmit")

    /** receive.
      */
    case object Receive extends NetworkIoDirectionValue("receive")
  }

  /** Values for [[NetworkTransport]].
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkTransport` instead.",
    ""
  )
  abstract class NetworkTransportValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object NetworkTransportValue {
    implicit val attributeFromNetworkTransportValue: Attribute.From[NetworkTransportValue, String] = _.value

    /** TCP
      */
    case object Tcp extends NetworkTransportValue("tcp")

    /** UDP
      */
    case object Udp extends NetworkTransportValue("udp")

    /** Named or anonymous pipe.
      */
    case object Pipe extends NetworkTransportValue("pipe")

    /** UNIX domain socket
      */
    case object Unix extends NetworkTransportValue("unix")

    /** QUIC
      */
    case object Quic extends NetworkTransportValue("quic")
  }

  /** Values for [[NetworkType]].
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkType` instead.",
    ""
  )
  abstract class NetworkTypeValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object NetworkTypeValue {
    implicit val attributeFromNetworkTypeValue: Attribute.From[NetworkTypeValue, String] = _.value

    /** IPv4
      */
    case object Ipv4 extends NetworkTypeValue("ipv4")

    /** IPv6
      */
    case object Ipv6 extends NetworkTypeValue("ipv6")
  }

}
