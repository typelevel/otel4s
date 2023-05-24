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
