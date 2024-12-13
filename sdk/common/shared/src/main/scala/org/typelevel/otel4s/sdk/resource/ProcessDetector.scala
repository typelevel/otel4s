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

package org.typelevel.otel4s.sdk.resource

import org.typelevel.otel4s.AttributeKey

/** Detects process-specific parameters such as executable name, executable path, PID, and so on.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/semconv/resource/process/]]
  */
object ProcessDetector extends ProcessDetectorPlatform {

  private[sdk] object Const {
    val Name = "process"
  }

  private[resource] object Keys {
    val Command: AttributeKey[String] =
      AttributeKey("process.command")

    val CommandArgs: AttributeKey[Seq[String]] =
      AttributeKey("process.command_args")

    val CommandLine: AttributeKey[String] =
      AttributeKey("process.command_line")

    val ExecutableName: AttributeKey[String] =
      AttributeKey("process.executable.name")

    val ExecutablePath: AttributeKey[String] =
      AttributeKey("process.executable.path")

    val Pid: AttributeKey[Long] =
      AttributeKey("process.pid")

    val Owner: AttributeKey[String] =
      AttributeKey("process.owner")
  }

}
