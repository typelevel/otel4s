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
object NetworkAttributes {

  /** Local address of the network connection - IP address or Unix domain socket name.
    */
  val NetworkLocalAddress: AttributeKey[String] =
    AttributeKey("network.local.address")

  /** Local port number of the network connection.
    */
  val NetworkLocalPort: AttributeKey[Long] =
    AttributeKey("network.local.port")

  /** Peer address of the network connection - IP address or Unix domain socket name.
    */
  val NetworkPeerAddress: AttributeKey[String] =
    AttributeKey("network.peer.address")

  /** Peer port number of the network connection.
    */
  val NetworkPeerPort: AttributeKey[Long] =
    AttributeKey("network.peer.port")

  /** <a href="https://wikipedia.org/wiki/Application_layer">OSI application layer</a> or non-OSI equivalent.
    *
    * @note
    *   <p> The value SHOULD be normalized to lowercase.
    */
  val NetworkProtocolName: AttributeKey[String] =
    AttributeKey("network.protocol.name")

  /** The actual version of the protocol used for network communication.
    *
    * @note
    *   <p> If protocol version is subject to negotiation (for example using <a
    *   href="https://www.rfc-editor.org/rfc/rfc7301.html">ALPN</a>), this attribute SHOULD be set to the negotiated
    *   version. If the actual protocol version is not known, this attribute SHOULD NOT be set.
    */
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
  val NetworkTransport: AttributeKey[String] =
    AttributeKey("network.transport")

  /** <a href="https://wikipedia.org/wiki/Network_layer">OSI network layer</a> or non-OSI equivalent.
    *
    * @note
    *   <p> The value SHOULD be normalized to lowercase.
    */
  val NetworkType: AttributeKey[String] =
    AttributeKey("network.type")

  /** Values for [[NetworkTransport]].
    */
  abstract class NetworkTransportValue(val value: String)
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

    /** Unix domain socket
      */
    case object Unix extends NetworkTransportValue("unix")

    /** QUIC
      */
    case object Quic extends NetworkTransportValue("quic")
  }

  /** Values for [[NetworkType]].
    */
  abstract class NetworkTypeValue(val value: String)
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
