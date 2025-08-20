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

import scala.util.Try

private[resource] trait ProcessDetectorPlatform { self: ProcessDetector.type =>

  def apply[F[_]: Sync]: TelemetryResourceDetector[F] =
    new Detector[F]

  private class Detector[F[_]: Sync] extends TelemetryResourceDetector.Unsealed[F] {
    def name: String = Const.Name

    def detect: F[Option[TelemetryResource]] = Sync[F].delay {
      val argv = Process.argv.toList

      val command =
        if (argv.length > 1) Attributes(Keys.Command(Process.argv(1)))
        else Attributes.empty

      val owner =
        Try(
          Attributes(Keys.Owner(OS.userInfo().username))
        ).getOrElse(Attributes.empty)

      val args =
        argv.headOption.toSeq ++ Process.execArgv ++ argv.drop(1)

      val attributes = Attributes(
        Keys.Pid(Process.pid.toLong),
        Keys.ExecutableName(Process.title),
        Keys.ExecutablePath(Process.execPath),
        Keys.CommandArgs(args)
      ) ++ command ++ owner

      Some(TelemetryResource(attributes, Some(SchemaUrls.Current)))
    }
  }

}
