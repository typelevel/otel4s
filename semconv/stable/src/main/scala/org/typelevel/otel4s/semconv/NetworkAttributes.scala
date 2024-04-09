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
object NetworkAttributes {

  /**
  * Local address of the network connection - IP address or Unix domain socket name.
  */
  val NetworkLocalAddress: AttributeKey[String] = string("network.local.address")

  /**
  * Local port number of the network connection.
  */
  val NetworkLocalPort: AttributeKey[Long] = long("network.local.port")

  /**
  * Peer address of the network connection - IP address or Unix domain socket name.
  */
  val NetworkPeerAddress: AttributeKey[String] = string("network.peer.address")

  /**
  * Peer port number of the network connection.
  */
  val NetworkPeerPort: AttributeKey[Long] = long("network.peer.port")

  /**
  * <a href="https://osi-model.com/application-layer/">OSI application layer</a> or non-OSI equivalent.
  *
  * @note 
  *  - The value SHOULD be normalized to lowercase.
  */
  val NetworkProtocolName: AttributeKey[String] = string("network.protocol.name")

  /**
  * The actual version of the protocol used for network communication.
  *
  * @note 
  *  - If protocol version is subject to negotiation (for example using <a href="https://www.rfc-editor.org/rfc/rfc7301.html">ALPN</a>), this attribute SHOULD be set to the negotiated version. If the actual protocol version is not known, this attribute SHOULD NOT be set.
  */
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
  val NetworkTransport: AttributeKey[String] = string("network.transport")

  /**
  * <a href="https://osi-model.com/network-layer/">OSI network layer</a> or non-OSI equivalent.
  *
  * @note 
  *  - The value SHOULD be normalized to lowercase.
  */
  val NetworkType: AttributeKey[String] = string("network.type")
  // Enum definitions
  
  /**
   * Values for [[NetworkTransport]].
   */
  abstract class NetworkTransportValue(val value: String)
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
  abstract class NetworkTypeValue(val value: String)
  object NetworkTypeValue {
    /** IPv4. */
    case object Ipv4 extends NetworkTypeValue("ipv4")
    /** IPv6. */
    case object Ipv6 extends NetworkTypeValue("ipv6")
  }

}