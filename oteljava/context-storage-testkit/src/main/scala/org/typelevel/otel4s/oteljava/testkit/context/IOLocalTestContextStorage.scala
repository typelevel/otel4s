/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.oteljava.testkit.context

import cats.effect.LiftIO
import cats.effect.MonadCancelThrow
import cats.effect.std.Console
import cats.mtl.Local
import io.opentelemetry.sdk.testing.context.SettableContextStorageProvider
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.context.IOLocalContextStorage

object IOLocalTestContextStorage {

  def localProvider[F[_]: MonadCancelThrow: Console: LiftIO]: LocalProvider[F, Context] =
    new LocalProvider.Unsealed[F, Context] {
      val delegate = IOLocalContextStorage.localProvider[F]

      override def local: F[Local[F, Context]] =
        SettableContextStorageProvider.getContextStorage() match {
          case storage: IOLocalContextStorage => IOLocalContextStorage.whenPropagationEnabled(storage.local)
          case _                              => delegate.local
        }
    }

}
