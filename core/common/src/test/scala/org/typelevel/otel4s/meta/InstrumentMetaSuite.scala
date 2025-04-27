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

package org.typelevel.otel4s.meta

import cats.effect.IO
import munit.CatsEffectSuite

class InstrumentMetaSuite extends CatsEffectSuite {

  test("static - enabled") {
    val enabled = InstrumentMeta.Static.enabled[cats.Id]

    assertEquals(enabled.unit, ())
    assertEquals(enabled.isEnabled, true)
  }

  test("static - disabled") {
    val disabled = InstrumentMeta.Static.disabled[cats.Id]

    assertEquals(disabled.unit, ())
    assertEquals(disabled.isEnabled, false)
  }

  test("dynamic - enabled") {
    val meta = InstrumentMeta.Dynamic.enabled[IO]

    for {
      _ <- assertIO_(meta.unit)
      _ <- assertIO(meta.isEnabled, true)
      _ <- assertIO(IO.ref(false).flatMap(r => meta.whenEnabled(r.set(true)) >> r.get), true)
    } yield ()
  }

  test("dynamic - disabled") {
    val meta = InstrumentMeta.Dynamic.disabled[IO]

    for {
      _ <- assertIO_(meta.unit)
      _ <- assertIO(meta.isEnabled, false)
      _ <- assertIO(IO.ref(false).flatMap(r => meta.whenEnabled(r.set(true)) >> r.get), false)
    } yield ()
  }
}
