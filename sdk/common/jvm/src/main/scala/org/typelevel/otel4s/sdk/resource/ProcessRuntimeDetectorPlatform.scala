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
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.semconv.SchemaUrls

private[resource] trait ProcessRuntimeDetectorPlatform {
  self: ProcessRuntimeDetector.type =>

  def apply[F[_]: Monad: SystemProperties]: TelemetryResourceDetector[F] =
    new Detector[F]

  private class Detector[F[_]: Monad: SystemProperties] extends TelemetryResourceDetector.Unsealed[F] {
    def name: String = Const.Name

    def detect: F[Option[TelemetryResource]] =
      for {
        runtimeName <- SystemProperties[F].get("java.runtime.name")
        runtimeVersion <- SystemProperties[F].get("java.runtime.version")
        vmVendor <- SystemProperties[F].get("java.vm.vendor")
        vmName <- SystemProperties[F].get("java.vm.name")
        vmVersion <- SystemProperties[F].get("java.vm.version")
      } yield {
        val attributes = Attributes.newBuilder

        val description =
          (vmVendor, vmName, vmVersion).mapN { (vendor, name, version) =>
            s"$vendor $name $version"
          }

        attributes.addAll(Keys.Name.maybe(runtimeName))
        attributes.addAll(Keys.Version.maybe(runtimeVersion))
        attributes.addAll(Keys.Description.maybe(description))

        Some(TelemetryResource(attributes.result(), Some(SchemaUrls.Current)))
      }
  }

}
