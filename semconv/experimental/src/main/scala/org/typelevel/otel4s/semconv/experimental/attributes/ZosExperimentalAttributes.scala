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
object ZosExperimentalAttributes {

  /** The System Management Facility (SMF) Identifier uniquely identified a z/OS system within a SYSPLEX or mainframe
    * environment and is used for system and performance analysis.
    */
  val ZosSmfId: AttributeKey[String] =
    AttributeKey("zos.smf.id")

  /** The name of the SYSPLEX to which the z/OS system belongs too.
    */
  val ZosSysplexName: AttributeKey[String] =
    AttributeKey("zos.sysplex.name")

}
