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
object ProcessExperimentalAttributes {

  /** The command used to launch the process (i.e. the command name). On Linux
    * based systems, can be set to the zeroth string in `proc/[pid]/cmdline`. On
    * Windows, can be set to the first parameter extracted from
    * `GetCommandLineW`.
    */
  val ProcessCommand: AttributeKey[String] = string("process.command")

  /** All the command arguments (including the command/executable itself) as
    * received by the process. On Linux-based systems (and some other Unixoid
    * systems supporting procfs), can be set according to the list of
    * null-delimited strings extracted from `proc/[pid]/cmdline`. For libc-based
    * executables, this would be the full argv vector passed to `main`.
    */
  val ProcessCommandArgs: AttributeKey[Seq[String]] = stringSeq(
    "process.command_args"
  )

  /** The full command used to launch the process as a single string
    * representing the full command. On Windows, can be set to the result of
    * `GetCommandLineW`. Do not set this if you have to assemble it just for
    * monitoring; use `process.command_args` instead.
    */
  val ProcessCommandLine: AttributeKey[String] = string("process.command_line")

  /** Specifies whether the context switches for this data point were voluntary
    * or involuntary.
    */
  val ProcessContextSwitchType: AttributeKey[String] = string(
    "process.context_switch_type"
  )

  /** The CPU state for this data point. A process SHOULD be characterized
    * <em>either</em> by data points with no `state` labels, <em>or only</em>
    * data points with `state` labels.
    */
  val ProcessCpuState: AttributeKey[String] = string("process.cpu.state")

  /** The name of the process executable. On Linux based systems, can be set to
    * the `Name` in `proc/[pid]/status`. On Windows, can be set to the base name
    * of `GetProcessImageFileNameW`.
    */
  val ProcessExecutableName: AttributeKey[String] = string(
    "process.executable.name"
  )

  /** The full path to the process executable. On Linux based systems, can be
    * set to the target of `proc/[pid]/exe`. On Windows, can be set to the
    * result of `GetProcessImageFileNameW`.
    */
  val ProcessExecutablePath: AttributeKey[String] = string(
    "process.executable.path"
  )

  /** The username of the user that owns the process.
    */
  val ProcessOwner: AttributeKey[String] = string("process.owner")

  /** The type of page fault for this data point. Type `major` is for major/hard
    * page faults, and `minor` is for minor/soft page faults.
    */
  val ProcessPagingFaultType: AttributeKey[String] = string(
    "process.paging.fault_type"
  )

  /** Parent Process identifier (PPID).
    */
  val ProcessParentPid: AttributeKey[Long] = long("process.parent_pid")

  /** Process identifier (PID).
    */
  val ProcessPid: AttributeKey[Long] = long("process.pid")

  /** An additional description about the runtime of the process, for example a
    * specific vendor customization of the runtime environment.
    */
  val ProcessRuntimeDescription: AttributeKey[String] = string(
    "process.runtime.description"
  )

  /** The name of the runtime of this process. For compiled native binaries,
    * this SHOULD be the name of the compiler.
    */
  val ProcessRuntimeName: AttributeKey[String] = string("process.runtime.name")

  /** The version of the runtime of this process, as returned by the runtime
    * without modification.
    */
  val ProcessRuntimeVersion: AttributeKey[String] = string(
    "process.runtime.version"
  )
  // Enum definitions

  /** Values for [[ProcessContextSwitchType]].
    */
  abstract class ProcessContextSwitchTypeValue(val value: String)
  object ProcessContextSwitchTypeValue {

    /** voluntary. */
    case object Voluntary extends ProcessContextSwitchTypeValue("voluntary")

    /** involuntary. */
    case object Involuntary extends ProcessContextSwitchTypeValue("involuntary")
  }

  /** Values for [[ProcessCpuState]].
    */
  abstract class ProcessCpuStateValue(val value: String)
  object ProcessCpuStateValue {

    /** system. */
    case object System extends ProcessCpuStateValue("system")

    /** user. */
    case object User extends ProcessCpuStateValue("user")

    /** wait. */
    case object Wait extends ProcessCpuStateValue("wait")
  }

  /** Values for [[ProcessPagingFaultType]].
    */
  abstract class ProcessPagingFaultTypeValue(val value: String)
  object ProcessPagingFaultTypeValue {

    /** major. */
    case object Major extends ProcessPagingFaultTypeValue("major")

    /** minor. */
    case object Minor extends ProcessPagingFaultTypeValue("minor")
  }

}
