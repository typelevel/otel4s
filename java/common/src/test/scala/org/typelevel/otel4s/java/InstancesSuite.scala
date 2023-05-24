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

package org.typelevel.otel4s.java

import cats.effect.testkit.TestInstances
import cats.effect.{IO, IOLocal}
import cats.mtl.laws.discipline.LocalTests
import munit.DisciplineSuite
import org.scalacheck.Arbitrary.arbString
import org.typelevel.otel4s.java.instances._

class InstancesSuite extends DisciplineSuite with TestInstances {
  implicit val ticker: Ticker = Ticker()

  unsafeRun {
    IOLocal("").map {implicit ioLocal =>
      checkAll("IOLocal.LocalLaws", LocalTests[IO, String].local[String, Int])
    }
  }
}
