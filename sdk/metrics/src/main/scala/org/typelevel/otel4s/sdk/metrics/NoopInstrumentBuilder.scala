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

import cats.Applicative
import cats.effect.Resource
import cats.effect.std.Console
import cats.syntax.functor._
import org.typelevel.otel4s.metrics.BucketBoundaries
import org.typelevel.otel4s.metrics.Counter
import org.typelevel.otel4s.metrics.Histogram
import org.typelevel.otel4s.metrics.Measurement
import org.typelevel.otel4s.metrics.ObservableCounter
import org.typelevel.otel4s.metrics.ObservableGauge
import org.typelevel.otel4s.metrics.ObservableMeasurement
import org.typelevel.otel4s.metrics.ObservableUpDownCounter
import org.typelevel.otel4s.metrics.UpDownCounter

private object NoopInstrumentBuilder {

  def counter[F[_]: Applicative: Console, A](
      name: String
  ): Counter.Builder[F, A] =
    new Counter.Builder[F, A] {
      def withUnit(unit: String): Counter.Builder[F, A] =
        this

      def withDescription(description: String): Counter.Builder[F, A] =
        this

      def create: F[Counter[F, A]] =
        warn("Counter", name).as(Counter.noop)
    }

  def histogram[F[_]: Applicative: Console, A](
      name: String
  ): Histogram.Builder[F, A] =
    new Histogram.Builder[F, A] {
      def withUnit(unit: String): Histogram.Builder[F, A] =
        this

      def withDescription(description: String): Histogram.Builder[F, A] =
        this

      def withExplicitBucketBoundaries(
          boundaries: BucketBoundaries
      ): Histogram.Builder[F, A] = this

      def create: F[Histogram[F, A]] =
        warn("Histogram", name).as(Histogram.noop)
    }

  def upDownCounter[F[_]: Applicative: Console, A](
      name: String
  ): UpDownCounter.Builder[F, A] =
    new UpDownCounter.Builder[F, A] {
      def withUnit(unit: String): UpDownCounter.Builder[F, A] =
        this

      def withDescription(description: String): UpDownCounter.Builder[F, A] =
        this

      def create: F[UpDownCounter[F, A]] =
        warn("UpDownCounter", name).as(UpDownCounter.noop)
    }

  def observableGauge[F[_]: Applicative: Console, A](
      name: String
  ): ObservableGauge.Builder[F, A] =
    new ObservableGauge.Builder[F, A] {
      def withUnit(unit: String): ObservableGauge.Builder[F, A] =
        this

      def withDescription(description: String): ObservableGauge.Builder[F, A] =
        this

      def createWithCallback(
          cb: ObservableMeasurement[F, A] => F[Unit]
      ): Resource[F, ObservableGauge] =
        createNoop

      def create(
          measurements: F[Iterable[Measurement[A]]]
      ): Resource[F, ObservableGauge] =
        createNoop

      def createObserver: F[ObservableMeasurement[F, A]] =
        warn.as(ObservableMeasurement.noop)

      private def createNoop: Resource[F, ObservableGauge] =
        Resource.eval(warn).as(new ObservableGauge {})

      private def warn: F[Unit] =
        NoopInstrumentBuilder.warn("ObservableGauge", name)
    }

  def observableCounter[F[_]: Applicative: Console, A](
      name: String
  ): ObservableCounter.Builder[F, A] =
    new ObservableCounter.Builder[F, A] {
      def withUnit(unit: String): ObservableCounter.Builder[F, A] =
        this

      def withDescription(
          description: String
      ): ObservableCounter.Builder[F, A] =
        this

      def createWithCallback(
          cb: ObservableMeasurement[F, A] => F[Unit]
      ): Resource[F, ObservableCounter] =
        createNoop

      def create(
          measurements: F[Iterable[Measurement[A]]]
      ): Resource[F, ObservableCounter] =
        createNoop

      def createObserver: F[ObservableMeasurement[F, A]] =
        warn.as(ObservableMeasurement.noop)

      private def createNoop: Resource[F, ObservableCounter] =
        Resource.eval(warn).as(new ObservableCounter {})

      private def warn: F[Unit] =
        NoopInstrumentBuilder.warn("ObservableCounter", name)
    }

  def observableUpDownCounter[F[_]: Applicative: Console, A](
      name: String
  ): ObservableUpDownCounter.Builder[F, A] =
    new ObservableUpDownCounter.Builder[F, A] {
      def withUnit(unit: String): ObservableUpDownCounter.Builder[F, A] =
        this

      def withDescription(
          description: String
      ): ObservableUpDownCounter.Builder[F, A] =
        this

      def createWithCallback(
          cb: ObservableMeasurement[F, A] => F[Unit]
      ): Resource[F, ObservableUpDownCounter] =
        createNoop

      def create(
          measurements: F[Iterable[Measurement[A]]]
      ): Resource[F, ObservableUpDownCounter] =
        createNoop

      def createObserver: F[ObservableMeasurement[F, A]] =
        warn.as(ObservableMeasurement.noop)

      private def createNoop: Resource[F, ObservableUpDownCounter] =
        Resource.eval(warn).as(new ObservableUpDownCounter {})

      private def warn: F[Unit] =
        NoopInstrumentBuilder.warn("ObservableUpDownCounter", name)
    }

  private def warn[F[_]: Console](instrument: String, name: String): F[Unit] =
    Console[F].errorln(
      s"SdkMeter: $instrument instrument has invalid name [$name]. Using noop instrument. " +
        "Instrument names must consist of 255 or fewer characters including alphanumeric, _, ., -, and start with a letter."
    )

}
