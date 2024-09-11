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
object SystemExperimentalAttributes {

  /** The logical CPU number [0..n-1]
    */
  val SystemCpuLogicalNumber: AttributeKey[Long] =
    AttributeKey("system.cpu.logical_number")

  /** Deprecated, use `cpu.mode` instead.
    */
  @deprecated("Replaced by `cpu.mode`", "")
  val SystemCpuState: AttributeKey[String] =
    AttributeKey("system.cpu.state")

  /** The device identifier
    */
  val SystemDevice: AttributeKey[String] =
    AttributeKey("system.device")

  /** The filesystem mode
    */
  val SystemFilesystemMode: AttributeKey[String] =
    AttributeKey("system.filesystem.mode")

  /** The filesystem mount path
    */
  val SystemFilesystemMountpoint: AttributeKey[String] =
    AttributeKey("system.filesystem.mountpoint")

  /** The filesystem state
    */
  val SystemFilesystemState: AttributeKey[String] =
    AttributeKey("system.filesystem.state")

  /** The filesystem type
    */
  val SystemFilesystemType: AttributeKey[String] =
    AttributeKey("system.filesystem.type")

  /** The memory state
    */
  val SystemMemoryState: AttributeKey[String] =
    AttributeKey("system.memory.state")

  /** A stateless protocol MUST NOT set this attribute
    */
  val SystemNetworkState: AttributeKey[String] =
    AttributeKey("system.network.state")

  /** The paging access direction
    */
  val SystemPagingDirection: AttributeKey[String] =
    AttributeKey("system.paging.direction")

  /** The memory paging state
    */
  val SystemPagingState: AttributeKey[String] =
    AttributeKey("system.paging.state")

  /** The memory paging type
    */
  val SystemPagingType: AttributeKey[String] =
    AttributeKey("system.paging.type")

  /** The process state, e.g., <a href="https://man7.org/linux/man-pages/man1/ps.1.html#PROCESS_STATE_CODES">Linux
    * Process State Codes</a>
    */
  val SystemProcessStatus: AttributeKey[String] =
    AttributeKey("system.process.status")

  /** Deprecated, use `system.process.status` instead.
    */
  @deprecated("Replaced by `system.process.status`.", "")
  val SystemProcessesStatus: AttributeKey[String] =
    AttributeKey("system.processes.status")

  /** Values for [[SystemCpuState]].
    */
  @deprecated("Replaced by `cpu.mode`", "")
  abstract class SystemCpuStateValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object SystemCpuStateValue {

    /** user.
      */
    case object User extends SystemCpuStateValue("user")

    /** system.
      */
    case object System extends SystemCpuStateValue("system")

    /** nice.
      */
    case object Nice extends SystemCpuStateValue("nice")

    /** idle.
      */
    case object Idle extends SystemCpuStateValue("idle")

    /** iowait.
      */
    case object Iowait extends SystemCpuStateValue("iowait")

    /** interrupt.
      */
    case object Interrupt extends SystemCpuStateValue("interrupt")

    /** steal.
      */
    case object Steal extends SystemCpuStateValue("steal")
  }

  /** Values for [[SystemFilesystemState]].
    */
  abstract class SystemFilesystemStateValue(val value: String)
  object SystemFilesystemStateValue {

    /** used.
      */
    case object Used extends SystemFilesystemStateValue("used")

    /** free.
      */
    case object Free extends SystemFilesystemStateValue("free")

    /** reserved.
      */
    case object Reserved extends SystemFilesystemStateValue("reserved")
  }

  /** Values for [[SystemFilesystemType]].
    */
  abstract class SystemFilesystemTypeValue(val value: String)
  object SystemFilesystemTypeValue {

    /** fat32.
      */
    case object Fat32 extends SystemFilesystemTypeValue("fat32")

    /** exfat.
      */
    case object Exfat extends SystemFilesystemTypeValue("exfat")

    /** ntfs.
      */
    case object Ntfs extends SystemFilesystemTypeValue("ntfs")

    /** refs.
      */
    case object Refs extends SystemFilesystemTypeValue("refs")

    /** hfsplus.
      */
    case object Hfsplus extends SystemFilesystemTypeValue("hfsplus")

    /** ext4.
      */
    case object Ext4 extends SystemFilesystemTypeValue("ext4")
  }

  /** Values for [[SystemMemoryState]].
    */
  abstract class SystemMemoryStateValue(val value: String)
  object SystemMemoryStateValue {

    /** used.
      */
    case object Used extends SystemMemoryStateValue("used")

    /** free.
      */
    case object Free extends SystemMemoryStateValue("free")

    /** shared.
      */
    case object Shared extends SystemMemoryStateValue("shared")

    /** buffers.
      */
    case object Buffers extends SystemMemoryStateValue("buffers")

    /** cached.
      */
    case object Cached extends SystemMemoryStateValue("cached")
  }

  /** Values for [[SystemNetworkState]].
    */
  abstract class SystemNetworkStateValue(val value: String)
  object SystemNetworkStateValue {

    /** close.
      */
    case object Close extends SystemNetworkStateValue("close")

    /** close_wait.
      */
    case object CloseWait extends SystemNetworkStateValue("close_wait")

    /** closing.
      */
    case object Closing extends SystemNetworkStateValue("closing")

    /** delete.
      */
    case object Delete extends SystemNetworkStateValue("delete")

    /** established.
      */
    case object Established extends SystemNetworkStateValue("established")

    /** fin_wait_1.
      */
    case object FinWait1 extends SystemNetworkStateValue("fin_wait_1")

    /** fin_wait_2.
      */
    case object FinWait2 extends SystemNetworkStateValue("fin_wait_2")

    /** last_ack.
      */
    case object LastAck extends SystemNetworkStateValue("last_ack")

    /** listen.
      */
    case object Listen extends SystemNetworkStateValue("listen")

    /** syn_recv.
      */
    case object SynRecv extends SystemNetworkStateValue("syn_recv")

    /** syn_sent.
      */
    case object SynSent extends SystemNetworkStateValue("syn_sent")

    /** time_wait.
      */
    case object TimeWait extends SystemNetworkStateValue("time_wait")
  }

  /** Values for [[SystemPagingDirection]].
    */
  abstract class SystemPagingDirectionValue(val value: String)
  object SystemPagingDirectionValue {

    /** in.
      */
    case object In extends SystemPagingDirectionValue("in")

    /** out.
      */
    case object Out extends SystemPagingDirectionValue("out")
  }

  /** Values for [[SystemPagingState]].
    */
  abstract class SystemPagingStateValue(val value: String)
  object SystemPagingStateValue {

    /** used.
      */
    case object Used extends SystemPagingStateValue("used")

    /** free.
      */
    case object Free extends SystemPagingStateValue("free")
  }

  /** Values for [[SystemPagingType]].
    */
  abstract class SystemPagingTypeValue(val value: String)
  object SystemPagingTypeValue {

    /** major.
      */
    case object Major extends SystemPagingTypeValue("major")

    /** minor.
      */
    case object Minor extends SystemPagingTypeValue("minor")
  }

  /** Values for [[SystemProcessStatus]].
    */
  abstract class SystemProcessStatusValue(val value: String)
  object SystemProcessStatusValue {

    /** running.
      */
    case object Running extends SystemProcessStatusValue("running")

    /** sleeping.
      */
    case object Sleeping extends SystemProcessStatusValue("sleeping")

    /** stopped.
      */
    case object Stopped extends SystemProcessStatusValue("stopped")

    /** defunct.
      */
    case object Defunct extends SystemProcessStatusValue("defunct")
  }

  /** Values for [[SystemProcessesStatus]].
    */
  @deprecated("Replaced by `system.process.status`.", "")
  abstract class SystemProcessesStatusValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object SystemProcessesStatusValue {

    /** running.
      */
    case object Running extends SystemProcessesStatusValue("running")

    /** sleeping.
      */
    case object Sleeping extends SystemProcessesStatusValue("sleeping")

    /** stopped.
      */
    case object Stopped extends SystemProcessesStatusValue("stopped")

    /** defunct.
      */
    case object Defunct extends SystemProcessesStatusValue("defunct")
  }

}
