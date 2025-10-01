package org.typelevel.otel4s.trace.meta

import cats.effect.IO
import munit.CatsEffectSuite

class InstrumentMetaSuite extends CatsEffectSuite {

  test("enabled") {
    val meta = InstrumentMeta.enabled[IO]

    for {
      _ <- assertIO_(meta.unit)
      _ <- assertIO(meta.isEnabled, true)
      _ <- assertIO(IO.ref(false).flatMap(r => meta.whenEnabled(r.set(true)) >> r.get), true)
    } yield ()
  }

  test("disabled") {
    val meta = InstrumentMeta.disabled[IO]

    for {
      _ <- assertIO_(meta.unit)
      _ <- assertIO(meta.isEnabled, false)
      _ <- assertIO(IO.ref(false).flatMap(r => meta.whenEnabled(r.set(true)) >> r.get), false)
    } yield ()
  }

  test("dynamic - from") {
    for {
      enabled <- IO.ref(false)
      meta = InstrumentMeta.from[IO](enabled.get)

      // disabled
      _ <- assertIO_(meta.unit)
      _ <- assertIO(meta.isEnabled, false)
      _ <- assertIO(IO.ref(false).flatMap(r => meta.whenEnabled(r.set(true)) >> r.get), false)

      // enabled
      _ <- enabled.set(true)
      _ <- assertIO_(meta.unit)
      _ <- assertIO(meta.isEnabled, true)
      _ <- assertIO(IO.ref(false).flatMap(r => meta.whenEnabled(r.set(true)) >> r.get), true)
    } yield ()
  }
}
