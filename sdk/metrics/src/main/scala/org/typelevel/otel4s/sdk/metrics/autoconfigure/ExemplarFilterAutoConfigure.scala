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

package org.typelevel.otel4s.sdk.metrics.autoconfigure

import cats.MonadThrow
import cats.effect.Resource
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.ConfigurationError
import org.typelevel.otel4s.sdk.metrics.exemplar.ExemplarFilter
import org.typelevel.otel4s.sdk.metrics.exemplar.TraceContextLookup

/** Autoconfigures an [[ExemplarFilter]].
  *
  * The configuration options:
  * {{{
  * | System property              | Environment variable         | Description                                           |
  * |------------------------------|------------------------------|-------------------------------------------------------|
  * | otel.metrics.exemplar.filter | OTEL_METRICS_EXEMPLAR_FILTER | The exemplar filter to use. Default is `trace_based`. |
  * }}}
  *
  * The following options for `otel.metrics.exemplar.filter` are supported:
  *   - `always_on` - [[ExemplarFilter.alwaysOn]]
  *   - `always_off` - [[ExemplarFilter.alwaysOff]]
  *   - `trace_based` - [[ExemplarFilter.traceBased]]
  *
  * @see
  *   [[https://opentelemetry.io/docs/languages/java/configuration/#exemplars]]
  */
private final class ExemplarFilterAutoConfigure[F[_]: MonadThrow](
    lookup: TraceContextLookup
) extends AutoConfigure.WithHint[F, ExemplarFilter](
      "ExemplarFilter",
      ExemplarFilterAutoConfigure.ConfigKeys.All
    ) {

  import ExemplarFilterAutoConfigure.ConfigKeys
  import ExemplarFilterAutoConfigure.Defaults

  private val configurers: Set[AutoConfigure.Named[F, ExemplarFilter]] = Set(
    AutoConfigure.Named.const("always_on", ExemplarFilter.alwaysOn),
    AutoConfigure.Named.const("always_off", ExemplarFilter.alwaysOff),
    AutoConfigure.Named.const("trace_based", ExemplarFilter.traceBased(lookup))
  )

  protected def fromConfig(config: Config): Resource[F, ExemplarFilter] = {
    val exemplarFilterValue = config
      .getOrElse(ConfigKeys.ExemplarFilter, Defaults.ExemplarFilter)
      .map(_.toLowerCase)

    exemplarFilterValue match {
      case Right(name) =>
        configurers.find(_.name == name) match {
          case Some(configure) =>
            configure.configure(config)

          case None =>
            Resource.raiseError(
              ConfigurationError.unrecognized(
                ConfigKeys.ExemplarFilter.name,
                name,
                configurers.map(_.name)
              ): Throwable
            )
        }

      case Left(error) =>
        Resource.raiseError(error: Throwable)
    }
  }

}

private[sdk] object ExemplarFilterAutoConfigure {

  private object ConfigKeys {
    val ExemplarFilter: Config.Key[String] =
      Config.Key("otel.metrics.exemplar.filter")

    val All: Set[Config.Key[_]] = Set(ExemplarFilter)
  }

  private object Defaults {
    val ExemplarFilter: String = "trace_based"
  }

  /** Autoconfigures an [[ExemplarFilter]].
    *
    * The configuration options:
    * {{{
    * | System property              | Environment variable         | Description                                           |
    * |------------------------------|------------------------------|-------------------------------------------------------|
    * | otel.metrics.exemplar.filter | OTEL_METRICS_EXEMPLAR_FILTER | The exemplar filter to use. Default is `trace_based`. |
    * }}}
    *
    * The following options for `otel.metrics.exemplar.filter` are supported:
    *   - `always_on` - [[ExemplarFilter.alwaysOn]]
    *   - `always_off` - [[ExemplarFilter.alwaysOff]]
    *   - `trace_based` - [[ExemplarFilter.traceBased]]
    *
    * @see
    *   [[https://opentelemetry.io/docs/languages/java/configuration/#exemplars]]
    *
    * @param traceContextLookup
    *   used by the exemplar reservoir to extract tracing information from the context
    */
  def apply[F[_]: MonadThrow](
      traceContextLookup: TraceContextLookup
  ): AutoConfigure[F, ExemplarFilter] =
    new ExemplarFilterAutoConfigure[F](traceContextLookup)

}
