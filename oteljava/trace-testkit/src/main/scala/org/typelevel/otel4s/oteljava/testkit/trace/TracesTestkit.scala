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
package trace

import cats.effect.Async
import cats.effect.Resource
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.opentelemetry.context.propagation.{TextMapPropagator => JTextMapPropagator}
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.`export`.SpanExporter
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.context.LocalContext
import org.typelevel.otel4s.oteljava.context.LocalContextProvider
import org.typelevel.otel4s.oteljava.context.propagation.PropagatorConverters._
import org.typelevel.otel4s.oteljava.trace.TraceScopeImpl
import org.typelevel.otel4s.oteljava.trace.TracerProviderImpl
import org.typelevel.otel4s.trace.TracerProvider

import scala.annotation.nowarn
import scala.jdk.CollectionConverters._

sealed trait TracesTestkit[F[_]] {

  /** The [[org.typelevel.otel4s.trace.TracerProvider TracerProvider]].
    */
  def tracerProvider: TracerProvider[F]

  /** The list of finished spans (OpenTelemetry Java models).
    *
    * @see
    *   [[resetSpans]] to reset the internal buffer
    */
  def finishedSpans: F[List[SpanData]]

  /** The list of finished spans (OpenTelemetry Java models).
    *
    * @example
    *   {{{
    * import io.opentelemetry.sdk.trace.data.SpanData
    *
    * TracesTestkit[F].finishedSpans[SpanData] // OpenTelemetry Java models
    *   }}}
    *
    * @see
    *   [[resetSpans]] to reset the internal buffer
    */
  @deprecated(
    "Use `finishedSpans` without a type parameter to work with OpenTelemetry Java `SpanData`, or use the expectation API for assertions.",
    "1.0.0-RC1"
  )
  def finishedSpans[A: FromSpanData]: F[List[A]]

  /** Resets the internal buffer.
    */
  def resetSpans: F[Unit]

  /** The propagators used by the [[org.typelevel.otel4s.trace.TracerProvider TracerProvider]].
    */
  def propagators: ContextPropagators[Context]

  /** The [[org.typelevel.otel4s.oteljava.context.LocalContext LocalContext]] used by the
    * [[org.typelevel.otel4s.trace.TracerProvider TracerProvider]].
    */
  def localContext: LocalContext[F]
}

object TracesTestkit {
  private[oteljava] trait Unsealed[F[_]] extends TracesTestkit[F]

  /** Builder for [[TracesTestkit]]. */
  sealed trait Builder[F[_]] {

    /** Adds the tracer provider builder customizer. Multiple customizers can be added, and they will be applied in the
      * order they were added.
      *
      * @param customizer
      *   the customizer to add
      */
    def addTracerProviderCustomizer(customizer: SdkTracerProviderBuilder => SdkTracerProviderBuilder): Builder[F]

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

    /** Creates [[TracesTestkit]] using the configuration of this builder. */
    def build: Resource[F, TracesTestkit[F]]

  }

  /** Creates a [[Builder]] of [[TracesTestkit]] with the default configuration. */
  def builder[F[_]: Async: LocalContextProvider]: Builder[F] =
    new BuilderImpl[F]()

  /** Creates a [[TracesTestkit]] using [[Builder]]. The instance keeps spans in memory.
    *
    * @param customize
    *   a function for customizing the builder
    */
  def inMemory[F[_]: Async: LocalContextProvider](
      customize: Builder[F] => Builder[F] = identity[Builder[F]](_)
  ): Resource[F, TracesTestkit[F]] =
    customize(builder[F]).build

  private def create[F[_]: Async: LocalContextProvider](
      inMemorySpanExporter: InMemorySpanExporter,
      customize: SdkTracerProviderBuilder => SdkTracerProviderBuilder,
      textMapPropagators: Iterable[JTextMapPropagator]
  ): Resource[F, TracesTestkit[F]] = {
    def createSpanProcessor(exporter: SpanExporter): F[SimpleSpanProcessor] =
      Async[F].delay(SimpleSpanProcessor.builder(exporter).build)

    def createTracerProvider(processor: SpanProcessor): F[SdkTracerProvider] =
      Async[F].delay {
        val builder = SdkTracerProvider
          .builder()
          .addSpanProcessor(processor)

        customize(builder).build()
      }

    val contextPropagators = ContextPropagators.of(
      JTextMapPropagator.composite(textMapPropagators.asJava).asScala
    )

    for {
      local <- Resource.eval(LocalProvider[F, Context].local)
      processor <- Resource.fromAutoCloseable(createSpanProcessor(inMemorySpanExporter))
      provider <- Resource.fromAutoCloseable(createTracerProvider(processor))
    } yield new Impl(
      TracerProviderImpl.local[F](
        provider,
        contextPropagators,
        TraceScopeImpl.fromLocal[F](local)
      ),
      contextPropagators,
      local,
      processor,
      inMemorySpanExporter
    )
  }

  private final class Impl[F[_]: Async](
      val tracerProvider: TracerProvider[F],
      val propagators: ContextPropagators[Context],
      val localContext: LocalContext[F],
      processor: SpanProcessor,
      exporter: InMemorySpanExporter
  ) extends TracesTestkit[F] {

    @nowarn("msg=Calls to parameterless method finishedSpans")
    def finishedSpans: F[List[SpanData]] =
      for {
        _ <- Conversions.asyncFromCompletableResultCode(
          Async[F].delay(processor.forceFlush())
        )
        result <- Async[F].delay(exporter.getFinishedSpanItems)
      } yield result.asScala.toList

    @nowarn("cat=deprecation")
    def finishedSpans[A: FromSpanData]: F[List[A]] =
      Async[F].map(finishedSpans)(_.map(FromSpanData[A].from))

    def resetSpans: F[Unit] =
      Async[F].delay(exporter.reset())
  }

  private final case class BuilderImpl[F[_]: Async: LocalContextProvider](
      customizer: SdkTracerProviderBuilder => SdkTracerProviderBuilder = identity(_),
      textMapPropagators: Vector[JTextMapPropagator] = Vector.empty
  ) extends Builder[F] {

    def addTracerProviderCustomizer(customizer: SdkTracerProviderBuilder => SdkTracerProviderBuilder): Builder[F] =
      copy(customizer = this.customizer.andThen(customizer))

    def addTextMapPropagators(propagators: JTextMapPropagator*): Builder[F] =
      copy(textMapPropagators = textMapPropagators ++ propagators)

    def withTextMapPropagators(propagators: Iterable[JTextMapPropagator]): Builder[F] =
      copy(textMapPropagators = propagators.toVector)

    def build: Resource[F, TracesTestkit[F]] = {
      Resource.eval(Async[F].delay(InMemorySpanExporter.create())).flatMap { exporter =>
        create[F](exporter, customizer, textMapPropagators)
      }
    }
  }

}
