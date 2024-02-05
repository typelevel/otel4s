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
import cats.effect.Resource
import cats.syntax.either._
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.ConfigurationError
import org.typelevel.otel4s.sdk.trace.samplers.Sampler

/** Autoconfigures a [[Sampler]].
  *
  * The configuration options:
  * {{{
  * | System property         | Environment variable    | Description                                                             |
  * |-------------------------|-------------------------|-------------------------------------------------------------------------|
  * | otel.traces.sampler     | OTEL_TRACES_SAMPLER     | The sampler to use for tracing. Defaults to `parentbased_always_on`     |
  * | otel.traces.sampler.arg | OTEL_TRACES_SAMPLER_ARG | An argument to the configured tracer if supported, for example a ratio. |
  * }}}
  *
  * @see
  *   [[https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md#sampler]]
  */
private final class SamplerAutoConfigure[F[_]: MonadThrow]
    extends AutoConfigure.WithHint[F, Sampler](
      "Sampler",
      SamplerAutoConfigure.ConfigKeys.All
    ) {

  import SamplerAutoConfigure.ConfigKeys
  import SamplerAutoConfigure.Defaults

  private val options = Set(
    "always_on",
    "always_off",
    "traceidratio",
    "parentbased_always_on",
    "parentbased_always_off",
    "parentbased_traceidratio"
  )

  def fromConfig(config: Config): Resource[F, Sampler] = {
    val sampler = config.getOrElse(ConfigKeys.Sampler, Defaults.Sampler)
    def traceIdRatioSampler =
      config
        .getOrElse(ConfigKeys.SamplerArg, Defaults.Ratio)
        .flatMap { ratio =>
          Either
            .catchNonFatal(Sampler.traceIdRatioBased(ratio))
            .leftMap { cause =>
              ConfigurationError(
                s"[${ConfigKeys.SamplerArg.name}] has invalid ratio [$ratio] - ${cause.getMessage}",
                cause
              )
            }
        }

    def attempt = sampler.flatMap {
      case "always_on" =>
        Right(Sampler.AlwaysOn)

      case "always_off" =>
        Right(Sampler.AlwaysOff)

      case "traceidratio" =>
        traceIdRatioSampler

      case "parentbased_always_on" =>
        Right(Sampler.parentBased(Sampler.AlwaysOn))

      case "parentbased_always_off" =>
        Right(Sampler.parentBased(Sampler.AlwaysOff))

      case "parentbased_traceidratio" =>
        traceIdRatioSampler.map(s => Sampler.parentBased(s))

      case other =>
        Left(
          ConfigurationError.unrecognized(
            ConfigKeys.Sampler.name,
            other,
            options
          )
        )
    }

    Resource.eval(MonadThrow[F].fromEither(attempt))
  }

}

private[sdk] object SamplerAutoConfigure {

  private object ConfigKeys {
    val Sampler: Config.Key[String] = Config.Key("otel.traces.sampler")
    val SamplerArg: Config.Key[Double] = Config.Key("otel.traces.sampler.arg")

    val All: Set[Config.Key[_]] = Set(Sampler, SamplerArg)
  }

  private object Defaults {
    val Sampler = "parentbased_always_on"
    val Ratio = 1.0
  }

  /** Autoconfigures a [[Sampler]].
    *
    * The configuration options:
    * {{{
    * | System property         | Environment variable    | Description                                                             |
    * |-------------------------|-------------------------|-------------------------------------------------------------------------|
    * | otel.traces.sampler     | OTEL_TRACES_SAMPLER     | The sampler to use for tracing. Defaults to `parentbased_always_on`     |
    * | otel.traces.sampler.arg | OTEL_TRACES_SAMPLER_ARG | An argument to the configured tracer if supported, for example a ratio. |
    * }}}
    *
    * Supported options for `otel.traces.sampler` are:
    *   - `always_on` - [[Sampler.AlwaysOn]]
    *
    *   - `always_off` - [[Sampler.AlwaysOff]]
    *
    *   - `traceidratio` - [[Sampler.traceIdRatioBased]], where
    *     `otel.traces.sampler.arg` sets the ratio
    *
    *   - `parentbased_always_on` - [[Sampler.parentBased]] with
    *     [[Sampler.AlwaysOn]]
    *
    *   - `parentbased_always_off` - [[Sampler.parentBased]] with
    *     [[Sampler.AlwaysOff]]
    *
    *   - `parentbased_traceidratio`- [[Sampler.parentBased]] with
    *     [[Sampler.traceIdRatioBased]], where `otel.traces.sampler.arg` sets
    *     the ratio
    *
    * @see
    *   [[https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md#sampler]]
    */
  def apply[F[_]: MonadThrow]: AutoConfigure[F, Sampler] =
    new SamplerAutoConfigure[F]

}
