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
object HostExperimentalAttributes {

  /** The CPU architecture the host system is running on.
    */
  val HostArch: AttributeKey[String] = string("host.arch")

  /** The amount of level 2 memory cache available to the processor (in Bytes).
    */
  val HostCpuCacheL2Size: AttributeKey[Long] = long("host.cpu.cache.l2.size")

  /** Family or generation of the CPU.
    */
  val HostCpuFamily: AttributeKey[String] = string("host.cpu.family")

  /** Model identifier. It provides more granular information about the CPU,
    * distinguishing it from other CPUs within the same family.
    */
  val HostCpuModelId: AttributeKey[String] = string("host.cpu.model.id")

  /** Model designation of the processor.
    */
  val HostCpuModelName: AttributeKey[String] = string("host.cpu.model.name")

  /** Stepping or core revisions.
    */
  val HostCpuStepping: AttributeKey[Long] = long("host.cpu.stepping")

  /** Processor manufacturer identifier. A maximum 12-character string.
    *
    * @note
    *   - <a href="https://wiki.osdev.org/CPUID">CPUID</a> command returns the
    *     vendor ID string in EBX, EDX and ECX registers. Writing these to
    *     memory in this order results in a 12-character string.
    */
  val HostCpuVendorId: AttributeKey[String] = string("host.cpu.vendor.id")

  /** Unique host ID. For Cloud, this must be the instance_id assigned by the
    * cloud provider. For non-containerized systems, this should be the
    * `machine-id`. See the table below for the sources to use to determine the
    * `machine-id` based on operating system.
    */
  val HostId: AttributeKey[String] = string("host.id")

  /** VM image ID or host OS image ID. For Cloud, this value is from the
    * provider.
    */
  val HostImageId: AttributeKey[String] = string("host.image.id")

  /** Name of the VM image or OS install the host was instantiated from.
    */
  val HostImageName: AttributeKey[String] = string("host.image.name")

  /** The version string of the VM image or host OS as defined in <a
    * href="/docs/resource/README.md#version-attributes">Version Attributes</a>.
    */
  val HostImageVersion: AttributeKey[String] = string("host.image.version")

  /** Available IP addresses of the host, excluding loopback interfaces.
    *
    * @note
    *   - IPv4 Addresses MUST be specified in dotted-quad notation. IPv6
    *     addresses MUST be specified in the <a
    *     href="https://www.rfc-editor.org/rfc/rfc5952.html">RFC 5952</a>
    *     format.
    */
  val HostIp: AttributeKey[Seq[String]] = stringSeq("host.ip")

  /** Available MAC addresses of the host, excluding loopback interfaces.
    *
    * @note
    *   - MAC Addresses MUST be represented in <a
    *     href="https://standards.ieee.org/wp-content/uploads/import/documents/tutorials/eui.pdf">IEEE
    *     RA hexadecimal form</a>: as hyphen-separated octets in uppercase
    *     hexadecimal form from most to least significant.
    */
  val HostMac: AttributeKey[Seq[String]] = stringSeq("host.mac")

  /** Name of the host. On Unix systems, it may contain what the hostname
    * command returns, or the fully qualified hostname, or another name
    * specified by the user.
    */
  val HostName: AttributeKey[String] = string("host.name")

  /** Type of host. For Cloud, this must be the machine type.
    */
  val HostType: AttributeKey[String] = string("host.type")
  // Enum definitions

  /** Values for [[HostArch]].
    */
  abstract class HostArchValue(val value: String)
  object HostArchValue {

    /** AMD64. */
    case object Amd64 extends HostArchValue("amd64")

    /** ARM32. */
    case object Arm32 extends HostArchValue("arm32")

    /** ARM64. */
    case object Arm64 extends HostArchValue("arm64")

    /** Itanium. */
    case object Ia64 extends HostArchValue("ia64")

    /** 32-bit PowerPC. */
    case object Ppc32 extends HostArchValue("ppc32")

    /** 64-bit PowerPC. */
    case object Ppc64 extends HostArchValue("ppc64")

    /** IBM z/Architecture. */
    case object S390x extends HostArchValue("s390x")

    /** 32-bit x86. */
    case object X86 extends HostArchValue("x86")
  }

}
