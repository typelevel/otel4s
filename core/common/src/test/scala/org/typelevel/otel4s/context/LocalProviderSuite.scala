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

package org.typelevel.otel4s.context

import cats.data.Kleisli
import cats.effect.IO
import cats.effect.IOLocal
import cats.mtl.Local
import munit.CatsEffectSuite
import org.typelevel.otel4s.context.vault.VaultContext

class LocalProviderSuite extends CatsEffectSuite {

  test("lift LocalProvider from implicit IOLocal") {
    IOLocal(VaultContext.root).map { implicit ioLocal =>
      assertEquals(
        LocalProvider[IO, VaultContext].toString,
        "LocalProvider.fromIOLocal"
      )
    }
  }

  test("lift LocalProvider from implicit Local (1)") {
    IOLocal(VaultContext.root).map { ioLocal =>
      implicit val local: Local[IO, VaultContext] = ioLocal.asLocal

      assertEquals(
        LocalProvider[IO, VaultContext].toString,
        "LocalProvider.fromLocal"
      )
    }
  }

  // liftFromLocal is prioritized over liftFromLiftIO
  test("lift LocalProvider from implicit Local (2)") {
    assertEquals(
      LocalProvider[Kleisli[IO, VaultContext, *], VaultContext].toString,
      "LocalProvider.fromLocal"
    )
  }

  test("lift LocalProvider for IO") {
    assertEquals(
      LocalProvider[IO, VaultContext].toString,
      "LocalProvider.fromLiftIO"
    )
  }

  test("fail when multiple Local source are in the implicit scope") {
    val err = compileErrors(
      """
        implicit val ioLocal: IOLocal[VaultContext] = ???
        implicit val local: Local[IO, VaultContext] = ???
        val provider = LocalProvider[IO, VaultContext]
      """
    )

    assert(err.toLowerCase.contains("ambiguous"), err)
  }

  test("fail when no Local source are in the implicit scope") {
    val err = compileErrors(
      """
        val provider = LocalProvider[cats.effect.SyncIO, VaultContext]
      """
    )

    assert(err.contains("Cannot find the `LocalProvider` for"), err)
  }
}
