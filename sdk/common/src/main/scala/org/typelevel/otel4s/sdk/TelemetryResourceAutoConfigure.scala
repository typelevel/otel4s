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

package org.typelevel.otel4s
package sdk

import cats.ApplicativeThrow
import cats.effect.Resource
import cats.syntax.all._
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.ConfigurationError
import org.typelevel.otel4s.semconv.resource.attributes.ResourceAttributes

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

private final class TelemetryResourceAutoConfigure[F[_]: ApplicativeThrow]
    extends AutoConfigure.WithHint[F, TelemetryResource]("TelemetryResource") {

  import TelemetryResourceAutoConfigure.ConfigKeys

  def configure(config: Config): Resource[F, TelemetryResource] = {
    def parse(entries: List[(String, String)], disabledKeys: Set[String]) =
      entries
        .filter { case (key, _) => !disabledKeys.contains(key) }
        .traverse { case (key, value) =>
          Either
            .catchNonFatal {
              val decoded =
                URLDecoder.decode(value, StandardCharsets.UTF_8.name)
              Attribute(key, decoded)
            }
            .leftMap { e =>
              ConfigurationError("Unable to decode resource attributes", e)
            }
        }

    val attempt = for {
      disabledKeys <-
        config.getOrElse(ConfigKeys.DisabledKeys, Set.empty[String])

      entries <-
        config.getOrElse(ConfigKeys.Attributes, Map.empty[String, String])

      attributes <- parse(entries.toList, disabledKeys)
    } yield {
      val serviceName = config
        .get(ConfigKeys.ServiceName)
        .toOption
        .flatten
        .map(value => ResourceAttributes.ServiceName(value))

      TelemetryResource(
        Attributes.fromSpecific(attributes ++ serviceName.toSeq)
      )
    }

    Resource.eval(ApplicativeThrow[F].fromEither(attempt))
  }

}

private[sdk] object TelemetryResourceAutoConfigure {

  private object ConfigKeys {
    val DisabledKeys: Config.Key[Set[String]] =
      Config.Key("otel.experimental.resource.disabled.keys")

    val Attributes: Config.Key[Map[String, String]] =
      Config.Key("otel.resource.attributes")

    val ServiceName: Config.Key[String] =
      Config.Key("otel.service.name")
  }

  def apply[F[_]: ApplicativeThrow]: AutoConfigure[F, TelemetryResource] =
    new TelemetryResourceAutoConfigure[F]

}
