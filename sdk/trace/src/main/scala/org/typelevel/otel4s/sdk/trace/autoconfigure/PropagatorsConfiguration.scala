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
import cats.effect.Resource
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.ConfigurationError
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.context.propagation.B3Propagator
import org.typelevel.otel4s.sdk.trace.context.propagation.W3CTraceContextPropagator

private final class PropagatorsConfiguration[F[_]: MonadThrow]
    extends AutoConfigure.WithHint[F, ContextPropagators[Context]](
      "ContextPropagators"
    ) {

  import PropagatorsConfiguration.ConfigKeys
  import PropagatorsConfiguration.Default

  def fromConfig(config: Config): Resource[F, ContextPropagators[Context]] = {
    val values = config.getOrElse(ConfigKeys.Propagators, Set.empty[String])
    Resource.eval {
      MonadThrow[F].fromEither(values).flatMap[ContextPropagators[Context]] {
        case names if names.contains("none") && names.sizeIs > 1 =>
          MonadThrow[F].raiseError(
            new ConfigurationError(
              s"[${ConfigKeys.Propagators}] contains 'none' along with other propagators",
              None
            )
          )

        case names if names.contains("none") =>
          MonadThrow[F].pure(ContextPropagators.noop)

        case names =>
          val requested =
            NonEmptyList.fromList(names.toList).getOrElse(Default)

          for {
            propagators <- requested.traverse(name => create(name))
          } yield ContextPropagators.of(propagators.toList: _*)
      }
    }
  }

  private def create(name: String): F[TextMapPropagator[Context]] =
    name match {
      case "tracecontext" =>
        MonadThrow[F].pure(W3CTraceContextPropagator)

      /*case "baggage" =>
        MonadThrow[F].pure(W3CBaggagePropagator)*/

      case "b3" =>
        MonadThrow[F].pure(B3Propagator.singleHeader)

      case "b3multi" =>
        MonadThrow[F].pure(B3Propagator.multipleHeaders)

      case other =>
        MonadThrow[F].raiseError(
          ConfigurationError.unrecognized("otel.propagators", other)
        )
    }
}

private[sdk] object PropagatorsConfiguration {

  private object ConfigKeys {
    val Propagators: Config.Key[Set[String]] = Config.Key("otel.propagators")
  }

  private val Default = NonEmptyList.one("tracecontext" /*, "baggage"*/ )

  def apply[F[_]: MonadThrow]: AutoConfigure[F, ContextPropagators[Context]] =
    new PropagatorsConfiguration[F]

}
