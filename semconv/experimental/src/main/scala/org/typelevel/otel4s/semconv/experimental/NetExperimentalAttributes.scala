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
object NetExperimentalAttributes {

  /** Deprecated, use `network.local.address`.
    */
  @deprecated("Use `network.local.address`", "0.5.0")
  val NetHostIp: AttributeKey[String] = string("net.host.ip")

  /** Deprecated, use `server.address`.
    */
  @deprecated("Use `server.address`", "0.5.0")
  val NetHostName: AttributeKey[String] = string("net.host.name")

  /** Deprecated, use `server.port`.
    */
  @deprecated("Use `server.port`", "0.5.0")
  val NetHostPort: AttributeKey[Long] = long("net.host.port")

  /** Deprecated, use `network.peer.address`.
    */
  @deprecated("Use `network.peer.address`", "0.5.0")
  val NetPeerIp: AttributeKey[String] = string("net.peer.ip")

  /** Deprecated, use `server.address` on client spans and `client.address` on
    * server spans.
    */
  @deprecated(
    "Use `server.address` on client spans and `client.address` on server spans",
    "0.5.0"
  )
  val NetPeerName: AttributeKey[String] = string("net.peer.name")

  /** Deprecated, use `server.port` on client spans and `client.port` on server
    * spans.
    */
  @deprecated(
    "Use `server.port` on client spans and `client.port` on server spans",
    "0.5.0"
  )
  val NetPeerPort: AttributeKey[Long] = long("net.peer.port")

  /** Deprecated, use `network.protocol.name`.
    */
  @deprecated("Use `network.protocol.name`", "0.5.0")
  val NetProtocolName: AttributeKey[String] = string("net.protocol.name")

  /** Deprecated, use `network.protocol.version`.
    */
  @deprecated("Use `network.protocol.version`", "0.5.0")
  val NetProtocolVersion: AttributeKey[String] = string("net.protocol.version")

  /** Deprecated, use `network.transport` and `network.type`.
    */
  @deprecated("Use `network.transport` and `network.type`", "0.5.0")
  val NetSockFamily: AttributeKey[String] = string("net.sock.family")

  /** Deprecated, use `network.local.address`.
    */
  @deprecated("Use `network.local.address`", "0.5.0")
  val NetSockHostAddr: AttributeKey[String] = string("net.sock.host.addr")

  /** Deprecated, use `network.local.port`.
    */
  @deprecated("Use `network.local.port`", "0.5.0")
  val NetSockHostPort: AttributeKey[Long] = long("net.sock.host.port")

  /** Deprecated, use `network.peer.address`.
    */
  @deprecated("Use `network.peer.address`", "0.5.0")
  val NetSockPeerAddr: AttributeKey[String] = string("net.sock.peer.addr")

  /** Deprecated, no replacement at this time.
    */
  @deprecated("No replacement at this time", "0.5.0")
  val NetSockPeerName: AttributeKey[String] = string("net.sock.peer.name")

  /** Deprecated, use `network.peer.port`.
    */
  @deprecated("Use `network.peer.port`", "0.5.0")
  val NetSockPeerPort: AttributeKey[Long] = long("net.sock.peer.port")

  /** Deprecated, use `network.transport`.
    */
  @deprecated("Use `network.transport`", "0.5.0")
  val NetTransport: AttributeKey[String] = string("net.transport")
  // Enum definitions

  /** Values for [[NetSockFamily]].
    */
  @deprecated("Use `network.transport` and `network.type`", "0.5.0")
  abstract class NetSockFamilyValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object NetSockFamilyValue {

    /** IPv4 address. */
    case object Inet extends NetSockFamilyValue("inet")

    /** IPv6 address. */
    case object Inet6 extends NetSockFamilyValue("inet6")

    /** Unix domain socket path. */
    case object Unix extends NetSockFamilyValue("unix")
  }

  /** Values for [[NetTransport]].
    */
  @deprecated("Use `network.transport`", "0.5.0")
  abstract class NetTransportValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object NetTransportValue {

    /** ip_tcp. */
    case object IpTcp extends NetTransportValue("ip_tcp")

    /** ip_udp. */
    case object IpUdp extends NetTransportValue("ip_udp")

    /** Named or anonymous pipe. */
    case object Pipe extends NetTransportValue("pipe")

    /** In-process communication. */
    case object Inproc extends NetTransportValue("inproc")

    /** Something else (non IP-based). */
    case object Other extends NetTransportValue("other")
  }

}
