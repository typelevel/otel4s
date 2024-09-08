/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.semconv

/** Indicates requirement level of the attribute.
  */
sealed trait Requirement {
  def level: Requirement.Level
  def note: Option[String]
}

object Requirement {
  sealed trait Level
  object Level {
    case object Required extends Level
    case object ConditionalRequired extends Level
    case object Recommended extends Level
    case object OptIn extends Level
  }

  def required: Requirement =
    Impl(Level.Required, None)

  def conditionallyRequired: Requirement =
    Impl(Level.ConditionalRequired, None)

  def conditionallyRequired(note: String): Requirement =
    Impl(Level.ConditionalRequired, Some(note))

  def recommended: Requirement =
    Impl(Level.Recommended, None)

  def recommended(note: String): Requirement =
    Impl(Level.Recommended, Some(note))

  def optIn: Requirement =
    Impl(Level.OptIn, None)

  def optIn(note: String): Requirement =
    Impl(Level.OptIn, Some(note))

  private final case class Impl(
      level: Level,
      note: Option[String]
  ) extends Requirement

}
