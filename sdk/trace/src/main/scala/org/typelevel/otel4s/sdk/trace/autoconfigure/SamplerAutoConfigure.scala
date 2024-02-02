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
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.ConfigurationError
import org.typelevel.otel4s.sdk.trace.samplers.Sampler

private final class SamplerAutoConfigure[F[_]: MonadThrow]
    extends AutoConfigure.WithHint[F, Sampler](
      "Sampler",
      SamplerAutoConfigure.ConfigKeys.All
    ) {

  import SamplerAutoConfigure.ConfigKeys

  def fromConfig(config: Config): Resource[F, Sampler] = {
    val sampler = config.getOrElse(ConfigKeys.Sampler, "parentbased_always_on")
    def ratio = config.getOrElse(ConfigKeys.SamplerArg, 1.0)

    val attempt = sampler.flatMap {
      case "always_on" =>
        Right(Sampler.AlwaysOn)

      case "always_off" =>
        Right(Sampler.AlwaysOff)

      case "traceidratio" =>
        ratio.map(r => Sampler.traceIdRatioBased(r))

      case "parentbased_always_on" =>
        Right(Sampler.parentBased(Sampler.AlwaysOn))

      case "parentbased_always_off" =>
        Right(Sampler.parentBased(Sampler.AlwaysOff))

      case "parentbased_traceidratio" =>
        ratio.map(r => Sampler.parentBased(Sampler.traceIdRatioBased(r)))

      case other =>
        Left(ConfigurationError.unrecognized(ConfigKeys.Sampler.name, other))
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

  def apply[F[_]: MonadThrow]: AutoConfigure[F, Sampler] =
    new SamplerAutoConfigure[F]

}
