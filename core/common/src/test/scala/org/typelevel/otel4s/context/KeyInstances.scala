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

import cats.effect.SyncIO
import org.scalacheck.Arbitrary
import org.scalacheck.Cogen
import org.scalacheck.Gen
import org.typelevel.otel4s.context.vault.VaultContext

trait KeyInstances {
  protected def genVaultKey[A]: Gen[VaultContext.Key[A]] =
    Gen.delay {
      for (name <- Arbitrary.arbitrary[String])
        yield VaultContext.Key.unique[SyncIO, A](name).unsafeRunSync()
    }

  protected implicit def arbitraryVaultKey[A]: Arbitrary[VaultContext.Key[A]] =
    Arbitrary(genVaultKey)
  protected implicit def arbitraryKey[A]: Arbitrary[Key[A]] =
    Arbitrary(genVaultKey)

  protected implicit def cogenVaultKey[A]: Cogen[VaultContext.Key[A]] =
    Cogen[String].contramap(_.name)
  protected implicit def cogenKey[A]: Cogen[Key[A]] =
    Cogen[String].contramap(_.name)
}
