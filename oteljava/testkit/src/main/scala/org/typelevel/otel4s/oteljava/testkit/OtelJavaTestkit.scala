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

package org.typelevel.otel4s
package oteljava.testkit

import cats.effect.Async
import cats.effect.Resource
import io.opentelemetry.context.propagation.{TextMapPropagator => JTextMapPropagator}
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder
import org.typelevel.otel4s.baggage.BaggageManager
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.logs.LoggerProvider
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.oteljava.baggage.BaggageManagerImpl
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.context.LocalContext
import org.typelevel.otel4s.oteljava.context.LocalContextProvider
import org.typelevel.otel4s.oteljava.testkit.logs.FromLogRecordData
import org.typelevel.otel4s.oteljava.testkit.logs.LogsTestkit
import org.typelevel.otel4s.oteljava.testkit.metrics.FromMetricData
import org.typelevel.otel4s.oteljava.testkit.metrics.MetricsTestkit
import org.typelevel.otel4s.oteljava.testkit.trace.FromSpanData
import org.typelevel.otel4s.oteljava.testkit.trace.TracesTestkit
import org.typelevel.otel4s.trace.TracerProvider

import scala.util.chaining._

sealed abstract class OtelJavaTestkit[F[_]] private (implicit
    val localContext: LocalContext[F]
) extends Otel4s.Unsealed[F]
    with LogsTestkit.Unsealed[F]
    with MetricsTestkit.Unsealed[F]
    with TracesTestkit.Unsealed[F] {

  type Ctx = Context

  val baggageManager: BaggageManager[F] = BaggageManagerImpl.fromLocal

  override def toString: String =
    s"OtelJavaTestkit{meterProvider=$meterProvider, tracerProvider=$tracerProvider, propagators=$propagators}"
}

object OtelJavaTestkit {

  /** Builder for [[OtelJavaTestkit]]. */
  sealed trait Builder[F[_]] {

    /** Adds the meter provider builder customizer. Multiple customizers can be added, and they will be applied in the
      * order they were added.
      *
      * @param customizer
      *   the customizer to add
      */
    def addMeterProviderCustomizer(customizer: SdkMeterProviderBuilder => SdkMeterProviderBuilder): Builder[F]

    /** Adds the tracer provider builder customizer. Multiple customizers can be added, and they will be applied in the
      * order they were added.
      *
      * @param customizer
      *   the customizer to add
      */
    def addTracerProviderCustomizer(customizer: SdkTracerProviderBuilder => SdkTracerProviderBuilder): Builder[F]

    /** Adds the logger provider builder customizer. Multiple customizers can be added, and they will be applied in the
      * order they were added.
      *
      * @param customizer
      *   the customizer to add
      */
    def addLoggerProviderCustomizer(customizer: SdkLoggerProviderBuilder => SdkLoggerProviderBuilder): Builder[F]

    /** Adds propagators to register on the tracer provider. New propagators are appended to the existing collection.
      *
      * @param propagators
      *   the propagators to add
      */
    def addTextMapPropagators(propagators: JTextMapPropagator*): Builder[F]

    /** Sets the propagators used by the tracer provider. Any previously added propagators are discarded.
      *
      * @param propagators
      *   the propagators to use
      */
    def withTextMapPropagators(propagators: Iterable[JTextMapPropagator]): Builder[F]

    /** Sets the `InMemoryLogRecordExporter` to use. Useful when Scala and Java instrumentation need to share the same
      * exporter.
      *
      * @param exporter
      *   the exporter to use
      */
    def withInMemoryLogRecordExporter(exporter: InMemoryLogRecordExporter): Builder[F]

    /** Sets the `InMemoryMetricReader` to use. Useful when Scala and Java instrumentation need to share the same
      * reader.
      *
      * @param reader
      *   the reader to use
      */
    def withInMemoryMetricReader(reader: InMemoryMetricReader): Builder[F]

    /** Sets the `InMemorySpanExporter` to use. Useful when Scala and Java instrumentation need to share the same
      * exporter.
      *
      * @param exporter
      *   the exporter to use
      */
    def withInMemorySpanExporter(exporter: InMemorySpanExporter): Builder[F]

    /** Creates [[OtelJavaTestkit]] using the configuration of this builder. */
    def build: Resource[F, OtelJavaTestkit[F]]

  }

  /** Creates a [[Builder]] of [[OtelJavaTestkit]] with the default configuration. */
  def builder[F[_]: Async: LocalContextProvider]: Builder[F] =
    BuilderImpl[F]()

  /** Creates an [[OtelJavaTestkit]] using [[Builder]]. The instance keeps metrics, logs, and spans in memory.
    *
    * @param customize
    *   a function for customizing the builder
    */
  def inMemory[F[_]: Async: LocalContextProvider](
      customize: Builder[F] => Builder[F] = identity(_)
  ): Resource[F, MetricsTestkit[F]] =
    customize(builder[F]).build

  /** Creates [[OtelJavaTestkit]] that keeps spans and metrics in-memory.
    *
    * @param customizeMeterProviderBuilder
    *   the customization of the meter provider builder
    *
    * @param customizeTracerProviderBuilder
    *   the customization of the tracer provider builder
    *
    * @param customizeLoggerProviderBuilder
    *   the customization of the logger provider builder
    *
    * @param textMapPropagators
    *   the propagators to use
    */
  @deprecated(
    "Use `OtelJavaTestkit.builder` to configure and build the testkit",
    "0.15.0"
  )
  def inMemory[F[_]: Async: LocalContextProvider](
      customizeMeterProviderBuilder: SdkMeterProviderBuilder => SdkMeterProviderBuilder = identity,
      customizeTracerProviderBuilder: SdkTracerProviderBuilder => SdkTracerProviderBuilder = identity,
      customizeLoggerProviderBuilder: SdkLoggerProviderBuilder => SdkLoggerProviderBuilder = identity,
      textMapPropagators: Iterable[JTextMapPropagator] = Nil
  ): Resource[F, OtelJavaTestkit[F]] =
    builder[F]
      .addMeterProviderCustomizer(customizeMeterProviderBuilder)
      .addTracerProviderCustomizer(customizeTracerProviderBuilder)
      .addLoggerProviderCustomizer(customizeLoggerProviderBuilder)
      .withTextMapPropagators(textMapPropagators)
      .build

  @deprecated(
    "Use `OtelJavaTestkit.builder` to configure exporters and build the testkit",
    "0.15.0"
  )
  def fromInMemory[F[_]: Async: LocalContextProvider](
      inMemoryLogRecordExporter: InMemoryLogRecordExporter,
      inMemoryMetricReader: InMemoryMetricReader,
      inMemorySpanExporter: InMemorySpanExporter,
      customizeMeterProviderBuilder: SdkMeterProviderBuilder => SdkMeterProviderBuilder = identity,
      customizeTracerProviderBuilder: SdkTracerProviderBuilder => SdkTracerProviderBuilder = identity,
      customizeLoggerProviderBuilder: SdkLoggerProviderBuilder => SdkLoggerProviderBuilder = identity,
      textMapPropagators: Iterable[JTextMapPropagator] = Nil
  ): Resource[F, OtelJavaTestkit[F]] =
    builder[F]
      .withInMemoryLogRecordExporter(inMemoryLogRecordExporter)
      .withInMemoryMetricReader(inMemoryMetricReader)
      .withInMemorySpanExporter(inMemorySpanExporter)
      .addMeterProviderCustomizer(customizeMeterProviderBuilder)
      .addTracerProviderCustomizer(customizeTracerProviderBuilder)
      .addLoggerProviderCustomizer(customizeLoggerProviderBuilder)
      .withTextMapPropagators(textMapPropagators)
      .build

  private final class Impl[F[_]](
      logs: LogsTestkit[F],
      metrics: MetricsTestkit[F],
      traces: TracesTestkit[F]
  ) extends OtelJavaTestkit[F]()(traces.localContext) {
    def loggerProvider: LoggerProvider[F, Context] = logs.loggerProvider
    def meterProvider: MeterProvider[F] = metrics.meterProvider
    def tracerProvider: TracerProvider[F] = traces.tracerProvider
    def propagators: ContextPropagators[Context] = traces.propagators
    def finishedSpans[A: FromSpanData]: F[List[A]] = traces.finishedSpans
    def resetSpans: F[Unit] = traces.resetSpans
    def collectMetrics[A: FromMetricData]: F[List[A]] = metrics.collectMetrics
    def collectLogs[A: FromLogRecordData]: F[List[A]] = logs.collectLogs
    def resetLogs: F[Unit] = logs.resetLogs
  }

  private final case class BuilderImpl[F[_]: Async: LocalContextProvider](
      meterCustomizer: SdkMeterProviderBuilder => SdkMeterProviderBuilder = identity(_),
      tracerCustomizer: SdkTracerProviderBuilder => SdkTracerProviderBuilder = identity(_),
      loggerCustomizer: SdkLoggerProviderBuilder => SdkLoggerProviderBuilder = identity(_),
      textMapPropagators: Vector[JTextMapPropagator] = Vector.empty,
      logExporter: Option[InMemoryLogRecordExporter] = None,
      metricReader: Option[InMemoryMetricReader] = None,
      spanExporter: Option[InMemorySpanExporter] = None
  ) extends Builder[F] {

    def addMeterProviderCustomizer(customizer: SdkMeterProviderBuilder => SdkMeterProviderBuilder): Builder[F] =
      copy(meterCustomizer = meterCustomizer.andThen(customizer))

    def addTracerProviderCustomizer(customizer: SdkTracerProviderBuilder => SdkTracerProviderBuilder): Builder[F] =
      copy(tracerCustomizer = tracerCustomizer.andThen(customizer))

    def addLoggerProviderCustomizer(customizer: SdkLoggerProviderBuilder => SdkLoggerProviderBuilder): Builder[F] =
      copy(loggerCustomizer = loggerCustomizer.andThen(customizer))

    def addTextMapPropagators(propagators: JTextMapPropagator*): Builder[F] =
      copy(textMapPropagators = textMapPropagators ++ propagators)

    def withTextMapPropagators(propagators: Iterable[JTextMapPropagator]): Builder[F] =
      copy(textMapPropagators = propagators.toVector)

    def withInMemoryLogRecordExporter(exporter: InMemoryLogRecordExporter): Builder[F] =
      copy(logExporter = Some(exporter))

    def withInMemoryMetricReader(reader: InMemoryMetricReader): Builder[F] =
      copy(metricReader = Some(reader))

    def withInMemorySpanExporter(exporter: InMemorySpanExporter): Builder[F] =
      copy(spanExporter = Some(exporter))

    def build: Resource[F, OtelJavaTestkit[F]] =
      Resource.eval(LocalProvider[F, Context].local).flatMap { local =>
        val logsBuilder = LogsTestkit
          .builder[F](Async[F], LocalProvider.fromLocal(local))
          .addLoggerProviderCustomizer(loggerCustomizer)
          .pipe(b => logExporter.fold(b)(b.withInMemoryLogRecordExporter))

        val metricsBuilder = MetricsTestkit
          .builder[F](Async[F], LocalProvider.fromLocal(local))
          .addMeterProviderCustomizer(meterCustomizer)
          .pipe(b => metricReader.fold(b)(b.withInMemoryMetricReader))

        val tracesBuilder = TracesTestkit
          .builder[F](Async[F], LocalProvider.fromLocal(local))
          .addTracerProviderCustomizer(tracerCustomizer)
          .withTextMapPropagators(textMapPropagators)
          .pipe(b => spanExporter.fold(b)(b.withInMemorySpanExporter))

        for {
          logs <- logsBuilder.build
          metrics <- metricsBuilder.build
          traces <- tracesBuilder.build
        } yield new Impl[F](logs, metrics, traces)
      }
  }

}
