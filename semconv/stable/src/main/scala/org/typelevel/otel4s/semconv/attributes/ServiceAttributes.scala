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
package attributes

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/attributes/SemanticAttributes.scala.j2
object ServiceAttributes {

  /** Logical name of the service. <p>
    * @note
    *   <p> MUST be the same for all instances of horizontally scaled services.
    *   If the value was not specified, SDKs MUST fallback to `unknown_service:`
    *   concatenated with <a href="process.md">`process.executable.name`</a>,
    *   e.g. `unknown_service:bash`. If `process.executable.name` is not
    *   available, the value MUST be set to `unknown_service`.
    */
  val ServiceName: AttributeKey[String] =
    AttributeKey("service.name")

  /** The version string of the service API or implementation. The format is not
    * defined by these conventions.
    */
  val ServiceVersion: AttributeKey[String] =
    AttributeKey("service.version")

}
