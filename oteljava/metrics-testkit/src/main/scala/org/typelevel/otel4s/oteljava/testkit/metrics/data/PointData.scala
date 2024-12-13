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

package org.typelevel.otel4s.oteljava.testkit
package metrics.data

import cats.Hash
import cats.Show
import cats.syntax.show._
import io.opentelemetry.sdk.metrics.data.{PointData => JPointData}
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.oteljava.AttributeConverters._

import scala.concurrent.duration._

/** A representation of the `io.opentelemetry.sdk.metrics.data.PointData`.
  */
sealed trait PointData[A] {
  def startTimestamp: FiniteDuration
  def recordTimestamp: FiniteDuration
  def attributes: Attributes
  def value: A
}

object PointData {

  def apply[A](
      startTimestamp: FiniteDuration,
      recordTimestamp: FiniteDuration,
      attributes: Attributes,
      value: A
  ): PointData[A] =
    Impl(startTimestamp, recordTimestamp, attributes, value)

  def apply[A <: JPointData, B](point: A, f: A => B): PointData[B] =
    PointData(
      point.getStartEpochNanos.nanos,
      point.getEpochNanos.nanos,
      point.getAttributes.toScala,
      f(point)
    )

  implicit def pointDataHash[A: Hash]: Hash[PointData[A]] =
    Hash.by(p => (p.startTimestamp, p.recordTimestamp, p.attributes, p.value))

  implicit def pointDataShow[A: Show]: Show[PointData[A]] =
    Show.show { p =>
      show"PointData(${p.startTimestamp}, ${p.recordTimestamp}, ${p.attributes}, ${p.value})"
    }

  private final case class Impl[A](
      startTimestamp: FiniteDuration,
      recordTimestamp: FiniteDuration,
      attributes: Attributes,
      value: A
  ) extends PointData[A]
}
