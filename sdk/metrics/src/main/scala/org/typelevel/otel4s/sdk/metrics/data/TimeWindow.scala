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

package org.typelevel.otel4s.sdk.metrics.data

import cats.Hash
import cats.Show

import scala.concurrent.duration.FiniteDuration

/** Represents a time window for data collection.
  */
sealed trait TimeWindow {

  /** The start of the time window.
    */
  def start: FiniteDuration

  /** The end of the time window. In most cases represents the sampling (collection) time.
    */
  def end: FiniteDuration

  override final def hashCode(): Int =
    Hash[TimeWindow].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: TimeWindow => Hash[TimeWindow].eqv(this, other)
      case _                 => false
    }

  override final def toString: String =
    Show[TimeWindow].show(this)
}

object TimeWindow {

  /** Creates a [[TimeWindow]] with the given values.
    *
    * @param start
    *   the start of the window
    *
    * @param end
    *   the end of the window
    */
  def apply(start: FiniteDuration, end: FiniteDuration): TimeWindow = {
    require(end >= start, "end must be greater than or equal to start")
    Impl(start, end)
  }

  implicit val timeWindowHash: Hash[TimeWindow] =
    Hash.by(w => (w.start, w.end))

  implicit val timeWindowShow: Show[TimeWindow] =
    Show.show(w => s"TimeWindow{start=${w.start}, end=${w.end}}")

  private final case class Impl(
      start: FiniteDuration,
      end: FiniteDuration
  ) extends TimeWindow
}
