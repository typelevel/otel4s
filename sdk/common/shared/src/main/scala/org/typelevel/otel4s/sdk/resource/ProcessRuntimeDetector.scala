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

/** Detects runtime details such as name, version, and description.
  *
  * @see
  *   https://opentelemetry.io/docs/specs/semconv/resource/process/#process-runtimes
  */
object ProcessRuntimeDetector extends ProcessRuntimeDetectorPlatform {

  private[sdk] object Const {
    val Name = "process_runtime"
  }

  private[resource] object Keys {
    val Name: AttributeKey[String] =
      AttributeKey("process.runtime.name")

    val Version: AttributeKey[String] =
      AttributeKey("process.runtime.version")

    val Description: AttributeKey[String] =
      AttributeKey("process.runtime.description")
  }

}
