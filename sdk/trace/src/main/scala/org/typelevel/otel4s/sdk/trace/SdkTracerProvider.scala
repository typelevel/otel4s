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
import org.typelevel.otel4s.sdk.context.LocalContext
import org.typelevel.otel4s.sdk.context.propagation.ContextPropagators
import org.typelevel.otel4s.sdk.internal.ComponentRegistry
import org.typelevel.otel4s.sdk.trace.samplers.Sampler
import org.typelevel.otel4s.trace.TracerBuilder
import org.typelevel.otel4s.trace.TracerProvider

class SdkTracerProvider[F[_]: Temporal](
    idGenerator: IdGenerator[F],
    resource: Resource,
    spanLimits: SpanLimits,
    sampler: Sampler,
    propagators: ContextPropagators,
    spanProcessors: List[SpanProcessor[F]],
    scope: SdkTraceScope[F]
) extends TracerProvider[F] {

  private val sharedState: TracerSharedState[F] =
    TracerSharedState(
      idGenerator,
      resource,
      spanLimits,
      sampler,
      SpanProcessor.composite(spanProcessors)
    )

  private val registry: ComponentRegistry[F, SdkTracer[F]] =
    new ComponentRegistry[F, SdkTracer[F]](scopeInfo =>
      Temporal[F].pure(
        new SdkTracer[F](sharedState, scopeInfo, propagators, scope)
      )
    )

  def tracer(name: String): TracerBuilder[F] =
    new SdkTracerBuilder[F](registry, name)
}

object SdkTracerProvider {

  /** Creates a new [[SdkTracerProviderBuilder]] with default configuration.
    */
  def builder[
      F[_]: Temporal: Random: LocalContext
  ]: SdkTracerProviderBuilder[F] =
    SdkTracerProviderBuilder.default

}
