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

package org.typelevel.otel4s.sdk.trace.autoconfigure

import cats.MonadThrow
import cats.data.NonEmptyList
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.ConfigurationError
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.propagation.W3CTraceContextPropagator

private[sdk] object PropagatorsConfiguration {

  private val Default = NonEmptyList.one("tracecontext" /*, "baggage"*/ )

  def configure[F[_]: MonadThrow](
      config: Config
  ): F[ContextPropagators[Context]] = {
    MonadThrow[F].fromEither(config.getStringSet("otel.propagators")).flatMap {
      case names if names.contains("none") && names.sizeIs > 1 =>
        MonadThrow[F].raiseError(
          new ConfigurationError(
            "[otel.propagators] contains 'none' along with other propagators",
            None
          )
        )

      case names if names.contains("none") =>
        MonadThrow[F].pure(ContextPropagators.noop)

      case names =>
        val requested = NonEmptyList.fromList(names.toList).getOrElse(Default)

        for {
          propagators <- requested.traverse(name => create(name))
        } yield ContextPropagators.of(propagators.toList: _*)
    }
  }

  private def create[F[_]: MonadThrow](
      name: String
  ): F[TextMapPropagator[Context]] =
    name match {
      case "tracecontext" =>
        MonadThrow[F].pure(W3CTraceContextPropagator)

      /*case "baggage" =>
        MonadThrow[F].pure(W3CBaggagePropagator)*/

      case other =>
        MonadThrow[F].raiseError(
          ConfigurationError.unrecognized("otel.propagators", other)
        )
    }
}
