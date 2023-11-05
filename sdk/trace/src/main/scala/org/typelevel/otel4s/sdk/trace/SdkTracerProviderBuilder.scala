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

import cats.effect.Temporal
import cats.effect.std.Random
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.sdk.{Resource => InstrumentResource}
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.LocalContext
import org.typelevel.otel4s.sdk.trace.samplers.Sampler

/** Builder for [[SdkTracerProvider]].
  */
sealed trait SdkTracerProviderBuilder[F[_]] {

  /** Sets an [[IdGenerator]].
    *
    * [[IdGenerator]] will be used each time a
    * [[org.typelevel.otel4s.trace.Span Span]] is started.
    *
    * '''Note''': the id generator must be thread-safe and return immediately
    * (no remote calls, as contention free as possible).
    */
  def setIdGenerator(idGenerator: IdGenerator[F]): SdkTracerProviderBuilder[F]

  /** Sets a [[org.typelevel.otel4s.sdk.Resource]] to be attached to all spans
    * created by [[org.typelevel.otel4s.trace.Tracer Tracer]].
    */
  def setResource(resource: InstrumentResource): SdkTracerProviderBuilder[F]

  /** Merges a [[org.typelevel.otel4s.sdk.Resource]] with the current one.
    */
  def addResource(resource: InstrumentResource): SdkTracerProviderBuilder[F]

  /** Sets an initial [[SpanLimits]] that should be used with this SDK.
    *
    * The limits will be used for every
    * [[org.typelevel.otel4s.trace.Span Span]].
    */
  def setSpanLimits(limits: SpanLimits): SdkTracerProviderBuilder[F]

  /** Sets a [[org.typelevel.otel4s.sdk.trace.samplers.Sampler Sampler]] to use
    * for sampling traces.
    *
    * Sampler will be called each time a
    * [[org.typelevel.otel4s.trace.Span Span]] is started.
    *
    * '''Note''': the sampler must be thread-safe and return immediately (no
    * remote calls, as contention free as possible).
    */
  def setSampler(sampler: Sampler): SdkTracerProviderBuilder[F]

  /** Adds a [[TextMapPropagator]]s to use for the context propagation.
    */
  def addTextMapPropagators(
      propagators: TextMapPropagator[Context]*
  ): SdkTracerProviderBuilder[F]

  /** Adds a [[SpanProcessor]] to the span processing pipeline that will be
    * built.
    *
    * [[SpanProcessor]] will be called each time a
    * [[org.typelevel.otel4s.trace.Span Span]] is started or ended.
    *
    * '''Note''': the span processor must be thread-safe and return immediately
    * (no remote calls, as contention free as possible).
    */
  def addSpanProcessor(
      processor: SpanProcessor[F]
  ): SdkTracerProviderBuilder[F]

  /** Creates a new [[SdkTracerProvider]] with configuration of this builder.
    */
  def build: SdkTracerProvider[F]
}

object SdkTracerProviderBuilder {

  /** Creates a new [[SdkTracerProviderBuilder]] with default configuration.
    */
  def default[
      F[_]: Temporal: Random: LocalContext
  ]: SdkTracerProviderBuilder[F] =
    new Builder[F](
      idGenerator = IdGenerator.random,
      resource = InstrumentResource.Default,
      spanLimits = SpanLimits.Default,
      sampler = Sampler.parentBased(Sampler.AlwaysOn),
      propagators = Nil,
      spanProcessors = Nil
    )

  private final case class Builder[F[_]: Temporal: LocalContext](
      idGenerator: IdGenerator[F],
      resource: InstrumentResource,
      spanLimits: SpanLimits,
      sampler: Sampler,
      propagators: List[TextMapPropagator[Context]],
      spanProcessors: List[SpanProcessor[F]]
  ) extends SdkTracerProviderBuilder[F] {

    def setIdGenerator(generator: IdGenerator[F]): SdkTracerProviderBuilder[F] =
      copy(idGenerator = generator)

    def setResource(resource: InstrumentResource): SdkTracerProviderBuilder[F] =
      copy(resource = resource)

    def addResource(resource: InstrumentResource): SdkTracerProviderBuilder[F] =
      copy(resource = this.resource.mergeInto(resource).fold(throw _, identity))

    def setSpanLimits(limits: SpanLimits): SdkTracerProviderBuilder[F] =
      copy(spanLimits = limits)

    def setSampler(sampler: Sampler): SdkTracerProviderBuilder[F] =
      copy(sampler = sampler)

    def addTextMapPropagators(
        propagator: TextMapPropagator[Context]*
    ): SdkTracerProviderBuilder[F] =
      copy(propagators = this.propagators ++ propagator)

    def addSpanProcessor(
        processor: SpanProcessor[F]
    ): SdkTracerProviderBuilder[F] =
      copy(spanProcessors = this.spanProcessors :+ processor)

    def build: SdkTracerProvider[F] =
      new SdkTracerProvider[F](
        idGenerator,
        resource,
        spanLimits,
        sampler,
        ContextPropagators.of(propagators: _*),
        spanProcessors,
        SdkTraceScope.fromLocal[F]
      )
  }

}
