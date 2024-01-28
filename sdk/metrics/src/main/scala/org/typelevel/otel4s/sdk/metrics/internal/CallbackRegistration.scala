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
import cats.effect.syntax.monadCancel._
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.foldable._
import org.typelevel.otel4s.sdk.metrics.RegisteredReader

import scala.concurrent.duration.FiniteDuration

private[metrics] final class CallbackRegistration[F[_]: MonadCancelThrow](
    measurements: NonEmptyList[SdkObservableMeasurement[F, _]],
    callback: F[Unit]
) {

  private val descriptors: NonEmptyList[InstrumentDescriptor] =
    measurements.map(_.descriptor)

  private val hasStorages: Boolean =
    measurements.exists(_.storages.nonEmpty)

  def invokeCallback(
      reader: RegisteredReader[F],
      startTimestamp: FiniteDuration,
      collectTimestamp: FiniteDuration
  ): F[Unit] =
    measurements
      .traverse_(_.setActiveReader(reader, startTimestamp, collectTimestamp))
      .flatMap(_ => callback)
      .guarantee(measurements.traverse_(_.unsetActiveReader))
      .whenA(hasStorages)

}
