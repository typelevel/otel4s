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

import cats.effect.IOLocal
import cats.effect.LiftIO
import cats.effect.Sync
import io.opentelemetry.api.trace.{TracerProvider => JTracerProvider}
import io.opentelemetry.context.{Context => JContext}
import org.typelevel.otel4s.java.trace.instances._
import org.typelevel.otel4s.trace.TracerBuilder
import org.typelevel.otel4s.trace.TracerProvider

private[java] class TracerProviderImpl[F[_]: Sync](
    jTracerProvider: JTracerProvider,
    scope: TraceScope[F]
) extends TracerProvider[F] {
  def tracer(name: String): TracerBuilder[F] =
    TracerBuilderImpl(jTracerProvider, scope, name)
}

private[java] object TracerProviderImpl {

  def ioLocal[F[_]: LiftIO: Sync](
      jTracerProvider: JTracerProvider,
      default: JContext = JContext.root()
  ): F[TracerProvider[F]] =
    IOLocal[TraceScope.Scope](TraceScope.Scope.Root(default))
      .map { implicit ioLocal: IOLocal[TraceScope.Scope] =>
        val traceScope = TraceScope.fromLocal[F](default)
        new TracerProviderImpl(jTracerProvider, traceScope): TracerProvider[F]
      }
      .to[F]

}
