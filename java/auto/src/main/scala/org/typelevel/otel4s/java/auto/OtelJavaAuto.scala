/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s.java.auto

import cats.effect.Async
import cats.effect.LiftIO
import cats.effect.Resource
import cats.effect.Sync
import cats.syntax.functor._
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import org.typelevel.otel4s.Otel4s
import org.typelevel.otel4s.java.OtelJava

object OtelJavaAuto {

  /** Creates an auto-configured [[org.typelevel.otel4s.Otel4s]] resource.
    */
  def resource[F[_]: LiftIO: Async]: Resource[F, Otel4s[F]] =
    OtelJava.resource(
      Sync[F]
        .delay(
          AutoConfiguredOpenTelemetrySdk.builder
            .registerShutdownHook(false)
            .setResultAsGlobal(false)
            .build()
        )
        .map(_.getOpenTelemetrySdk)
    )
}
