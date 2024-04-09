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
package autoconfigure

import cats.MonadThrow
import cats.effect.Resource
import cats.syntax.either._
import cats.syntax.traverse._
import org.typelevel.otel4s.semconv.attributes.ServiceAttributes

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/** Autoconfigures [[TelemetryResource]].
  *
  * The configuration options:
  * {{{
  * | System property                          | Environment variable                     | Description                                                                                                |
  * |------------------------------------------|------------------------------------------|------------------------------------------------------------------------------------------------------------|
  * | otel.resource.attributes                 | OTEL_RESOURCE_ATTRIBUTES                 | Specify resource attributes in the following format: key1=val1,key2=val2,key3=val3                         |
  * | otel.service.name                        | OTEL_SERVICE_NAME                        | Specify logical service name. Takes precedence over `service.name` defined with `otel.resource.attributes` |
  * | otel.experimental.resource.disabled-keys | OTEL_EXPERIMENTAL_RESOURCE_DISABLED_KEYS | Specify resource attribute keys that are filtered.                                                         |
  * }}}
  *
  * @see
  *   [[https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md#opentelemetry-resource]]
  */
private final class TelemetryResourceAutoConfigure[F[_]: MonadThrow]
    extends AutoConfigure.WithHint[F, TelemetryResource](
      "TelemetryResource",
      TelemetryResourceAutoConfigure.ConfigKeys.All
    ) {

  import TelemetryResourceAutoConfigure.ConfigKeys

  def fromConfig(config: Config): Resource[F, TelemetryResource] = {
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
        .map(_.to(Attributes))

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
        .map(value => ServiceAttributes.ServiceName(value))

      val default = TelemetryResource.default
      val fromEnv = TelemetryResource(attributes ++ serviceName)

      default.mergeUnsafe(fromEnv)
    }

    Resource.eval(MonadThrow[F].fromEither(attempt))
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

    val All: Set[Config.Key[_]] = Set(DisabledKeys, Attributes, ServiceName)
  }

  /** Returns [[AutoConfigure]] that configures the [[TelemetryResource]].
    *
    * The configuration options:
    * {{{
    * | System property                          | Environment variable                     | Description                                                                                                |
    * |------------------------------------------|------------------------------------------|------------------------------------------------------------------------------------------------------------|
    * | otel.resource.attributes                 | OTEL_RESOURCE_ATTRIBUTES                 | Specify resource attributes in the following format: key1=val1,key2=val2,key3=val3                         |
    * | otel.service.name                        | OTEL_SERVICE_NAME                        | Specify logical service name. Takes precedence over `service.name` defined with `otel.resource.attributes` |
    * | otel.experimental.resource.disabled-keys | OTEL_EXPERIMENTAL_RESOURCE_DISABLED_KEYS | Specify resource attribute keys that are filtered.                                                         |
    * }}}
    *
    * @see
    *   [[https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md#opentelemetry-resource]]
    */
  def apply[F[_]: MonadThrow]: AutoConfigure[F, TelemetryResource] =
    new TelemetryResourceAutoConfigure[F]

}
