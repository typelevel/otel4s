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

import cats.Monad
import cats.effect.std.SystemProperties
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.semconv.SchemaUrls

import java.util.Locale

private[resource] trait OSDetectorPlatform { self: OSDetector.type =>

  def apply[F[_]: Monad: SystemProperties]: TelemetryResourceDetector[F] =
    new Detector[F]

  private class Detector[F[_]: Monad: SystemProperties] extends TelemetryResourceDetector.Unsealed[F] {
    def name: String = Const.Name

    def detect: F[Option[TelemetryResource]] =
      for {
        nameOpt <- SystemProperties[F].get("os.name")
        versionOpt <- SystemProperties[F].get("os.version")
      } yield {
        val builder = Attributes.newBuilder

        val tpe = Keys.Type.maybe(nameOpt.flatMap(nameToType))

        val description = {
          val value = versionOpt
            .zip(nameOpt)
            .map { case (version, name) => name + " " + version }
            .orElse(nameOpt)

          Keys.Description.maybe(value)
        }

        builder.addAll(tpe)
        builder.addAll(description)

        val attributes = builder.result()

        Option.when(attributes.nonEmpty)(
          TelemetryResource(attributes, Some(SchemaUrls.Current))
        )
      }
  }

  // transforms OS names to match the spec:
  // https://opentelemetry.io/docs/specs/semconv/resource/os/
  private def nameToType(name: String): Option[String] =
    name.toLowerCase(Locale.ROOT) match {
      case os if os.startsWith("windows")      => Some("windows")
      case os if os.startsWith("linux")        => Some("linux")
      case os if os.startsWith("mac")          => Some("darwin")
      case os if os.startsWith("freebsd")      => Some("freebsd")
      case os if os.startsWith("netbsd")       => Some("netbsd")
      case os if os.startsWith("openbsd")      => Some("openbsd")
      case os if os.startsWith("dragonflybsd") => Some("dragonflybsd")
      case os if os.startsWith("hp-ux")        => Some("hpux")
      case os if os.startsWith("aix")          => Some("aix")
      case os if os.startsWith("solaris")      => Some("solaris")
      case os if os.startsWith("z/os")         => Some("z_os")
      case _                                   => None
    }

}
