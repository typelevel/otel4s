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
object EnduserExperimentalAttributes {

  /** Unique identifier of an end user in the system. It maybe a username, email address, or other identifier.
    *
    * @note
    *   <p> Unique identifier of an end user in the system. <blockquote> [!Warning] This field contains sensitive (PII)
    *   information.</blockquote>
    */
  val EnduserId: AttributeKey[String] =
    AttributeKey("enduser.id")

  /** Pseudonymous identifier of an end user. This identifier should be a random value that is not directly linked or
    * associated with the end user's actual identity.
    *
    * @note
    *   <p> Pseudonymous identifier of an end user. <blockquote> [!Warning] This field contains sensitive (linkable PII)
    *   information.</blockquote>
    */
  val EnduserPseudoId: AttributeKey[String] =
    AttributeKey("enduser.pseudo.id")

  /** Deprecated, use `user.roles` instead.
    */
  @deprecated("Replaced by `user.roles` attribute.", "")
  val EnduserRole: AttributeKey[String] =
    AttributeKey("enduser.role")

  /** Deprecated, no replacement at this time.
    */
  @deprecated("Removed.", "")
  val EnduserScope: AttributeKey[String] =
    AttributeKey("enduser.scope")

}
