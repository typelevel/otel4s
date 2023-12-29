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

package org.typelevel.otel4s.sdk.autoconfigure

import cats.syntax.either._
import cats.syntax.traverse._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.Resource
import org.typelevel.otel4s.semconv.resource.attributes.ResourceAttributes

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

private[sdk] object ResourceConfiguration {

  // Attributes specified via otel.resource.attributes follow the W3C Baggage spec and
  // characters outside the baggage-octet range are percent encoded
  // https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/sdk.md#specifying-resource-information-via-an-environment-variable
  def configure(config: Config): Either[ConfigurationError, Resource] = {
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
              new ConfigurationError(
                "Unable to decode resource attributes",
                Some(e)
              )
            }
        }

    for {
      disabledKeys <-
        config.getStringSet("otel.experimental.resource.disabled.keys")

      entries <- config.getStringMap("otel.resource.attributes")
      attributes <- parse(entries.toList, disabledKeys)
    } yield {
      val serviceName = config
        .getString("otel.service.name")
        .map(value => ResourceAttributes.ServiceName(value))

      Resource(Attributes.fromSpecific(attributes ++ serviceName.toSeq))
    }
  }

}
