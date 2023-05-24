package org.typelevel.otel4s.java

import cats.effect.testkit.TestInstances
import cats.effect.{IO, IOLocal}
import cats.mtl.laws.discipline.LocalTests
import munit.{CatsEffectSuite, DisciplineSuite}
import org.scalacheck.Arbitrary.arbString
import org.typelevel.otel4s.java.instances._

class InstancesSuite extends CatsEffectSuite with DisciplineSuite with TestInstances {
  implicit val ticker: Ticker = Ticker()
  implicit val ioLocal: IOLocal[String] = IOLocal("").unsafeRunSync()(munitIORuntime)

  checkAll("IOLocal.LocalLaws", LocalTests[IO, String].local[String, Int])
}
