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

import cats.data.NonEmptyList
import cats.effect.MonadCancelThrow
import cats.effect.Resource
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.ConfigurationError
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.context.propagation.B3Propagator
import org.typelevel.otel4s.sdk.trace.context.propagation.JaegerPropagator
import org.typelevel.otel4s.sdk.trace.context.propagation.OtTracePropagator
import org.typelevel.otel4s.sdk.trace.context.propagation.W3CBaggagePropagator
import org.typelevel.otel4s.sdk.trace.context.propagation.W3CTraceContextPropagator

/** Autoconfigures [[ContextPropagators]].
  *
  * The configuration options:
  * {{{
  * | System property  | Environment variable | Description                                                                                                           |
  * |------------------|----------------------|-----------------------------------------------------------------------------------------------------------------------|
  * | otel.propagators | OTEL_PROPAGATORS     | The propagators to use. Use a comma-separated list for multiple propagators. Default is `tracecontext,baggage` (W3C). |
  * }}}
  *
  * @see
  *   [[https://opentelemetry.io/docs/languages/java/configuration/#propagators]]
  */
private final class ContextPropagatorsAutoConfigure[F[_]: MonadCancelThrow](
    extra: Set[AutoConfigure.Named[F, TextMapPropagator[Context]]]
) extends AutoConfigure.WithHint[F, ContextPropagators[Context]](
      "ContextPropagators",
      ContextPropagatorsAutoConfigure.ConfigKeys.All
    ) {

  import ContextPropagatorsAutoConfigure.ConfigKeys
  import ContextPropagatorsAutoConfigure.Const
  import ContextPropagatorsAutoConfigure.Default

  private val configurers = {
    val default: Set[AutoConfigure.Named[F, TextMapPropagator[Context]]] = Set(
      AutoConfigure.Named.const("none", TextMapPropagator.noop),
      AutoConfigure.Named.const(
        "tracecontext",
        W3CTraceContextPropagator.default
      ),
      AutoConfigure.Named.const("baggage", W3CBaggagePropagator.default),
      AutoConfigure.Named.const("b3", B3Propagator.singleHeader),
      AutoConfigure.Named.const("b3multi", B3Propagator.multipleHeaders),
      AutoConfigure.Named.const("jaeger", JaegerPropagator.default),
      AutoConfigure.Named.const("ottrace", OtTracePropagator.default)
    )

    default ++ extra
  }

  def fromConfig(config: Config): Resource[F, ContextPropagators[Context]] = {
    val values = config.getOrElse(ConfigKeys.Propagators, Set.empty[String])
    Resource.eval(MonadCancelThrow[F].fromEither(values)).flatMap {
      case names if names.contains(Const.NonePropagator) && names.sizeIs > 1 =>
        Resource.raiseError(
          ConfigurationError(
            s"[${ConfigKeys.Propagators}] contains '${Const.NonePropagator}' along with other propagators"
          ): Throwable
        )

      case names if names.contains("none") =>
        Resource.pure(ContextPropagators.noop)

      case names =>
        val requested = NonEmptyList.fromList(names.toList).getOrElse(Default)

        for {
          propagators <- requested.traverse(name => create(name, config))
        } yield ContextPropagators.of(propagators.toList: _*)
    }
  }

  private def create(
      name: String,
      config: Config
  ): Resource[F, TextMapPropagator[Context]] =
    configurers.find(_.name == name) match {
      case Some(configurer) =>
        configurer.configure(config)

      case None =>
        Resource.raiseError(
          ConfigurationError.unrecognized(
            ConfigKeys.Propagators.name,
            name,
            configurers.map(_.name)
          ): Throwable
        )
    }
}

private[sdk] object ContextPropagatorsAutoConfigure {

  private object ConfigKeys {
    val Propagators: Config.Key[Set[String]] = Config.Key("otel.propagators")

    val All: Set[Config.Key[_]] = Set(Propagators)
  }

  private object Const {
    val NonePropagator = "none"
  }

  private val Default = NonEmptyList.of("tracecontext", "baggage")

  /** Autoconfigures [[ContextPropagators]].
    *
    * The configuration options:
    * {{{
    * | System property  | Environment variable | Description                                                                                                           |
    * |------------------|----------------------|-----------------------------------------------------------------------------------------------------------------------|
    * | otel.propagators | OTEL_PROPAGATORS     | The propagators to use. Use a comma-separated list for multiple propagators. Default is `tracecontext,baggage` (W3C). |
    * }}}
    *
    * Out of the box, the following options are supported:
    *   - [[W3CTraceContextPropagator tracecontext]]
    *   - [[W3CBaggagePropagator baggage]]
    *   - [[B3Propagator.singleHeader b3]]
    *   - [[B3Propagator.multipleHeaders b3multi]]
    *   - [[JaegerPropagator jaeger]]
    *   - [[OtTracePropagator ottrace]]
    *
    * @see
    *   [[https://opentelemetry.io/docs/languages/java/configuration/#propagators]]
    *
    * @param extra
    *   extra configurers to use
    */
  def apply[F[_]: MonadCancelThrow](
      extra: Set[AutoConfigure.Named[F, TextMapPropagator[Context]]]
  ): AutoConfigure[F, ContextPropagators[Context]] =
    new ContextPropagatorsAutoConfigure[F](extra)

}
