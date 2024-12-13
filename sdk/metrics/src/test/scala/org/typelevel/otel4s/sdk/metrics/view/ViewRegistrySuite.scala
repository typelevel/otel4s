/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.sdk.metrics.view

import cats.data.NonEmptyVector
import cats.effect.IO
import cats.syntax.applicative._
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens

class ViewRegistrySuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  import ViewRegistry.toGlobPattern

  test("select by instrument type") {
    PropF.forAllF(
      Gens.instrumentDescriptor,
      Gens.instrumentationScope
    ) { (descriptor, scope) =>
      val selector = InstrumentSelector.builder
        .withInstrumentType(descriptor.instrumentType)
        .build

      val view = View.builder.build

      val registry = ViewRegistry[IO](Vector(RegisteredView(selector, view)))

      registry
        .findViews(descriptor, scope)
        .assertEquals(Some(NonEmptyVector.one(view)))
    }
  }

  test("select by instrument unit") {
    PropF.forAllF(
      Gens.instrumentDescriptor,
      Gens.instrumentationScope
    ) { (descriptor, scope) =>
      val selector = InstrumentSelector.builder
        .withInstrumentUnit(descriptor.unit.filter(_.nonEmpty).getOrElse("*"))
        .build

      val view = View.builder.build

      val registry = ViewRegistry[IO](Vector(RegisteredView(selector, view)))

      registry
        .findViews(descriptor, scope)
        .assertEquals(Some(NonEmptyVector.one(view)))
        .whenA(descriptor.unit.exists(_.nonEmpty))
    }
  }

  test("select by instrument name (wildcard)") {
    PropF.forAllF(
      Gens.instrumentDescriptor,
      Gens.instrumentationScope
    ) { (descriptor, scope) =>
      val selector = InstrumentSelector.builder
        .withInstrumentName("*")
        .build

      val view = View.builder.build

      val registry = ViewRegistry[IO](Vector(RegisteredView(selector, view)))

      registry
        .findViews(descriptor, scope)
        .assertEquals(Some(NonEmptyVector.one(view)))
    }
  }

  test("select by instrument name (exact match)") {
    PropF.forAllF(
      Gens.instrumentDescriptor,
      Gens.instrumentationScope
    ) { (descriptor, scope) =>
      val selector = InstrumentSelector.builder
        .withInstrumentName(descriptor.name.toString)
        .build

      val view = View.builder.build

      val registry = ViewRegistry[IO](Vector(RegisteredView(selector, view)))

      registry
        .findViews(descriptor, scope)
        .assertEquals(Some(NonEmptyVector.one(view)))
    }
  }

  test("select by meter name") {
    PropF.forAllF(
      Gens.instrumentDescriptor,
      Gens.instrumentationScope
    ) { (descriptor, scope) =>
      val selector = InstrumentSelector.builder
        .withMeterName(scope.name)
        .build

      val view = View.builder.build

      val registry = ViewRegistry[IO](Vector(RegisteredView(selector, view)))

      registry
        .findViews(descriptor, scope)
        .assertEquals(Some(NonEmptyVector.one(view)))
        .whenA(scope.name.nonEmpty)
    }
  }

  test("select by meter version") {
    PropF.forAllF(
      Gens.instrumentDescriptor,
      Gens.instrumentationScope
    ) { (descriptor, scope) =>
      val selector = InstrumentSelector.builder
        .withMeterVersion(scope.version.filter(_.nonEmpty).getOrElse("*"))
        .build

      val view = View.builder.build

      val registry = ViewRegistry[IO](Vector(RegisteredView(selector, view)))

      registry
        .findViews(descriptor, scope)
        .assertEquals(Some(NonEmptyVector.one(view)))
        .whenA(scope.version.exists(_.nonEmpty))
    }
  }

  test("select by meter schema url") {
    PropF.forAllF(
      Gens.instrumentDescriptor,
      Gens.instrumentationScope
    ) { (descriptor, scope) =>
      val selector = InstrumentSelector.builder
        .withMeterSchemaUrl(scope.schemaUrl.filter(_.nonEmpty).getOrElse("*"))
        .build

      val view = View.builder.build

      val registry = ViewRegistry[IO](Vector(RegisteredView(selector, view)))

      registry
        .findViews(descriptor, scope)
        .assertEquals(Some(NonEmptyVector.one(view)))
        .whenA(scope.schemaUrl.exists(_.nonEmpty))
    }
  }

  test("select by all params") {
    PropF.forAllF(
      Gens.instrumentDescriptor,
      Gens.instrumentationScope
    ) { (descriptor, scope) =>
      val selector = {
        val builder = InstrumentSelector.builder
          .withInstrumentName(descriptor.name.toString)
          .withInstrumentType(descriptor.instrumentType)

        val withUnit =
          descriptor.unit
            .filter(_.nonEmpty)
            .fold(builder)(builder.withInstrumentUnit)

        val withMeterName =
          Option(scope.name)
            .filter(_.nonEmpty)
            .fold(withUnit)(withUnit.withMeterName)

        val withMeterVersion =
          scope.version
            .filter(_.nonEmpty)
            .fold(withMeterName)(withMeterName.withMeterVersion)

        val withMeterSchemaUrl =
          scope.schemaUrl
            .filter(_.nonEmpty)
            .fold(withMeterVersion)(withUnit.withMeterSchemaUrl)

        withMeterSchemaUrl.build
      }

      val view = View.builder.build

      val registry = ViewRegistry[IO](Vector(RegisteredView(selector, view)))

      registry
        .findViews(descriptor, scope)
        .assertEquals(Some(NonEmptyVector.one(view)))
    }
  }

  test("correctly create a pattern") {
    assertEquals(toGlobPattern("foo").apply("foo"), true)
    assertEquals(toGlobPattern("foo").apply("Foo"), true)
    assertEquals(toGlobPattern("foo").apply("bar"), false)
    assertEquals(toGlobPattern("fo?").apply("foo"), true)
    assertEquals(toGlobPattern("fo??bar").apply("fooo"), false)
    assertEquals(toGlobPattern("fo??bar").apply("fooobar"), true)
    assertEquals(toGlobPattern("fo?").apply("fob"), true)
    assertEquals(toGlobPattern("fo?").apply("fooo"), false)
    assertEquals(toGlobPattern("*").apply("foo"), true)
    assertEquals(toGlobPattern("*").apply("bar"), true)
    assertEquals(toGlobPattern("*").apply("baz"), true)
    assertEquals(toGlobPattern("*").apply("foo.bar.baz"), true)
    assertEquals(toGlobPattern("fo*").apply("fo"), true)
    assertEquals(toGlobPattern("fo*").apply("foo"), true)
    assertEquals(toGlobPattern("fo*").apply("fooo"), true)
    assertEquals(toGlobPattern("fo*bar").apply("foo.bar.baz"), false)
    assertEquals(toGlobPattern("fo*bar").apply("foo.bar.baz.bar"), true)
    assertEquals(toGlobPattern("f()[]$^.{}|").apply("f()[]$^.{}|"), true)
    assertEquals(toGlobPattern("f()[]$^.{}|?").apply("f()[]$^.{}|o"), true)
    assertEquals(toGlobPattern("f()[]$^.{}|*").apply("f()[]$^.{}|ooo"), true)

  }

}
