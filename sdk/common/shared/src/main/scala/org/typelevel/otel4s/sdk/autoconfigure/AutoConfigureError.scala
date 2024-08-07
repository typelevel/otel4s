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

  /** Creates an [[AutoConfigureError]] with the given `hint` and `cause`.
    *
    * @param hint
    *   the name of the component
    *
    * @param cause
    *   the cause
    */
  def apply(
      hint: String,
      cause: Throwable
  ): AutoConfigureError =
    new AutoConfigureError(
      s"Cannot autoconfigure [$hint]. Cause: ${cause.getMessage}.",
      cause
    )

  /** Creates an [[AutoConfigureError]] with the given `hint` and `cause`. The
    * debug information associated with the `configKeys` will be added to the
    * message.
    *
    * @param hint
    *   the name of the component
    *
    * @param cause
    *   the cause
    *
    * @param configKeys
    *   the config keys that could be used to autoconfigure the component
    *
    * @param config
    *   the config
    */
  def apply(
      hint: String,
      cause: Throwable,
      configKeys: Set[Config.Key[_]],
      config: Config
  ): AutoConfigureError =
    if (configKeys.nonEmpty) {
      val params = configKeys.toSeq
        .sortBy(_.name)
        .zipWithIndex
        .map { case (key, i) =>
          val name = key.name
          val value =
            config.get[String](key.name).toOption.flatten.getOrElse("N/A")
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
