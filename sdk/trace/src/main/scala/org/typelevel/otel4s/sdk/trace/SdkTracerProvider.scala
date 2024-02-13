/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk
package trace

import cats.Parallel
import cats.effect.Temporal
import cats.effect.std.Console
import cats.effect.std.Random
import cats.syntax.functor._
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.LocalContext
import org.typelevel.otel4s.sdk.trace.processor.SpanProcessor
import org.typelevel.otel4s.sdk.trace.processor.SpanStorage
import org.typelevel.otel4s.sdk.trace.samplers.Sampler
import org.typelevel.otel4s.trace.TracerBuilder
import org.typelevel.otel4s.trace.TracerProvider

private class SdkTracerProvider[F[_]: Temporal: Parallel: Console](
    idGenerator: IdGenerator[F],
    resource: TelemetryResource,
    sampler: Sampler,
    propagators: ContextPropagators[Context],
    spanProcessors: List[SpanProcessor[F]],
    traceScope: SdkTraceScope[F],
    storage: SpanStorage[F]
) extends TracerProvider[F] {

  private val sharedState: TracerSharedState[F] =
    TracerSharedState(
      idGenerator,
      resource,
      sampler,
      SpanProcessor.of(spanProcessors: _*)
    )

  def tracer(name: String): TracerBuilder[F] =
    new SdkTracerBuilder[F](propagators, traceScope, sharedState, storage, name)
}

object SdkTracerProvider {

  /** Builder for [[org.typelevel.otel4s.trace.TracerProvider TracerProvider]].
    */
  sealed trait Builder[F[_]] {

    /** Sets an [[IdGenerator]].
      *
      * [[IdGenerator]] will be used each time a
      * [[org.typelevel.otel4s.trace.Span Span]] is started.
      *
      * @note
      *   the id generator must be thread-safe and return immediately (no remote
      *   calls, as contention free as possible).
      *
      * @param idGenerator
      *   the [[IdGenerator]] to use
      */
    def withIdGenerator(idGenerator: IdGenerator[F]): Builder[F]

    /** Sets a [[TelemetryResource]] to be attached to all spans created by
      * [[org.typelevel.otel4s.trace.Tracer Tracer]].
      *
      * @note
      *   on multiple subsequent calls, the resource from the last call will be
      *   retained.
      *
      * @param resource
      *   the [[TelemetryResource]] to use
      */
    def withResource(resource: TelemetryResource): Builder[F]

    /** Merges the given [[TelemetryResource]] with the current one.
      *
      * @note
      *   if both resources have different non-empty `schemaUrl`, the merge will
      *   fail.
      *
      * @see
      *   [[TelemetryResource.mergeUnsafe]]
      *
      * @param resource
      *   the [[TelemetryResource]] to merge the current one with
      */
    def addResource(resource: TelemetryResource): Builder[F]

    /** Sets a [[org.typelevel.otel4s.sdk.trace.samplers.Sampler Sampler]].
      *
      * The sampler will be called each time a
      * [[org.typelevel.otel4s.trace.Span Span]] is started.
      *
      * @note
      *   the sampler must be thread-safe and return immediately (no remote
      *   calls, as contention free as possible).
      *
      * @param sampler
      *   the [[org.typelevel.otel4s.sdk.trace.samplers.Sampler Sampler]] to use
      */
    def withSampler(sampler: Sampler): Builder[F]

    /** Adds a
      * [[org.typelevel.otel4s.context.propagation.TextMapPropagator TextMapPropagator]]s
      * to use for the context propagation.
      *
      * @param propagators
      *   the propagators to add
      */
    def addTextMapPropagators(
        propagators: TextMapPropagator[Context]*
    ): Builder[F]

    /** Adds a
      * [[org.typelevel.otel4s.sdk.trace.processor.SpanProcessor SpanProcessor]]
      * to the span processing pipeline that will be built.
      *
      * The span processor will be called each time a
      * [[org.typelevel.otel4s.trace.Span Span]] is started or ended.
      *
      * @note
      *   the span processor must be thread-safe and return immediately (no
      *   remote calls, as contention free as possible).
      *
      * @param processor
      *   the span processor to add
      */
    def addSpanProcessor(processor: SpanProcessor[F]): Builder[F]

    /** Creates a new
      * [[org.typelevel.otel4s.trace.TracerProvider TracerProvider]] with the
      * configuration of this builder.
      */
    def build: F[TracerProvider[F]]
  }

  /** Creates a new [[Builder]] with default configuration.
    */
  def builder[
      F[_]: Temporal: Parallel: Random: LocalContext: Console
  ]: Builder[F] =
    BuilderImpl[F](
      idGenerator = IdGenerator.random,
      resource = TelemetryResource.default,
      sampler = Sampler.parentBased(Sampler.AlwaysOn),
      propagators = Nil,
      spanProcessors = Nil
    )

  private final case class BuilderImpl[
      F[_]: Temporal: Parallel: LocalContext: Console
  ](
      idGenerator: IdGenerator[F],
      resource: TelemetryResource,
      sampler: Sampler,
      propagators: List[TextMapPropagator[Context]],
      spanProcessors: List[SpanProcessor[F]]
  ) extends Builder[F] {

    def withIdGenerator(generator: IdGenerator[F]): Builder[F] =
      copy(idGenerator = generator)

    def withResource(resource: TelemetryResource): Builder[F] =
      copy(resource = resource)

    def addResource(resource: TelemetryResource): Builder[F] =
      copy(resource = this.resource.mergeUnsafe(resource))

    def withSampler(sampler: Sampler): Builder[F] =
      copy(sampler = sampler)

    def addTextMapPropagators(
        propagators: TextMapPropagator[Context]*
    ): Builder[F] =
      copy(propagators = this.propagators ++ propagators)

    def addSpanProcessor(processor: SpanProcessor[F]): Builder[F] =
      copy(spanProcessors = this.spanProcessors :+ processor)

    def build: F[TracerProvider[F]] =
      SpanStorage.create[F].map { storage =>
        new SdkTracerProvider[F](
          idGenerator,
          resource,
          sampler,
          ContextPropagators.of(propagators: _*),
          spanProcessors :+ storage,
          SdkTraceScope.fromLocal[F],
          storage
        )
      }
  }
}
