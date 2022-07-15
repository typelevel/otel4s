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

package org.typelevel.otel4s
package java

import cats.effect.LiftIO
import cats.effect.Sync
import cats.syntax.functor._
import io.opentelemetry.api.{OpenTelemetry => JOpenTelemetry}
import io.opentelemetry.api.common.{Attributes => JAttributes}
import io.opentelemetry.api.metrics.{DoubleHistogram => JDoubleHistogram}
import io.opentelemetry.api.metrics.{LongCounter => JLongCounter}
import io.opentelemetry.api.metrics.{LongUpDownCounter => JLongUpDownCounter}
import io.opentelemetry.api.metrics.{Meter => JMeter}
import org.typelevel.otel4s.java.trace._
import org.typelevel.otel4s.metrics._
import org.typelevel.otel4s.trace._

object OtelJava {

  def forSync[F[_]: LiftIO](
      jOtel: JOpenTelemetry
  )(implicit F: Sync[F]): F[Otel4s[F]] =
    TraceProviderImpl.ioLocal(jOtel).map { provider =>
      new Otel4s[F] {
        val meterProvider: MeterProvider[F] = new MeterProviderImpl[F](jOtel)
        val traceProvider: TraceProvider[F] = provider
      }
    }

  private class MeterProviderImpl[F[_]: Sync](jOtel: JOpenTelemetry)
      extends MeterProvider[F] {
    def meter(name: String): MeterBuilder[F] = new MeterBuilderImpl(jOtel, name)
  }

  private case class MeterBuilderImpl[F[_]](
      jOtel: JOpenTelemetry,
      name: String,
      version: Option[String] = None,
      schemaUrl: Option[String] = None
  )(implicit F: Sync[F])
      extends MeterBuilder[F] {
    def withVersion(version: String): MeterBuilder[F] =
      copy(version = Option(version))
    def withSchemaUrl(schemaUrl: String): MeterBuilder[F] =
      copy(schemaUrl = Option(schemaUrl))

    def get: F[Meter[F]] = F.delay {
      val b = jOtel.meterBuilder(name)
      version.foreach(b.setInstrumentationVersion)
      schemaUrl.foreach(b.setSchemaUrl)
      new MeterImpl(b.build())
    }
  }

  private class MeterImpl[F[_]: Sync](jMeter: JMeter) extends Meter[F] {
    def counter(name: String): SyncInstrumentBuilder[F, Counter[F, Long]] =
      new CounterBuilderImpl(jMeter, name)

    def histogram(
        name: String
    ): SyncInstrumentBuilder[F, Histogram[F, Double]] =
      new HistogramBuilderImpl(jMeter, name)

    def upDownCounter(
        name: String
    ): SyncInstrumentBuilder[F, UpDownCounter[F, Long]] =
      new UpDownCounterBuilderImpl(jMeter, name)
  }

  private case class CounterBuilderImpl[F[_]](
      jMeter: JMeter,
      name: String,
      unit: Option[String] = None,
      description: Option[String] = None
  )(implicit F: Sync[F])
      extends SyncInstrumentBuilder[F, Counter[F, Long]] {
    type Self = CounterBuilderImpl[F]

    def withUnit(unit: String): Self = copy(unit = Option(unit))
    def withDescription(description: String): Self =
      copy(description = Option(description))

    def create: F[Counter[F, Long]] = F.delay {
      val b = jMeter.counterBuilder(name)
      unit.foreach(b.setUnit)
      description.foreach(b.setDescription)
      new CounterImpl(b.build)
    }
  }

  private class CounterImpl[F[_]](longCounter: JLongCounter)(implicit
      F: Sync[F]
  ) extends Counter[F, Long] {

    val backend: Counter.Backend[F, Long] =
      new Counter.LongBackend[F] {
        val isEnabled: Boolean = true

        def add(value: Long, attributes: Attribute[_]*): F[Unit] =
          F.delay(longCounter.add(value, toJAttributes(attributes)))
      }

  }

  private case class HistogramBuilderImpl[F[_]](
      jMeter: JMeter,
      name: String,
      unit: Option[String] = None,
      description: Option[String] = None
  )(implicit F: Sync[F])
      extends SyncInstrumentBuilder[F, Histogram[F, Double]] {
    type Self = HistogramBuilderImpl[F]

    def withUnit(unit: String): Self = copy(unit = Option(unit))
    def withDescription(description: String): Self =
      copy(description = Option(description))

    def create: F[Histogram[F, Double]] = F.delay {
      val b = jMeter.histogramBuilder(name)
      unit.foreach(b.setUnit)
      description.foreach(b.setDescription)
      new HistogramImpl(b.build)
    }
  }

  private class HistogramImpl[F[_]](histogram: JDoubleHistogram)(implicit
      F: Sync[F]
  ) extends Histogram[F, Double] {

    val backend: Histogram.Backend[F, Double] =
      new Histogram.DoubleBackend[F] {
        val isEnabled: Boolean = true
        def record(value: Double, attributes: Attribute[_]*): F[Unit] =
          F.delay(histogram.record(value, toJAttributes(attributes)))
      }
  }

  private case class UpDownCounterBuilderImpl[F[_]](
      jMeter: JMeter,
      name: String,
      unit: Option[String] = None,
      description: Option[String] = None
  )(implicit F: Sync[F])
      extends SyncInstrumentBuilder[F, UpDownCounter[F, Long]] {
    type Self = UpDownCounterBuilderImpl[F]

    def withUnit(unit: String) =
      copy(unit = Option(unit))

    def withDescription(description: String) =
      copy(description = Option(description))

    def create: F[UpDownCounter[F, Long]] = F.delay {
      val b = jMeter.upDownCounterBuilder(name)
      unit.foreach(b.setUnit)
      description.foreach(b.setDescription)
      new UpDownCounterImpl(b.build)
    }
  }

  private class UpDownCounterImpl[F[_]](counter: JLongUpDownCounter)(implicit
      F: Sync[F]
  ) extends UpDownCounter[F, Long] {

    val backend: UpDownCounter.Backend[F, Long] =
      new UpDownCounter.LongBackend[F] {
        val isEnabled: Boolean = true

        def add(value: Long, attributes: Attribute[_]*): F[Unit] =
          F.delay(counter.add(value, toJAttributes(attributes)))

      }
  }

  private def toJAttributes(attributes: Seq[Attribute[_]]): JAttributes =
    Conversions.toJAttributes(attributes)
}
