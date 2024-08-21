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
package sdk
package resource

import cats.effect.Sync

/** A detector creates a resource with environment(platform)-specific
  * attributes.
  *
  * @note
  *   a detector that populates resource attributes according to OpenTelemetry
  *   semantic conventions MUST ensure that the resource has a Schema URL set to
  *   a value that matches the semantic conventions.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/resource/sdk/#detecting-resource-information-from-the-environment]]
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
trait TelemetryResourceDetector[F[_]] {

  /** The name of the detector.
    */
  def name: String

  /** Returns the detected resource, if any.
    */
  def detect: F[Option[TelemetryResource]]

  override def toString: String = name
}

object TelemetryResourceDetector {

  /** Returns the default set of resource detectors.
    *
    * Includes:
    *   - host detector
    *   - os detector
    *   - process runtime detector
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    */
  def default[F[_]: Sync]: Set[TelemetryResourceDetector[F]] =
    Set(HostDetector[F], OSDetector[F], ProcessRuntimeDetector[F])

}
