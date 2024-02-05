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

final class ConfigurationError(
    message: String,
    cause: Option[Throwable]
) extends RuntimeException(message, cause.orNull)

object ConfigurationError {

  def apply(message: String): ConfigurationError =
    new ConfigurationError(message, None)

  def apply(message: String, cause: Throwable): ConfigurationError =
    new ConfigurationError(message, Some(cause))

  def unrecognized(key: String, value: String): ConfigurationError =
    new ConfigurationError(s"Unrecognized value for [$key]: $value", None)

  def unrecognized(
      key: String,
      value: String,
      supported: Set[String]
  ): ConfigurationError =
    new ConfigurationError(
      s"Unrecognized value for [$key]: $value. Supported options [${supported.mkString(", ")}]",
      None
    )

}
