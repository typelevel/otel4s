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

package org.typelevel.otel4s.java.trace

import cats.effect.Sync
import cats.mtl.Local
import io.opentelemetry.api.trace.{TracerProvider => JTracerProvider}
import org.typelevel.otel4s.ContextPropagators
import org.typelevel.otel4s.context.LocalVault
import org.typelevel.otel4s.trace.TracerBuilder
import org.typelevel.otel4s.trace.TracerProvider
import org.typelevel.vault.Vault

private[java] class TracerProviderImpl[F[_]: Sync: LocalVault](
    jTracerProvider: JTracerProvider,
    propagators: ContextPropagators[F],
    scope: TraceScope[F]
) extends TracerProvider[F] {
  def tracer(name: String): TracerBuilder[F] =
    TracerBuilderImpl(jTracerProvider, propagators, scope, name)
}

private[java] object TracerProviderImpl {

  def local[F[_]](
      jTracerProvider: JTracerProvider,
      propagators: ContextPropagators[F]
  )(implicit F: Sync[F], L: Local[F, Vault]): TracerProvider[F] = {
    val traceScope = TraceScope.fromLocal[F]
    new TracerProviderImpl(jTracerProvider, propagators, traceScope)
  }

}
