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
import munit.FunSuite
import org.typelevel.otel4s.context.syntax._
import org.typelevel.otel4s.context.vault.VaultContext

class ContextSuite extends FunSuite {
  test("implicit syntax") {
    def check[C, K[X] <: Key[X]](implicit
        c: Contextual.Keyed[C, K],
        kp: Key.Provider[SyncIO, K]
    ): Unit = {
      val key1 = kp.uniqueKey[String]("key1").unsafeRunSync()
      val key2 = kp.uniqueKey[Int]("key2").unsafeRunSync()

      var ctx = c.root
      assertEquals(ctx.get(key1), None)
      assertEquals(ctx.get(key2), None)

      ctx = ctx.updated(key1, "1")
      assertEquals(ctx.get(key1), Some("1"))
      assertEquals(ctx.get(key2), None)

      ctx = ctx.updated(key1, "2")
      assertEquals(ctx.get(key1), Some("2"))
      assertEquals(ctx.get(key2), None)

      ctx = ctx.updated(key2, 1)
      assertEquals(ctx.get(key1), Some("2"))
      assertEquals(ctx.get(key2), Some(1))
    }

    check[VaultContext, VaultContext.Key]
  }
}
