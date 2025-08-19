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
import cats.effect.std.SystemProperties
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.semconv.SchemaUrls

import java.net.InetAddress

private[resource] trait HostDetectorPlatform { self: HostDetector.type =>

  def apply[F[_]: Sync: SystemProperties]: TelemetryResourceDetector[F] =
    new Detector[F]

  private class Detector[F[_]: Sync: SystemProperties] extends TelemetryResourceDetector.Unsealed[F] {
    def name: String = Const.Name

    def detect: F[Option[TelemetryResource]] =
      for {
        host <- Sync[F].blocking(InetAddress.getLocalHost.getHostName).redeem(_ => None, Some(_))
        arch <- SystemProperties[F].get("os.arch")
      } yield {
        val builder = Attributes.newBuilder

        builder.addAll(Keys.Host.maybe(host))
        builder.addAll(Keys.Arch.maybe(arch))

        val attributes = builder.result()

        Option.when(attributes.nonEmpty)(
          TelemetryResource(attributes, Some(SchemaUrls.Current))
        )
      }
  }

}
