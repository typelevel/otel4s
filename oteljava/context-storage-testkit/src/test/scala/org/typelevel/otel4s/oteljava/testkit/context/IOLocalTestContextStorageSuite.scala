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

package org.typelevel.otel4s.oteljava.testkit.context

import cats.effect.IO
import io.opentelemetry.context.ContextStorage
import io.opentelemetry.sdk.testing.context.SettableContextStorageProvider
import munit._
import org.typelevel.otel4s.oteljava.context.IOLocalContextStorage

class IOLocalTestContextStorageSuite extends CatsEffectSuite {

  test("unwrap SettableContextStorageProvider") {
    for {
      error <- IOLocalContextStorage.localProvider[IO].local.attempt
      success <- IOLocalTestContextStorage.localProvider[IO].local.attempt
    } yield {
      // testing that we're indeed using a SettableContextStorageProvider$SettableContextStorage
      assertEquals(
        ContextStorage.get().getClass: Any,
        new SettableContextStorageProvider().get().getClass: Any
      )
      // backed by a IOLocalContextStorage
      assertEquals(
        SettableContextStorageProvider.getContextStorage().getClass: Any,
        classOf[IOLocalContextStorage]: Any
      )
      assert(error.isLeft) // failed the IOLocalContextStorage
      assert(success.isRight) // properly unwrapped from SettableContextStorageProvider
    }
  }

}
