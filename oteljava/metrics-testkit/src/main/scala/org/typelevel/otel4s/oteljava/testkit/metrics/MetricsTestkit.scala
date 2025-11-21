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

package org.typelevel.otel4s.oteljava.testkit.metrics

import cats.effect.Async
import cats.effect.Resource
import cats.mtl.Ask
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.oteljava.context.{AskContext, Context, LocalContextProvider}
import org.typelevel.otel4s.oteljava.metrics.MeterProviderImpl

import scala.jdk.CollectionConverters._

sealed trait MetricsTestkit[F[_]] {

  /** The [[org.typelevel.otel4s.metrics.MeterProvider MeterProvider]].
    */
  def meterProvider: MeterProvider[F]

  /** Collects and returns metrics.
    *
    * @example
    *   {{{
    * import io.opentelemetry.sdk.metrics.data.MetricData
    * import org.typelevel.otel4s.oteljava.testkit.metrics.data.Metric
    *
    * MetricTestkit[F].collectMetrics[MetricData] // OpenTelemetry Java models
    * MetricTestkit[F].collectMetrics[Metric] // Otel4s testkit models
    *   }}}
    *
    * @note
    *   metrics are recollected on each invocation.
    */
  def collectMetrics[A: FromMetricData]: F[List[A]]

}

object MetricsTestkit {
  private[oteljava] trait Unsealed[F[_]] extends MetricsTestkit[F]

  /** Builder for [[MetricsTestkit]]. */
  sealed trait Builder[F[_]] {

    /** Adds the meter provider builder customizer. Multiple customizers can be added, and they will be applied in the
      * order they were added.
      *
      * @param customizer
      *   the customizer to add
      */
    def addMeterProviderCustomizer(customizer: SdkMeterProviderBuilder => SdkMeterProviderBuilder): Builder[F]

    /** Sets the `InMemoryMetricReader` to use. Useful when Scala and Java instrumentation need to share the same
      * reader.
      *
      * @param reader
      *   the reader to use
      */
    def withInMemoryMetricReader(reader: InMemoryMetricReader): Builder[F]

    /** Creates [[MetricsTestkit]] using the configuration of this builder. */
    def build: Resource[F, MetricsTestkit[F]]

  }

  /** Creates a [[Builder]] of [[MetricsTestkit]] with the default configuration. */
  def builder[F[_]: Async: LocalContextProvider]: Builder[F] =
    new BuilderImpl[F]()

  /** Creates a [[MetricsTestkit]] using [[Builder]] with the default configuration. The instance keeps metrics in
    * memory.
    */
  def inMemory[F[_]: Async: LocalContextProvider]: Resource[F, MetricsTestkit[F]] =
    builder[F].build

  /** Creates a [[MetricsTestkit]] using [[Builder]]. The instance keeps metrics in memory.
    *
    * @param customize
    *   a function for customizing the builder
    */
  def inMemory[F[_]: Async: LocalContextProvider](
      customize: Builder[F] => Builder[F]
  ): Resource[F, MetricsTestkit[F]] =
    customize(builder[F]).build

  /** Creates [[MetricsTestkit]] that keeps metrics in-memory.
    *
    * @note
    *   the implementation does not record exemplars. Use `OtelJavaTestkit` if you need to record exemplars.
    *
    * @param customize
    *   the customization of the builder
    */
  @deprecated(
    "Use `MetricsTestkit.inMemory` overloaded alternative with `Builder[F]` customizer or `MetricsTestkit.builder`",
    "0.15.0"
  )
  def inMemory[F[_]: Async](
      customize: SdkMeterProviderBuilder => SdkMeterProviderBuilder = identity
  ): Resource[F, MetricsTestkit[F]] = {
    implicit val askContext: AskContext[F] = Ask.const(Context.root)
    for {
      inMemoryMetricReader <- Resource.eval(Async[F].delay(InMemoryMetricReader.create()))
      metricsTestkit <- create[F](inMemoryMetricReader, customize)
    } yield metricsTestkit
  }

  /** Creates [[MetricsTestkit]] that keeps metrics in-memory from an existing reader. Useful when a Scala
    * instrumentation requires a Java instrumentation, both sharing the same reader.
    *
    * @note
    *   the implementation does not record exemplars. Use `OtelJavaTestkit` if you need to record exemplars.
    *
    * @param inMemoryMetricReader
    *   the reader to use
    *
    * @param customize
    *   the customization of the builder
    */
  @deprecated("Use `MetricsTestkit.builder` to provide the reader or `MetricsTestkit.inMemory` for defaults", "0.15.0")
  def fromInMemory[F[_]: Async](
      inMemoryMetricReader: InMemoryMetricReader,
      customize: SdkMeterProviderBuilder => SdkMeterProviderBuilder = identity
  ): Resource[F, MetricsTestkit[F]] = {
    implicit val askContext: AskContext[F] = Ask.const(Context.root)
    create[F](inMemoryMetricReader, customize)
  }

  private[oteljava] def create[F[_]: Async: AskContext](
      customize: SdkMeterProviderBuilder => SdkMeterProviderBuilder
  ): Resource[F, MetricsTestkit[F]] = {
    for {
      inMemoryMetricReader <- Resource.eval(Async[F].delay(InMemoryMetricReader.create()))
      metricsTestkit <- create[F](inMemoryMetricReader, customize)
    } yield metricsTestkit
  }

  private def create[F[_]: Async: AskContext](
      inMemoryMetricReader: InMemoryMetricReader,
      customize: SdkMeterProviderBuilder => SdkMeterProviderBuilder
  ): Resource[F, MetricsTestkit[F]] = {
    def createMetricProvider(metricReader: InMemoryMetricReader) =
      Async[F].delay {
        val builder = SdkMeterProvider
          .builder()
          .registerMetricReader(metricReader)

        customize(builder).build()
      }

    for {
      provider <- Resource.fromAutoCloseable(createMetricProvider(inMemoryMetricReader))
    } yield new Impl(
      new MeterProviderImpl(provider),
      inMemoryMetricReader
    )
  }

  private final class Impl[F[_]: Async](
      val meterProvider: MeterProvider[F],
      metricReader: InMemoryMetricReader
  ) extends MetricsTestkit[F] {

    def collectMetrics[A: FromMetricData]: F[List[A]] =
      Async[F].delay {
        val metrics = metricReader.collectAllMetrics().asScala.toList
        metrics.map(FromMetricData[A].from)
      }
  }

  private final case class BuilderImpl[F[_]: Async: LocalContextProvider](
      customizer: SdkMeterProviderBuilder => SdkMeterProviderBuilder = identity(_),
      inMemoryMetricReader: Option[InMemoryMetricReader] = None
  ) extends Builder[F] {

    def addMeterProviderCustomizer(customizer: SdkMeterProviderBuilder => SdkMeterProviderBuilder): Builder[F] =
      copy(customizer = this.customizer.andThen(customizer))

    def withInMemoryMetricReader(reader: InMemoryMetricReader): Builder[F] =
      copy(inMemoryMetricReader = Some(reader))

    def build: Resource[F, MetricsTestkit[F]] =
      Resource.eval(LocalProvider[F, Context].local).flatMap { implicit local =>
        inMemoryMetricReader.fold(create[F](customizer))(create[F](_, customizer))
      }
  }

}
