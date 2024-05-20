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

package org.typelevel.otel4s.sdk.metrics.internal

import cats.data.NonEmptyList
import cats.effect.MonadCancelThrow
import cats.syntax.applicative._
import cats.syntax.foldable._
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import org.typelevel.otel4s.sdk.metrics.internal.exporter.RegisteredReader

private[metrics] final class CallbackRegistration[F[_]: MonadCancelThrow](
    measurements: NonEmptyList[SdkObservableMeasurement[F, _]],
    callback: F[Unit]
) {

  private val hasStorages: Boolean =
    measurements.exists(_.hasStorages)

  /** Set the active reader on each observable measurement so that measurements
    * are only recorded to relevant storages.
    *
    * @param reader
    *   the reader to use
    *
    * @param timeWindow
    *   the time window of the measurement
    */
  def invokeCallback(
      reader: RegisteredReader[F],
      timeWindow: TimeWindow
  ): F[Unit] =
    measurements
      .traverse_(_.withActiveReader(reader, timeWindow))
      .surround(callback)
      .whenA(hasStorages)

  override def toString: String =
    s"CallbackRegistration{instrumentDescriptors=${measurements.map(_.descriptor).mkString_("[", ", ", "]")}"
}
