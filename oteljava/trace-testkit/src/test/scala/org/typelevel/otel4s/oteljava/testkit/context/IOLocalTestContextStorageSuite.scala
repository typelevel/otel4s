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
