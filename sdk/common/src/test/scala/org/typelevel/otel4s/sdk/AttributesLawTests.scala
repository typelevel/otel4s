package org.typelevel.otel4s.sdk

import cats.Hash
import cats.implicits.catsKernelStdHashForList
import cats.kernel.Eq
import cats.kernel.laws.discipline.MonoidTests
import munit.DisciplineSuite
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.sdk.arbitrary.attributes

class AttributesLawTests extends DisciplineSuite {

  implicit def hashAttribute: Hash[Attribute[_]] =
    Hash.fromUniversalHashCode[Attribute[_]]

  implicit val attributesEq: Eq[Attributes] = Eq.by(_.toList)

  checkAll("Attributes.MonoidLaws", MonoidTests[Attributes].monoid)

}
