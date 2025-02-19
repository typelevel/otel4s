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
    * it could be used for describing details about a wifi connection.
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

  /** The network IO operation direction.
    */
  val NetworkIoDirection: AttributeKey[String] =
    AttributeKey("network.io.direction")

  /** Local address of the network connection - IP address or Unix domain socket name.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkLocalAddress` instead.",
    ""
  )
  val NetworkLocalAddress: AttributeKey[String] =
    AttributeKey("network.local.address")

  /** Local port number of the network connection.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkLocalPort` instead.",
    ""
  )
  val NetworkLocalPort: AttributeKey[Long] =
    AttributeKey("network.local.port")

  /** Peer address of the network connection - IP address or Unix domain socket name.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkPeerAddress` instead.",
    ""
  )
  val NetworkPeerAddress: AttributeKey[String] =
    AttributeKey("network.peer.address")

  /** Peer port number of the network connection.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkPeerPort` instead.",
    ""
  )
  val NetworkPeerPort: AttributeKey[Long] =
    AttributeKey("network.peer.port")

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

    /** wifi.
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

    /** TCP
      */
    case object Tcp extends NetworkTransportValue("tcp")

    /** UDP
      */
    case object Udp extends NetworkTransportValue("udp")

    /** Named or anonymous pipe.
      */
    case object Pipe extends NetworkTransportValue("pipe")

    /** Unix domain socket
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

    /** IPv4
      */
    case object Ipv4 extends NetworkTypeValue("ipv4")

    /** IPv6
      */
    case object Ipv6 extends NetworkTypeValue("ipv6")
  }

}
