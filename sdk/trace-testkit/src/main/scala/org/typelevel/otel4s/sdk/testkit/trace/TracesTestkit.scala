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

package org.typelevel.otel4s.sdk.testkit.trace

import cats.FlatMap
import cats.Parallel
import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Random
import cats.syntax.flatMap._
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.sdk.common.Diagnostic
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.LocalContext
import org.typelevel.otel4s.sdk.context.LocalContextProvider
import org.typelevel.otel4s.sdk.trace.SdkTracerProvider
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.processor.SimpleSpanProcessor
import org.typelevel.otel4s.sdk.trace.processor.SpanProcessor
import org.typelevel.otel4s.trace.TracerProvider

sealed trait TracesTestkit[F[_]] {

  /** The [[org.typelevel.otel4s.trace.TracerProvider TracerProvider]].
    */
  def tracerProvider: TracerProvider[F]

  /** The list of finished spans.
    *
    * @note
    *   each invocation cleans up the internal buffer.
    */
  def finishedSpans: F[List[SpanData]]
}

object TracesTestkit {
  private[sdk] trait Unsealed[F[_]] extends TracesTestkit[F]

  /** Builder for [[TracesTestkit]]. */
  sealed trait Builder[F[_]] {

    /** Adds the tracer provider builder customizer. Multiple customizers can be added, and they will be applied in the
      * order they were added.
      *
      * @param customizer
      *   the customizer to add
      */
    def addTracerProviderCustomizer(
        customizer: SdkTracerProvider.Builder[F] => SdkTracerProvider.Builder[F]
    ): Builder[F]

    /** Creates [[TracesTestkit]] using the configuration of this builder. */
    def build: Resource[F, TracesTestkit[F]]

  }

  /** Creates a [[Builder]] of [[TracesTestkit]] with the default configuration. */
  def builder[F[_]: Async: Parallel: Diagnostic: LocalContextProvider]: Builder[F] =
    new BuilderImpl[F]()

  /** Creates a [[TracesTestkit]] using [[Builder]]. The instance keeps spans in memory.
    *
    * @param customize
    *   a function for customizing the builder
    */
  def inMemory[F[_]: Async: Parallel: Diagnostic: LocalContextProvider](
      customize: Builder[F] => Builder[F] = identity[Builder[F]](_)
  ): Resource[F, TracesTestkit[F]] =
    customize(builder[F]).build

  private final class Impl[F[_]: FlatMap](
      val tracerProvider: TracerProvider[F],
      processor: SpanProcessor[F],
      exporter: InMemorySpanExporter[F]
  ) extends TracesTestkit[F] {
    def finishedSpans: F[List[SpanData]] =
      processor.forceFlush >> exporter.finishedSpans
  }

  private final case class BuilderImpl[F[_]: Async: Parallel: Diagnostic: LocalContextProvider](
      customizer: SdkTracerProvider.Builder[F] => SdkTracerProvider.Builder[F] = (b: SdkTracerProvider.Builder[F]) => b,
  ) extends Builder[F] {

    def addTracerProviderCustomizer(
        customizer: SdkTracerProvider.Builder[F] => SdkTracerProvider.Builder[F]
    ): Builder[F] =
      copy(customizer = this.customizer.andThen(customizer))

    def build: Resource[F, TracesTestkit[F]] = {
      def createTracerProvider(
          processor: SpanProcessor[F]
      )(implicit local: LocalContext[F]): F[TracerProvider[F]] =
        Random.scalaUtilRandom[F].flatMap { implicit random =>
          val builder = SdkTracerProvider.builder[F].addSpanProcessor(processor)
          customizer(builder).build
        }

      for {
        local <- Resource.eval(LocalProvider[F, Context].local)
        exporter <- Resource.eval(InMemorySpanExporter.create[F](None))
        processor <- Resource.pure(SimpleSpanProcessor(exporter))
        tracerProvider <- Resource.eval(createTracerProvider(processor)(local))
      } yield new Impl(tracerProvider, processor, exporter)
    }
  }

}
