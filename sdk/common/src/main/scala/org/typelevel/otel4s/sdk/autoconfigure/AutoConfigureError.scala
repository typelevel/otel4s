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

package org.typelevel.otel4s.sdk.autoconfigure

final class AutoConfigureError private (
    message: String,
    cause: Throwable
) extends RuntimeException(message, cause)

object AutoConfigureError {

  def apply(
      hint: String,
      cause: Throwable
  ): AutoConfigureError =
    new AutoConfigureError(
      s"Cannot autoconfigure [$hint]. Cause: ${cause.getMessage}",
      cause
    )

  def apply(
      hint: String,
      cause: Throwable,
      configKeys: Set[Config.Key[_]],
      config: Config
  ): AutoConfigureError =
    if (configKeys.nonEmpty) {
      val params = configKeys.zipWithIndex
        .map { case (key, i) =>
          val name = key.name
          val value = config.getOrElse[String](key.name, "[N/A]")
          val idx = i + 1
          s"$idx) `$name` - $value"
        }
        .mkString("\n")

      new AutoConfigureError(
        s"Cannot autoconfigure [$hint].\nCause: ${cause.getMessage}.\nConfig:\n$params",
        cause
      )
    } else {
      apply(hint, cause)
    }

}
