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

package org.typelevel.otel4s.sdk.resource

import cats.effect.Sync
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.semconv.SchemaUrls

private[resource] trait OSDetectorPlatform { self: OSDetector.type =>

  def apply[F[_]: Sync]: TelemetryResourceDetector[F] =
    new Detector[F]

  private class Detector[F[_]: Sync] extends TelemetryResourceDetector[F] {
    def name: String = Const.Name

    def detect: F[Option[TelemetryResource]] = Sync[F].delay {
      val tpe = Keys.Type(normalizeType(OS.platform()))
      val release = Keys.Description(OS.release())

      Some(
        TelemetryResource(Attributes(tpe, release), Some(SchemaUrls.Current))
      )
    }
  }

  // transforms https://nodejs.org/api/os.html#osplatform values to match the spec:
  // https://opentelemetry.io/docs/specs/semconv/resource/os/
  private def normalizeType(platform: String): String =
    platform match {
      case "sunos" => "solaris"
      case "win32" => "windows"
      case other   => other
    }

}
