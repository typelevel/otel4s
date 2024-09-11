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
object ProcessExperimentalAttributes {

  /** The command used to launch the process (i.e. the command name). On Linux based systems, can be set to the zeroth
    * string in `proc/[pid]/cmdline`. On Windows, can be set to the first parameter extracted from `GetCommandLineW`.
    */
  val ProcessCommand: AttributeKey[String] =
    AttributeKey("process.command")

  /** All the command arguments (including the command/executable itself) as received by the process. On Linux-based
    * systems (and some other Unixoid systems supporting procfs), can be set according to the list of null-delimited
    * strings extracted from `proc/[pid]/cmdline`. For libc-based executables, this would be the full argv vector passed
    * to `main`.
    */
  val ProcessCommandArgs: AttributeKey[Seq[String]] =
    AttributeKey("process.command_args")

  /** The full command used to launch the process as a single string representing the full command. On Windows, can be
    * set to the result of `GetCommandLineW`. Do not set this if you have to assemble it just for monitoring; use
    * `process.command_args` instead.
    */
  val ProcessCommandLine: AttributeKey[String] =
    AttributeKey("process.command_line")

  /** Specifies whether the context switches for this data point were voluntary or involuntary.
    */
  val ProcessContextSwitchType: AttributeKey[String] =
    AttributeKey("process.context_switch_type")

  /** Deprecated, use `cpu.mode` instead.
    */
  @deprecated("Replaced by `cpu.mode`", "")
  val ProcessCpuState: AttributeKey[String] =
    AttributeKey("process.cpu.state")

  /** The date and time the process was created, in ISO 8601 format.
    */
  val ProcessCreationTime: AttributeKey[String] =
    AttributeKey("process.creation.time")

  /** The name of the process executable. On Linux based systems, can be set to the `Name` in `proc/[pid]/status`. On
    * Windows, can be set to the base name of `GetProcessImageFileNameW`.
    */
  val ProcessExecutableName: AttributeKey[String] =
    AttributeKey("process.executable.name")

  /** The full path to the process executable. On Linux based systems, can be set to the target of `proc/[pid]/exe`. On
    * Windows, can be set to the result of `GetProcessImageFileNameW`.
    */
  val ProcessExecutablePath: AttributeKey[String] =
    AttributeKey("process.executable.path")

  /** The exit code of the process.
    */
  val ProcessExitCode: AttributeKey[Long] =
    AttributeKey("process.exit.code")

  /** The date and time the process exited, in ISO 8601 format.
    */
  val ProcessExitTime: AttributeKey[String] =
    AttributeKey("process.exit.time")

  /** The PID of the process's group leader. This is also the process group ID (PGID) of the process.
    */
  val ProcessGroupLeaderPid: AttributeKey[Long] =
    AttributeKey("process.group_leader.pid")

  /** Whether the process is connected to an interactive shell.
    */
  val ProcessInteractive: AttributeKey[Boolean] =
    AttributeKey("process.interactive")

  /** The username of the user that owns the process.
    */
  val ProcessOwner: AttributeKey[String] =
    AttributeKey("process.owner")

  /** The type of page fault for this data point. Type `major` is for major/hard page faults, and `minor` is for
    * minor/soft page faults.
    */
  val ProcessPagingFaultType: AttributeKey[String] =
    AttributeKey("process.paging.fault_type")

  /** Parent Process identifier (PPID).
    */
  val ProcessParentPid: AttributeKey[Long] =
    AttributeKey("process.parent_pid")

  /** Process identifier (PID).
    */
  val ProcessPid: AttributeKey[Long] =
    AttributeKey("process.pid")

  /** The real user ID (RUID) of the process.
    */
  val ProcessRealUserId: AttributeKey[Long] =
    AttributeKey("process.real_user.id")

  /** The username of the real user of the process.
    */
  val ProcessRealUserName: AttributeKey[String] =
    AttributeKey("process.real_user.name")

  /** An additional description about the runtime of the process, for example a specific vendor customization of the
    * runtime environment.
    */
  val ProcessRuntimeDescription: AttributeKey[String] =
    AttributeKey("process.runtime.description")

  /** The name of the runtime of this process.
    */
  val ProcessRuntimeName: AttributeKey[String] =
    AttributeKey("process.runtime.name")

  /** The version of the runtime of this process, as returned by the runtime without modification.
    */
  val ProcessRuntimeVersion: AttributeKey[String] =
    AttributeKey("process.runtime.version")

  /** The saved user ID (SUID) of the process.
    */
  val ProcessSavedUserId: AttributeKey[Long] =
    AttributeKey("process.saved_user.id")

  /** The username of the saved user.
    */
  val ProcessSavedUserName: AttributeKey[String] =
    AttributeKey("process.saved_user.name")

  /** The PID of the process's session leader. This is also the session ID (SID) of the process.
    */
  val ProcessSessionLeaderPid: AttributeKey[Long] =
    AttributeKey("process.session_leader.pid")

  /** The effective user ID (EUID) of the process.
    */
  val ProcessUserId: AttributeKey[Long] =
    AttributeKey("process.user.id")

  /** The username of the effective user of the process.
    */
  val ProcessUserName: AttributeKey[String] =
    AttributeKey("process.user.name")

  /** Virtual process identifier. <p>
    * @note
    *   <p> The process ID within a PID namespace. This is not necessarily unique across all processes on the host but
    *   it is unique within the process namespace that the process exists within.
    */
  val ProcessVpid: AttributeKey[Long] =
    AttributeKey("process.vpid")

  /** Values for [[ProcessContextSwitchType]].
    */
  abstract class ProcessContextSwitchTypeValue(val value: String)
  object ProcessContextSwitchTypeValue {

    /** voluntary.
      */
    case object Voluntary extends ProcessContextSwitchTypeValue("voluntary")

    /** involuntary.
      */
    case object Involuntary extends ProcessContextSwitchTypeValue("involuntary")
  }

  /** Values for [[ProcessCpuState]].
    */
  @deprecated("Replaced by `cpu.mode`", "")
  abstract class ProcessCpuStateValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object ProcessCpuStateValue {

    /** system.
      */
    case object System extends ProcessCpuStateValue("system")

    /** user.
      */
    case object User extends ProcessCpuStateValue("user")

    /** wait.
      */
    case object Wait extends ProcessCpuStateValue("wait")
  }

  /** Values for [[ProcessPagingFaultType]].
    */
  abstract class ProcessPagingFaultTypeValue(val value: String)
  object ProcessPagingFaultTypeValue {

    /** major.
      */
    case object Major extends ProcessPagingFaultTypeValue("major")

    /** minor.
      */
    case object Minor extends ProcessPagingFaultTypeValue("minor")
  }

}
