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
object HostExperimentalAttributes {

  /** The CPU architecture the host system is running on.
    */
  val HostArch: AttributeKey[String] =
    AttributeKey("host.arch")

  /** The amount of level 2 memory cache available to the processor (in Bytes).
    */
  val HostCpuCacheL2Size: AttributeKey[Long] =
    AttributeKey("host.cpu.cache.l2.size")

  /** Family or generation of the CPU.
    */
  val HostCpuFamily: AttributeKey[String] =
    AttributeKey("host.cpu.family")

  /** Model identifier. It provides more granular information about the CPU, distinguishing it from other CPUs within
    * the same family.
    */
  val HostCpuModelId: AttributeKey[String] =
    AttributeKey("host.cpu.model.id")

  /** Model designation of the processor.
    */
  val HostCpuModelName: AttributeKey[String] =
    AttributeKey("host.cpu.model.name")

  /** Stepping or core revisions.
    */
  val HostCpuStepping: AttributeKey[String] =
    AttributeKey("host.cpu.stepping")

  /** Processor manufacturer identifier. A maximum 12-character string.
    *
    * @note
    *   <p> <a href="https://wiki.osdev.org/CPUID">CPUID</a> command returns the vendor ID string in EBX, EDX and ECX
    *   registers. Writing these to memory in this order results in a 12-character string.
    */
  val HostCpuVendorId: AttributeKey[String] =
    AttributeKey("host.cpu.vendor.id")

  /** Unique host ID, determined by the first matching source in the priority order.
    *
    * @note
    *   <p> SHOULD be determined using the first available source from the following priority tiers: <ol> <li>Known
    *   cloud vendors - cloud instance ID <li>Known VM vendors - provider VM ID <li>OS-dependent machine ID
    *   <li>User-provided value </ol> <p> The value SHOULD conform to the following properties: <ul>
    *   <li><strong>Unique</strong> within a given context; <li><strong><a
    *   href="https://opentelemetry.io/docs/specs/otel/entities/data-model/#repeatable-identity">Repeatable</a></strong> -
    *   multiple observers on the same host come up with the same value; <li><strong>Persistent</strong> - survives host
    *   restarts and software updates. </ul> <p> <strong>Collecting `host.id` From Known Environments:</strong> When
    *   `host.id` is derived from a cloud vendor instance ID, the resource SHOULD also carry the corresponding
    *   `cloud.provider` and `cloud.platform` attributes so that the source of the `host.id` value can be unambiguously
    *   determined. <p> The following cloud environments are currently recognized: <p>
    *   | Environment        | `host.id` source | `cloud.provider` | `cloud.platform`        |
    *   |:-------------------|:-----------------|:-----------------|:------------------------|
    *   | AWS EC2            | EC2 instance ID  | `aws`            | `aws_ec2`               |
    *   | AWS EKS (node)     | EC2 instance ID  | `aws`            | `aws_eks`               |
    *   | Azure VM           | `vmId`           | `azure`          | `azure.vm`              |
    *   | GCP Compute Engine | GCE instance ID  | `gcp`            | `gcp_compute_engine`    |
    *   | GCP GKE (node)     | GCE instance ID  | `gcp`            | `gcp_kubernetes_engine` |
    *   <p> The listed `cloud.provider`/`cloud.platform` values are for example only. Refer to the <a
    *   href="/docs/registry/attributes/cloud.md">`cloud`</a> attribute definitions for the relevant set of values. <p>
    *   <strong>Non-privileged Machine ID Lookup:</strong> When collecting `host.id` for non-containerized systems,
    *   non-privileged lookups of the machine ID are preferred. SDK detector implementations MUST use the sources listed
    *   below to obtain the machine ID. <p>
    *   | OS      | Primary                                                                                     | Fallback                                    |
    *   |:--------|:--------------------------------------------------------------------------------------------|:--------------------------------------------|
    *   | Linux   | contents of `/etc/machine-id`                                                               | contents of `/var/lib/dbus/machine-id`      |
    *   | BSD     | contents of `/etc/hostid`                                                                   | output of `/bin/kenv -q smbios.system.uuid` |
    *   | macOS   | `IOPlatformUUID` line from the output of `/usr/sbin/ioreg -rd1 -c "IOPlatformExpertDevice"` | -                                           |
    *   | Windows | `MachineGuid` from registry `HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Cryptography`            | -                                           |
    *   <p> <strong>Privileged Machine ID Lookup:</strong> The `host.id` can be looked up using privileged sources. For
    *   example, Linux systems can use the output of `dmidecode -t system`, `dmidecode -t baseboard`,
    *   `dmidecode -t chassis`, or read the corresponding data from the filesystem (e.g.
    *   `cat /sys/devices/virtual/dmi/id/product_id`, `cat /sys/devices/virtual/dmi/id/product_uuid`, etc), however, SDK
    *   resource detector implementations MUST NOT collect `host.id` from privileged sources. If privileged lookup of
    *   `host.id` is required, the value SHOULD be injected via the `OTEL_RESOURCE_ATTRIBUTES` environment variable. <p>
    *   <strong>Collecting `host.id` From User Configuration:</strong> If none of the above sources yields a non-empty
    *   value, the implementation MAY be unable to determine `host.id` automatically and SHOULD omit the attribute. In
    *   that case, the `host.id` can only be provided through user configuration or other available manual options.
    *   Users SHOULD ensure that the `host.id` value conforms to the properties stated above.
    */
  val HostId: AttributeKey[String] =
    AttributeKey("host.id")

  /** VM image ID or host OS image ID. For Cloud, this value is from the provider.
    */
  val HostImageId: AttributeKey[String] =
    AttributeKey("host.image.id")

  /** Name of the VM image or OS install the host was instantiated from.
    */
  val HostImageName: AttributeKey[String] =
    AttributeKey("host.image.name")

  /** The version string of the VM image or host OS as defined in <a
    * href="/docs/resource/README.md#version-attributes">Version Attributes</a>.
    */
  val HostImageVersion: AttributeKey[String] =
    AttributeKey("host.image.version")

  /** Available IP addresses of the host, excluding loopback interfaces.
    *
    * @note
    *   <p> IPv4 Addresses MUST be specified in dotted-quad notation. IPv6 addresses MUST be specified in the <a
    *   href="https://www.rfc-editor.org/rfc/rfc5952.html">RFC 5952</a> format.
    */
  val HostIp: AttributeKey[Seq[String]] =
    AttributeKey("host.ip")

  /** Available MAC addresses of the host, excluding loopback interfaces.
    *
    * @note
    *   <p> MAC Addresses MUST be represented in <a
    *   href="https://standards.ieee.org/wp-content/uploads/import/documents/tutorials/eui.pdf">IEEE RA hexadecimal
    *   form</a>: as hyphen-separated octets in uppercase hexadecimal form from most to least significant.
    */
  val HostMac: AttributeKey[Seq[String]] =
    AttributeKey("host.mac")

  /** Name of the host. On UNIX systems, it may contain what the hostname command returns, or the fully qualified
    * hostname, or another name specified by the user.
    */
  val HostName: AttributeKey[String] =
    AttributeKey("host.name")

  /** Type of host. For Cloud, this must be the machine type.
    */
  val HostType: AttributeKey[String] =
    AttributeKey("host.type")

  /** Values for [[HostArch]].
    */
  abstract class HostArchValue(val value: String)
  object HostArchValue {
    implicit val attributeFromHostArchValue: Attribute.From[HostArchValue, String] = _.value

    /** AMD64
      */
    case object Amd64 extends HostArchValue("amd64")

    /** ARM32
      */
    case object Arm32 extends HostArchValue("arm32")

    /** ARM64
      */
    case object Arm64 extends HostArchValue("arm64")

    /** Itanium
      */
    case object Ia64 extends HostArchValue("ia64")

    /** 32-bit PowerPC
      */
    case object Ppc32 extends HostArchValue("ppc32")

    /** 64-bit PowerPC
      */
    case object Ppc64 extends HostArchValue("ppc64")

    /** IBM z/Architecture
      */
    case object S390x extends HostArchValue("s390x")

    /** 32-bit x86
      */
    case object X86 extends HostArchValue("x86")
  }

}
