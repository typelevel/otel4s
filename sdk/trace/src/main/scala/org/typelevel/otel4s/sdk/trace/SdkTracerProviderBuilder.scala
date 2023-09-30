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

import cats.effect.Clock
import cats.effect.Concurrent
import org.typelevel.otel4s.sdk.context.propagation.ContextPropagators
import org.typelevel.otel4s.sdk.context.propagation.TextMapPropagator
import org.typelevel.otel4s.sdk.trace.samplers.Sampler

final case class SdkTracerProviderBuilder[F[_]: Concurrent: Clock](
    idGenerator: IdGenerator[F],
    resource: Resource,
    spanLimits: SpanLimits,
    sampler: Sampler,
    propagators: List[TextMapPropagator],
    spanProcessors: List[SpanProcessor[F]],
    scope: SdkTraceScope[F],
) {

  // def setClock(): SdkTracerProviderBuilder[F] = copy()

  def setIdGenerator(idGenerator: IdGenerator[F]): SdkTracerProviderBuilder[F] =
    copy(idGenerator = idGenerator)

  def setResource(resource: Resource): SdkTracerProviderBuilder[F] =
    copy(resource = resource)

  def addResource(resource: Resource): SdkTracerProviderBuilder[F] =
    copy(resource = this.resource.mergeInto(resource).fold(throw _, identity))

  def setSpanLimits(limits: SpanLimits): SdkTracerProviderBuilder[F] =
    copy(spanLimits = limits)

  def setSampler(sampler: Sampler): SdkTracerProviderBuilder[F] =
    copy(sampler = sampler)

  def addTextMapPropagator(
      propagator: TextMapPropagator
  ): SdkTracerProviderBuilder[F] =
    copy(propagators = this.propagators :+ propagator)

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
      ContextPropagators.create(TextMapPropagator.composite(propagators)),
      spanProcessors,
      scope
    )

}
/*

object SdkTracerProviderBuilder {

  def default[F[_]: Async: Clock]: F[SdkTracerProviderBuilder[F]] =
    for {
      random      <- Random.scalaUtilRandom[F]
      idGenerator <- IdGenerator.random[F](implicitly, random)
    } yield SdkTracerProviderBuilder(idGenerator, resource, , Sampler.recordOnly, )

}*/
