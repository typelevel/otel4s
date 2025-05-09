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

/** Indicates stability of the attribute.
  */
sealed trait Stability

object Stability {

  def stable: Stability = Stable
  def development: Stability = Development
  def releaseCandidate: Stability = ReleaseCandidate

  private case object Stable extends Stability
  private case object Development extends Stability
  private case object ReleaseCandidate extends Stability
}
