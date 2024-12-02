/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s.metrics

import cats.Apply
import cats.effect.Resource
import cats.syntax.apply._

trait BatchCallback[F[_]] {

  /** Constructs a batch callback.
    *
    * Batch callbacks allow a single callback to observe measurements for multiple asynchronous instruments.
    *
    * The callback will be called when the instruments are being observed.
    *
    * Callbacks are expected to abide by the following restrictions:
    *   - Short-living and (ideally) non-blocking
    *   - Run in a finite amount of time
    *   - Safe to call repeatedly, across multiple threads
    *
    * @example
    *   {{{
    * val meter: Meter[F] = ???
    * val server: F[Unit] = ??? // runs the server
    *
    * val background: Resource[F, Unit] =
    *   for {
    *     counter <- Resource.eval(meter.observableCounter[Long]("counter").createObserver)
    *     upDownCounter <- Resource.eval(meter.observableUpDownCounter[Double]("up-down-counter").createObserver)
    *     gauge <- Resource.eval(meter.observableGauge[Double]("gauge").createObserver)
    *     callback = counter.record(1L) *> upDownCounter.record(2.0) *> gauge.record(3.0)
    *     _ <- meter.batchCallback(callback, counter, upDownCounter, gauge)
    *   } yield ()
    *
    * background.surround(server) // register batch callback and run the server
    *   }}}
    *
    * @param callback
    *   the callback to observe values on-demand
    *
    * @param observable
    *   the instrument for which the callback may observe values
    *
    * @param rest
    *   the instruments for which the callback may observe values
    */
  def apply(
      callback: F[Unit],
      observable: ObservableMeasurement[F, _],
      rest: ObservableMeasurement[F, _]*
  ): Resource[F, Unit]

  /** Constructs a batch callback.
    *
    * Batch callbacks allow a single callback to observe measurements for multiple asynchronous instruments.
    *
    * The callback will be called when the instruments are being observed.
    *
    * Callbacks are expected to abide by the following restrictions:
    *   - Short-living and (ideally) non-blocking
    *   - Run in a finite amount of time
    *   - Safe to call repeatedly, across multiple threads
    *
    * @example
    *   {{{
    * val meter: Meter[F] = ???
    * val server: F[Unit] = ??? // runs the server
    *
    * val background: Resource[F, Unit] =
    *   meter.batchCallback.of(
    *     meter.observableCounter[Long]("counter").createObserver,
    *     meter.observableUpDownCounter[Double]("up-down-counter").createObserver,
    *     meter.observableGauge[Double]("gauge").createObserver
    *   ) { (counter, upDownCounter, gauge) =>
    *     counter.record(1L) *> upDownCounter.record(2.0) *> gauge.record(3.0)
    *   }
    *
    * background.surround(server) // register batch callback and run the server
    *   }}}
    */
  final def of[A1, A2](
      a1: F[ObservableMeasurement[F, A1]],
      a2: F[ObservableMeasurement[F, A2]]
  )(
      cb: (
          ObservableMeasurement[F, A1],
          ObservableMeasurement[F, A2]
      ) => F[Unit]
  )(implicit F: Apply[F]): Resource[F, Unit] =
    Resource
      .eval((a1, a2).tupled)
      .flatMap { case (a1, a2) => apply(cb(a1, a2), a1, a2) }

  final def of[A1, A2, A3](
      a1: F[ObservableMeasurement[F, A1]],
      a2: F[ObservableMeasurement[F, A2]],
      a3: F[ObservableMeasurement[F, A3]]
  )(
      cb: (
          ObservableMeasurement[F, A1],
          ObservableMeasurement[F, A2],
          ObservableMeasurement[F, A3]
      ) => F[Unit]
  )(implicit F: Apply[F]): Resource[F, Unit] =
    Resource
      .eval((a1, a2, a3).tupled)
      .flatMap { case (a1, a2, a3) => apply(cb(a1, a2, a3), a1, a2, a3) }

  final def of[A1, A2, A3, A4](
      a1: F[ObservableMeasurement[F, A1]],
      a2: F[ObservableMeasurement[F, A2]],
      a3: F[ObservableMeasurement[F, A3]],
      a4: F[ObservableMeasurement[F, A4]]
  )(
      cb: (
          ObservableMeasurement[F, A1],
          ObservableMeasurement[F, A2],
          ObservableMeasurement[F, A3],
          ObservableMeasurement[F, A4]
      ) => F[Unit]
  )(implicit F: Apply[F]): Resource[F, Unit] =
    Resource
      .eval((a1, a2, a3, a4).tupled)
      .flatMap { case (a1, a2, a3, a4) =>
        apply(cb(a1, a2, a3, a4), a1, a2, a3, a4)
      }

  final def of[A1, A2, A3, A4, A5](
      a1: F[ObservableMeasurement[F, A1]],
      a2: F[ObservableMeasurement[F, A2]],
      a3: F[ObservableMeasurement[F, A3]],
      a4: F[ObservableMeasurement[F, A4]],
      a5: F[ObservableMeasurement[F, A5]]
  )(
      cb: (
          ObservableMeasurement[F, A1],
          ObservableMeasurement[F, A2],
          ObservableMeasurement[F, A3],
          ObservableMeasurement[F, A4],
          ObservableMeasurement[F, A5]
      ) => F[Unit]
  )(implicit F: Apply[F]): Resource[F, Unit] =
    Resource
      .eval((a1, a2, a3, a4, a5).tupled)
      .flatMap { case (a1, a2, a3, a4, a5) =>
        apply(cb(a1, a2, a3, a4, a5), a1, a2, a3, a4, a5)
      }

  final def of[A1, A2, A3, A4, A5, A6](
      a1: F[ObservableMeasurement[F, A1]],
      a2: F[ObservableMeasurement[F, A2]],
      a3: F[ObservableMeasurement[F, A3]],
      a4: F[ObservableMeasurement[F, A4]],
      a5: F[ObservableMeasurement[F, A5]],
      a6: F[ObservableMeasurement[F, A6]],
  )(
      cb: (
          ObservableMeasurement[F, A1],
          ObservableMeasurement[F, A2],
          ObservableMeasurement[F, A3],
          ObservableMeasurement[F, A4],
          ObservableMeasurement[F, A5],
          ObservableMeasurement[F, A6]
      ) => F[Unit]
  )(implicit F: Apply[F]): Resource[F, Unit] =
    Resource
      .eval((a1, a2, a3, a4, a5, a6).tupled)
      .flatMap { case (a1, a2, a3, a4, a5, a6) =>
        apply(cb(a1, a2, a3, a4, a5, a6), a1, a2, a3, a4, a5, a6)
      }

  final def of[A1, A2, A3, A4, A5, A6, A7](
      a1: F[ObservableMeasurement[F, A1]],
      a2: F[ObservableMeasurement[F, A2]],
      a3: F[ObservableMeasurement[F, A3]],
      a4: F[ObservableMeasurement[F, A4]],
      a5: F[ObservableMeasurement[F, A5]],
      a6: F[ObservableMeasurement[F, A6]],
      a7: F[ObservableMeasurement[F, A7]]
  )(
      cb: (
          ObservableMeasurement[F, A1],
          ObservableMeasurement[F, A2],
          ObservableMeasurement[F, A3],
          ObservableMeasurement[F, A4],
          ObservableMeasurement[F, A5],
          ObservableMeasurement[F, A6],
          ObservableMeasurement[F, A7]
      ) => F[Unit]
  )(implicit F: Apply[F]): Resource[F, Unit] =
    Resource
      .eval((a1, a2, a3, a4, a5, a6, a7).tupled)
      .flatMap { case (a1, a2, a3, a4, a5, a6, a7) =>
        apply(cb(a1, a2, a3, a4, a5, a6, a7), a1, a2, a3, a4, a5, a6, a7)
      }

  final def of[A1, A2, A3, A4, A5, A6, A7, A8](
      a1: F[ObservableMeasurement[F, A1]],
      a2: F[ObservableMeasurement[F, A2]],
      a3: F[ObservableMeasurement[F, A3]],
      a4: F[ObservableMeasurement[F, A4]],
      a5: F[ObservableMeasurement[F, A5]],
      a6: F[ObservableMeasurement[F, A6]],
      a7: F[ObservableMeasurement[F, A7]],
      a8: F[ObservableMeasurement[F, A8]]
  )(
      cb: (
          ObservableMeasurement[F, A1],
          ObservableMeasurement[F, A2],
          ObservableMeasurement[F, A3],
          ObservableMeasurement[F, A4],
          ObservableMeasurement[F, A5],
          ObservableMeasurement[F, A6],
          ObservableMeasurement[F, A7],
          ObservableMeasurement[F, A8]
      ) => F[Unit]
  )(implicit F: Apply[F]): Resource[F, Unit] =
    Resource
      .eval((a1, a2, a3, a4, a5, a6, a7, a8).tupled)
      .flatMap { case (a1, a2, a3, a4, a5, a6, a7, a8) =>
        apply(
          cb(a1, a2, a3, a4, a5, a6, a7, a8),
          a1,
          a2,
          a3,
          a4,
          a5,
          a6,
          a7,
          a8
        )
      }

  final def of[A1, A2, A3, A4, A5, A6, A7, A8, A9](
      a1: F[ObservableMeasurement[F, A1]],
      a2: F[ObservableMeasurement[F, A2]],
      a3: F[ObservableMeasurement[F, A3]],
      a4: F[ObservableMeasurement[F, A4]],
      a5: F[ObservableMeasurement[F, A5]],
      a6: F[ObservableMeasurement[F, A6]],
      a7: F[ObservableMeasurement[F, A7]],
      a8: F[ObservableMeasurement[F, A8]],
      a9: F[ObservableMeasurement[F, A9]]
  )(
      cb: (
          ObservableMeasurement[F, A1],
          ObservableMeasurement[F, A2],
          ObservableMeasurement[F, A3],
          ObservableMeasurement[F, A4],
          ObservableMeasurement[F, A5],
          ObservableMeasurement[F, A6],
          ObservableMeasurement[F, A7],
          ObservableMeasurement[F, A8],
          ObservableMeasurement[F, A9]
      ) => F[Unit]
  )(implicit F: Apply[F]): Resource[F, Unit] =
    Resource
      .eval((a1, a2, a3, a4, a5, a6, a7, a8, a9).tupled)
      .flatMap { case (a1, a2, a3, a4, a5, a6, a7, a8, a9) =>
        apply(
          cb(a1, a2, a3, a4, a5, a6, a7, a8, a9),
          a1,
          a2,
          a3,
          a4,
          a5,
          a6,
          a7,
          a8,
          a9
        )
      }

}

object BatchCallback {

  def noop[F[_]]: BatchCallback[F] =
    new BatchCallback[F] {
      def apply(
          callback: F[Unit],
          observable: ObservableMeasurement[F, _],
          rest: ObservableMeasurement[F, _]*
      ): Resource[F, Unit] =
        Resource.unit
    }

}
