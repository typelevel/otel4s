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
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.semconv.SchemaUrls

private[resource] trait ProcessRuntimeDetectorPlatform {
  self: ProcessRuntimeDetector.type =>

  def apply[F[_]: Sync]: TelemetryResourceDetector[F] =
    new Detector[F]

  private class Detector[F[_]: Sync] extends TelemetryResourceDetector[F] {
    def name: String = Const.Name

    def detect: F[Option[TelemetryResource]] =
      for {
        runtimeName <- Sync[F].delay(sys.props.get("java.runtime.name"))
        runtimeVersion <- Sync[F].delay(sys.props.get("java.runtime.version"))
        vmVendor <- Sync[F].delay(sys.props.get("java.vm.vendor"))
        vmName <- Sync[F].delay(sys.props.get("java.vm.name"))
        vmVersion <- Sync[F].delay(sys.props.get("java.vm.version"))
      } yield {
        val attributes = Attributes.newBuilder

        runtimeName.foreach(name => attributes.addOne(Keys.Name(name)))
        runtimeVersion.foreach(name => attributes.addOne(Keys.Version(name)))
        (vmVendor, vmName, vmVersion).mapN { (vendor, name, version) =>
          attributes.addOne(Keys.Description(s"$vendor $name $version"))
        }

        Some(TelemetryResource(attributes.result(), Some(SchemaUrls.Current)))
      }
  }

}
