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

package org.typelevel.otel4s.sdk.metrics

import cats.data.NonEmptyList
import cats.effect.MonadCancelThrow
import cats.effect.Resource
import cats.syntax.functor._
import cats.syntax.traverse._
import org.typelevel.otel4s.metrics.BatchCallback
import org.typelevel.otel4s.metrics.ObservableMeasurement
import org.typelevel.otel4s.sdk.common.Diagnostic
import org.typelevel.otel4s.sdk.metrics.internal.CallbackRegistration
import org.typelevel.otel4s.sdk.metrics.internal.MeterSharedState
import org.typelevel.otel4s.sdk.metrics.internal.SdkObservableMeasurement

private class SdkBatchCallback[F[_]: MonadCancelThrow: Diagnostic](
    sharedState: MeterSharedState[F]
) extends BatchCallback.Unsealed[F] {

  def apply(
      callback: F[Unit],
      observable: ObservableMeasurement[F, _],
      rest: ObservableMeasurement[F, _]*
  ): Resource[F, Unit] = {
    val scope = sharedState.scope

    val all = (observable +: rest).toList
      .flatTraverse[F, SdkObservableMeasurement[F, _]] {
        case o: SdkObservableMeasurement[F, _] if o.scope == scope =>
          MonadCancelThrow[F].pure(List(o))

        case _: SdkObservableMeasurement[F, _] =>
          Diagnostic[F]
            .error(
              "SdkBatchCallback: called with instruments that belong to a different Meter."
            )
            .as(Nil)

        case _ =>
          Diagnostic[F]
            .error(
              "SdkBatchCallback: called with instruments that were not created by the SDK."
            )
            .as(Nil)
      }

    Resource.eval(all).flatMap { sdkMeasurements =>
      NonEmptyList.fromList(sdkMeasurements) match {
        case Some(measurements) =>
          sharedState.withCallback(
            new CallbackRegistration(measurements, callback)
          )

        case None =>
          Resource.unit
      }
    }
  }

}
