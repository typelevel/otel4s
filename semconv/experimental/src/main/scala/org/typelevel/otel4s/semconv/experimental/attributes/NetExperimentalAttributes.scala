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

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/attributes/experimental/SemanticAttributes.scala.j2
object NetExperimentalAttributes {

  /** Deprecated, use `network.local.address`.
    */
  @deprecated("Replaced by `network.local.address`.", "")
  val NetHostIp: AttributeKey[String] =
    AttributeKey("net.host.ip")

  /** Deprecated, use `server.address`.
    */
  @deprecated("Replaced by `server.address`.", "")
  val NetHostName: AttributeKey[String] =
    AttributeKey("net.host.name")

  /** Deprecated, use `server.port`.
    */
  @deprecated("Replaced by `server.port`.", "")
  val NetHostPort: AttributeKey[Long] =
    AttributeKey("net.host.port")

  /** Deprecated, use `network.peer.address`.
    */
  @deprecated("Replaced by `network.peer.address`.", "")
  val NetPeerIp: AttributeKey[String] =
    AttributeKey("net.peer.ip")

  /** Deprecated, use `server.address` on client spans and `client.address` on server spans.
    */
  @deprecated(
    "Replaced by `server.address` on client spans and `client.address` on server spans.",
    ""
  )
  val NetPeerName: AttributeKey[String] =
    AttributeKey("net.peer.name")

  /** Deprecated, use `server.port` on client spans and `client.port` on server spans.
    */
  @deprecated(
    "Replaced by `server.port` on client spans and `client.port` on server spans.",
    ""
  )
  val NetPeerPort: AttributeKey[Long] =
    AttributeKey("net.peer.port")

  /** Deprecated, use `network.protocol.name`.
    */
  @deprecated("Replaced by `network.protocol.name`.", "")
  val NetProtocolName: AttributeKey[String] =
    AttributeKey("net.protocol.name")

  /** Deprecated, use `network.protocol.version`.
    */
  @deprecated("Replaced by `network.protocol.version`.", "")
  val NetProtocolVersion: AttributeKey[String] =
    AttributeKey("net.protocol.version")

  /** Deprecated, use `network.transport` and `network.type`.
    */
  @deprecated("Split to `network.transport` and `network.type`.", "")
  val NetSockFamily: AttributeKey[String] =
    AttributeKey("net.sock.family")

  /** Deprecated, use `network.local.address`.
    */
  @deprecated("Replaced by `network.local.address`.", "")
  val NetSockHostAddr: AttributeKey[String] =
    AttributeKey("net.sock.host.addr")

  /** Deprecated, use `network.local.port`.
    */
  @deprecated("Replaced by `network.local.port`.", "")
  val NetSockHostPort: AttributeKey[Long] =
    AttributeKey("net.sock.host.port")

  /** Deprecated, use `network.peer.address`.
    */
  @deprecated("Replaced by `network.peer.address`.", "")
  val NetSockPeerAddr: AttributeKey[String] =
    AttributeKey("net.sock.peer.addr")

  /** Deprecated, no replacement at this time.
    */
  @deprecated("Removed.", "")
  val NetSockPeerName: AttributeKey[String] =
    AttributeKey("net.sock.peer.name")

  /** Deprecated, use `network.peer.port`.
    */
  @deprecated("Replaced by `network.peer.port`.", "")
  val NetSockPeerPort: AttributeKey[Long] =
    AttributeKey("net.sock.peer.port")

  /** Deprecated, use `network.transport`.
    */
  @deprecated("Replaced by `network.transport`.", "")
  val NetTransport: AttributeKey[String] =
    AttributeKey("net.transport")

  /** Values for [[NetSockFamily]].
    */
  @deprecated("Split to `network.transport` and `network.type`.", "")
  abstract class NetSockFamilyValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object NetSockFamilyValue {

    /** IPv4 address
      */
    case object Inet extends NetSockFamilyValue("inet")

    /** IPv6 address
      */
    case object Inet6 extends NetSockFamilyValue("inet6")

    /** Unix domain socket path
      */
    case object Unix extends NetSockFamilyValue("unix")
  }

  /** Values for [[NetTransport]].
    */
  @deprecated("Replaced by `network.transport`.", "")
  abstract class NetTransportValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object NetTransportValue {

    /** ip_tcp.
      */
    case object IpTcp extends NetTransportValue("ip_tcp")

    /** ip_udp.
      */
    case object IpUdp extends NetTransportValue("ip_udp")

    /** Named or anonymous pipe.
      */
    case object Pipe extends NetTransportValue("pipe")

    /** In-process communication.
      */
    case object Inproc extends NetTransportValue("inproc")

    /** Something else (non IP-based).
      */
    case object Other extends NetTransportValue("other")
  }

}
