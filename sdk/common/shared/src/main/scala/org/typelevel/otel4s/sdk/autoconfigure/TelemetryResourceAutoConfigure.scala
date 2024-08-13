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

import cats.effect.Resource
import cats.effect.Sync
import cats.effect.std.Console
import cats.syntax.applicativeError._
import cats.syntax.either._
import cats.syntax.functor._
import cats.syntax.traverse._
import org.typelevel.otel4s.sdk.resource._
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
  * | otel.otel4s.resource.detectors           | OTEL_OTEL4S_RESOURCE_DETECTORS           | Specify resource detectors to use. Defaults to `host,os`.                                                  |
  * }}}
  *
  * @see
  *   [[https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md#opentelemetry-resource]]
  *
  * @param extraDetectors
  *   the extra detectors to use
  */
private final class TelemetryResourceAutoConfigure[F[_]: Sync: Console](
    extraDetectors: Set[TelemetryResourceDetector[F]]
) extends AutoConfigure.WithHint[F, TelemetryResource](
      "TelemetryResource",
      TelemetryResourceAutoConfigure.ConfigKeys.All
    ) {

  import TelemetryResourceAutoConfigure.ConfigKeys
  import TelemetryResourceAutoConfigure.Const
  import TelemetryResourceAutoConfigure.Defaults

  private val detectors: Set[TelemetryResourceDetector[F]] =
    TelemetryResourceDetector.default ++ extraDetectors

  def fromConfig(config: Config): Resource[F, TelemetryResource] =
    for {
      disabledKeys <- Resource.eval(
        Sync[F].fromEither(
          config.getOrElse(ConfigKeys.DisabledKeys, Set.empty[String])
        )
      )

      envResource <- Resource.eval(
        Sync[F].fromEither(fromEnv(config, disabledKeys))
      )

      detectedResource <- fromDetectors(config, disabledKeys)
    } yield detectedResource.fold(envResource)(_.mergeUnsafe(envResource))

  private def fromEnv(
      config: Config,
      disabledKeys: Set[String]
  ): Either[Throwable, TelemetryResource] = {

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

    for {
      entries <- config.getOrElse(
        ConfigKeys.Attributes,
        Map.empty[String, String]
      )

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
  }

  private def fromDetectors(
      config: Config,
      disabledKeys: Set[String]
  ): Resource[F, Option[TelemetryResource]] = {

    def removeDisabledAttributes(resource: TelemetryResource) =
      TelemetryResource(
        resource.attributes.filterNot(a => disabledKeys.contains(a.key.name)),
        resource.schemaUrl
      )

    def detect(name: String): F[Option[TelemetryResource]] =
      detectors.find(_.name == name) match {
        case Some(detector) =>
          detector.detect
            .map(_.map(removeDisabledAttributes))
            .handleErrorWith { e =>
              Console[F]
                .errorln(
                  s"Detector [${detector.name}] failed to detect the resource. The detector is ignored. " +
                    s"${e.getMessage}\n${e.getStackTrace.mkString("\n")}\n"
                )
                .as(None)
            }

        case None =>
          Sync[F].raiseError(
            ConfigurationError.unrecognized(
              ConfigKeys.Detectors.name,
              name,
              detectors.map(_.name) + Const.NoneDetector
            )
          )
      }

    config.getOrElse(ConfigKeys.Detectors, Defaults.Detectors) match {
      case Right(n) if n.contains(Const.NoneDetector) && n.sizeIs > 1 =>
        Resource.raiseError(
          ConfigurationError(
            s"[${ConfigKeys.Detectors}] contains '${Const.NoneDetector}' along with other detectors"
          ): Throwable
        )

      case Right(m) if m.contains(Const.NoneDetector) && m.sizeIs == 1 =>
        Resource.pure(None)

      case Right(names) =>
        Resource.eval(
          names.toList
            .flatTraverse(detector => detect(detector).map(_.toList))
            .map(resources => resources.reduceOption(_ mergeUnsafe _))
        )

      case Left(error) =>
        Resource.raiseError(error: Throwable)
    }
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

    val Detectors: Config.Key[Set[String]] =
      Config.Key("otel.otel4s.resource.detectors")

    val All: Set[Config.Key[_]] = Set(DisabledKeys, Attributes, ServiceName)
  }

  private object Const {
    val NoneDetector = "none"
  }

  private object Defaults {
    val Detectors: Set[String] = Set(
      HostDetector.Const.Name,
      OSDetector.Const.Name
    )
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
    * | otel.otel4s.resource.detectors           | OTEL_OTEL4S_RESOURCE_DETECTORS           | Specify resource detectors to use. Defaults to `host,os`.                                                  |
    * }}}
    *
    * @see
    *   [[https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md#opentelemetry-resource]]
    *
    * @param extraDetectors
    *   the extra detectors to use
    */
  def apply[F[_]: Sync: Console](
      extraDetectors: Set[TelemetryResourceDetector[F]]
  ): AutoConfigure[F, TelemetryResource] =
    new TelemetryResourceAutoConfigure[F](extraDetectors)

}
