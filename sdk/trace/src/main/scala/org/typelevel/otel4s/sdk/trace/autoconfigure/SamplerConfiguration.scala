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

import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.ConfigurationError
import org.typelevel.otel4s.sdk.trace.samplers.Sampler

private[sdk] object SamplerConfiguration {

  def configure(config: Config): Either[ConfigurationError, Sampler] = {
    val sampler =
      config.getString("otel.traces.sampler").getOrElse("parentbased_always_on")

    def ratio =
      config.getDouble("otel.traces.sampler.arg").map(_.getOrElse(1.0))

    sampler match {
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
        Left(ConfigurationError.unrecognized("otel.traces.sampler", other))
    }
  }

}
