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

package org.typelevel.otel4s.sdk.metrics.exemplar

import cats.effect.SyncIO
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens

class ExemplarFilterSuite extends ScalaCheckSuite {

  private val valueGen: Gen[Either[Long, Double]] =
    Gen.either(Gen.long, Gen.double)

  private val traceContextKey = Context.Key
    .unique[SyncIO, ExemplarData.TraceContext]("trace-context")
    .unsafeRunSync()

  test("alwaysOn - allow all values") {
    val filter = ExemplarFilter.alwaysOn

    Prop.forAll(valueGen, Gens.attributes) {
      case (Left(value), attributes) =>
        assert(filter.shouldSample(value, attributes, Context.root))

      case (Right(value), attributes) =>
        assert(filter.shouldSample(value, attributes, Context.root))
    }
  }

  test("alwaysOff - forbid all values") {
    val filter = ExemplarFilter.alwaysOff

    Prop.forAll(valueGen, Gens.attributes) {
      case (Left(value), attributes) =>
        assert(!filter.shouldSample(value, attributes, Context.root))

      case (Right(value), attributes) =>
        assert(!filter.shouldSample(value, attributes, Context.root))
    }
  }

  test("traceBased - forbid all values when TraceContextLookup is noop") {
    val filter = ExemplarFilter.traceBased(TraceContextLookup.noop)

    Prop.forAll(valueGen, Gens.attributes) {
      case (Left(value), attributes) =>
        assert(!filter.shouldSample(value, attributes, Context.root))

      case (Right(value), attributes) =>
        assert(!filter.shouldSample(value, attributes, Context.root))
    }
  }

  test("traceBased - decide according to the tracing context") {
    val filter = ExemplarFilter.traceBased(_.get(traceContextKey))

    Prop.forAll(valueGen, Gens.attributes, Gens.traceContext) {
      case (Left(value), attributes, traceContext) =>
        val ctx = Context.root.updated(traceContextKey, traceContext)

        assertEquals(
          filter.shouldSample(value, attributes, ctx),
          traceContext.isSampled
        )

      case (Right(value), attributes, traceContext) =>
        val ctx = Context.root.updated(traceContextKey, traceContext)

        assertEquals(
          filter.shouldSample(value, attributes, ctx),
          traceContext.isSampled
        )
    }
  }

}
