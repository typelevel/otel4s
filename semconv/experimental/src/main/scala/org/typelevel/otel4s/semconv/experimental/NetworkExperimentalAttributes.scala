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
object NetworkExperimentalAttributes {

  /**
  * The ISO 3166-1 alpha-2 2-character country code associated with the mobile carrier network.
  */
  val NetworkCarrierIcc: AttributeKey[String] = string("network.carrier.icc")

  /**
  * The mobile carrier country code.
  */
  val NetworkCarrierMcc: AttributeKey[String] = string("network.carrier.mcc")

  /**
  * The mobile carrier network code.
  */
  val NetworkCarrierMnc: AttributeKey[String] = string("network.carrier.mnc")

  /**
  * The name of the mobile carrier.
  */
  val NetworkCarrierName: AttributeKey[String] = string("network.carrier.name")

  /**
  * This describes more details regarding the connection.type. It may be the type of cell technology connection, but it could be used for describing details about a wifi connection.
  */
  val NetworkConnectionSubtype: AttributeKey[String] = string("network.connection.subtype")

  /**
  * The internet connection type.
  */
  val NetworkConnectionType: AttributeKey[String] = string("network.connection.type")

  /**
  * The network IO operation direction.
  */
  val NetworkIoDirection: AttributeKey[String] = string("network.io.direction")

  /**
  * Local address of the network connection - IP address or Unix domain socket name.
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkLocalAddress` instead.", "0.5.0")
  val NetworkLocalAddress: AttributeKey[String] = string("network.local.address")

  /**
  * Local port number of the network connection.
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkLocalPort` instead.", "0.5.0")
  val NetworkLocalPort: AttributeKey[Long] = long("network.local.port")

  /**
  * Peer address of the network connection - IP address or Unix domain socket name.
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkPeerAddress` instead.", "0.5.0")
  val NetworkPeerAddress: AttributeKey[String] = string("network.peer.address")

  /**
  * Peer port number of the network connection.
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkPeerPort` instead.", "0.5.0")
  val NetworkPeerPort: AttributeKey[Long] = long("network.peer.port")

  /**
  * <a href="https://osi-model.com/application-layer/">OSI application layer</a> or non-OSI equivalent.
  *
  * @note 
  *  - The value SHOULD be normalized to lowercase.
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkProtocolName` instead.", "0.5.0")
  val NetworkProtocolName: AttributeKey[String] = string("network.protocol.name")

  /**
  * The actual version of the protocol used for network communication.
  *
  * @note 
  *  - If protocol version is subject to negotiation (for example using <a href="https://www.rfc-editor.org/rfc/rfc7301.html">ALPN</a>), this attribute SHOULD be set to the negotiated version. If the actual protocol version is not known, this attribute SHOULD NOT be set.
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkProtocolVersion` instead.", "0.5.0")
  val NetworkProtocolVersion: AttributeKey[String] = string("network.protocol.version")

  /**
  * <a href="https://osi-model.com/transport-layer/">OSI transport layer</a> or <a href="https://wikipedia.org/wiki/Inter-process_communication">inter-process communication method</a>.
  *
  * @note 
  *  - The value SHOULD be normalized to lowercase.
  *  - Consider always setting the transport when setting a port number, since
a port number is ambiguous without knowing the transport. For example
different processes could be listening on TCP port 12345 and UDP port 12345.
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkTransport` instead.", "0.5.0")
  val NetworkTransport: AttributeKey[String] = string("network.transport")

  /**
  * <a href="https://osi-model.com/network-layer/">OSI network layer</a> or non-OSI equivalent.
  *
  * @note 
  *  - The value SHOULD be normalized to lowercase.
  */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkType` instead.", "0.5.0")
  val NetworkType: AttributeKey[String] = string("network.type")
  // Enum definitions
  
  /**
   * Values for [[NetworkConnectionSubtype]].
   */
  abstract class NetworkConnectionSubtypeValue(val value: String)
  object NetworkConnectionSubtypeValue {
    /** GPRS. */
    case object Gprs extends NetworkConnectionSubtypeValue("gprs")
    /** EDGE. */
    case object Edge extends NetworkConnectionSubtypeValue("edge")
    /** UMTS. */
    case object Umts extends NetworkConnectionSubtypeValue("umts")
    /** CDMA. */
    case object Cdma extends NetworkConnectionSubtypeValue("cdma")
    /** EVDO Rel. 0. */
    case object Evdo0 extends NetworkConnectionSubtypeValue("evdo_0")
    /** EVDO Rev. A. */
    case object EvdoA extends NetworkConnectionSubtypeValue("evdo_a")
    /** CDMA2000 1XRTT. */
    case object Cdma20001xrtt extends NetworkConnectionSubtypeValue("cdma2000_1xrtt")
    /** HSDPA. */
    case object Hsdpa extends NetworkConnectionSubtypeValue("hsdpa")
    /** HSUPA. */
    case object Hsupa extends NetworkConnectionSubtypeValue("hsupa")
    /** HSPA. */
    case object Hspa extends NetworkConnectionSubtypeValue("hspa")
    /** IDEN. */
    case object Iden extends NetworkConnectionSubtypeValue("iden")
    /** EVDO Rev. B. */
    case object EvdoB extends NetworkConnectionSubtypeValue("evdo_b")
    /** LTE. */
    case object Lte extends NetworkConnectionSubtypeValue("lte")
    /** EHRPD. */
    case object Ehrpd extends NetworkConnectionSubtypeValue("ehrpd")
    /** HSPAP. */
    case object Hspap extends NetworkConnectionSubtypeValue("hspap")
    /** GSM. */
    case object Gsm extends NetworkConnectionSubtypeValue("gsm")
    /** TD-SCDMA. */
    case object TdScdma extends NetworkConnectionSubtypeValue("td_scdma")
    /** IWLAN. */
    case object Iwlan extends NetworkConnectionSubtypeValue("iwlan")
    /** 5G NR (New Radio). */
    case object Nr extends NetworkConnectionSubtypeValue("nr")
    /** 5G NRNSA (New Radio Non-Standalone). */
    case object Nrnsa extends NetworkConnectionSubtypeValue("nrnsa")
    /** LTE CA. */
    case object LteCa extends NetworkConnectionSubtypeValue("lte_ca")
  }
  /**
   * Values for [[NetworkConnectionType]].
   */
  abstract class NetworkConnectionTypeValue(val value: String)
  object NetworkConnectionTypeValue {
    /** wifi. */
    case object Wifi extends NetworkConnectionTypeValue("wifi")
    /** wired. */
    case object Wired extends NetworkConnectionTypeValue("wired")
    /** cell. */
    case object Cell extends NetworkConnectionTypeValue("cell")
    /** unavailable. */
    case object Unavailable extends NetworkConnectionTypeValue("unavailable")
    /** unknown. */
    case object Unknown extends NetworkConnectionTypeValue("unknown")
  }
  /**
   * Values for [[NetworkIoDirection]].
   */
  abstract class NetworkIoDirectionValue(val value: String)
  object NetworkIoDirectionValue {
    /** transmit. */
    case object Transmit extends NetworkIoDirectionValue("transmit")
    /** receive. */
    case object Receive extends NetworkIoDirectionValue("receive")
  }
  /**
   * Values for [[NetworkTransport]].
   */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkTransportValue` instead.", "0.5.0")
  abstract class NetworkTransportValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object NetworkTransportValue {
    /** TCP. */
    case object Tcp extends NetworkTransportValue("tcp")
    /** UDP. */
    case object Udp extends NetworkTransportValue("udp")
    /** Named or anonymous pipe. */
    case object Pipe extends NetworkTransportValue("pipe")
    /** Unix domain socket. */
    case object Unix extends NetworkTransportValue("unix")
  }
  /**
   * Values for [[NetworkType]].
   */
  @deprecated("use `org.typelevel.otel4s.semconv.attributes.NetworkAttributes.NetworkTypeValue` instead.", "0.5.0")
  abstract class NetworkTypeValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object NetworkTypeValue {
    /** IPv4. */
    case object Ipv4 extends NetworkTypeValue("ipv4")
    /** IPv6. */
    case object Ipv6 extends NetworkTypeValue("ipv6")
  }

}