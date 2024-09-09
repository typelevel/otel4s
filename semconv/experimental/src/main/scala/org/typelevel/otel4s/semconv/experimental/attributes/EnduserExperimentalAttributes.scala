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

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/attributes/experimental/SemanticAttributes.scala.j2
object EnduserExperimentalAttributes {

  /** Deprecated, use `user.id` instead.
    */
  @deprecated("Replaced by `user.id` attribute.", "")
  val EnduserId: AttributeKey[String] =
    AttributeKey("enduser.id")

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
