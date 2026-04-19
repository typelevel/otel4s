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

package org.typelevel.otel4s.oteljava.testkit.trace

import io.opentelemetry.sdk.trace.data.SpanData

class TraceExpectationsSuite extends TraceExpectationSupport {

  testkitTest("check matches an unordered forest") { testkit =>
    for {
      tracer <- testkit.tracerProvider.get("service")
      _ <- tracer.spanBuilder("outer").build.use { outer =>
        tracer.spanBuilder("body-1").withParent(outer.context).build.use_ >>
          tracer.spanBuilder("body-2").withParent(outer.context).build.use_
      }
      spans <- testkit.finishedSpans
    } yield {
      val expectation = TraceForestExpectation.unordered(
        TraceExpectation.unordered(
          SpanExpectation.name("outer").noParentSpanContext,
          TraceExpectation.leaf(SpanExpectation.name("body-1")),
          TraceExpectation.leaf(SpanExpectation.name("body-2"))
        )
      )

      assertSuccess(TraceExpectations.check(spans, expectation))
    }
  }

  testkitTest("check fails when a child is attached to the wrong parent") { testkit =>
    val missingParent = spanContext(
      traceIdHex = "0af7651916cd43dd8448eb211c80319c",
      spanIdHex = "0000000000000042",
      sampled = true
    )

    for {
      tracer <- testkit.tracerProvider.get("service")
      _ <- tracer.spanBuilder("outer").build.use_
      _ <- tracer.spanBuilder("other-root").build.use { other =>
        tracer.spanBuilder("body").withParent(other.context).build.use_
      }
      _ <- tracer.spanBuilder("detached-root").withParent(missingParent).build.use_
      spans <- testkit.finishedSpans.map(_.sortBy(_.getName))
    } yield {
      val expectation = TraceForestExpectation.unordered(
        TraceExpectation.unordered(
          SpanExpectation.name("outer").noParentSpanContext,
          TraceExpectation.leaf(SpanExpectation.name("body"))
        ),
        TraceExpectation.leaf(SpanExpectation.name("other-root").noParentSpanContext),
        TraceExpectation.leaf(
          SpanExpectation.name("detached-root").parentSpanContextExact(missingParent)
        )
      )

      val result = TraceExpectations.check(spans, expectation)
      assert(result.isLeft)
      assert(TraceExpectations.format(result.swap.toOption.get).contains("trace mismatch"))
    }
  }

  testkitTest("check reports root count mismatches") { testkit =>
    for {
      tracer <- testkit.tracerProvider.get("service")
      _ <- tracer.spanBuilder("one").build.use_
      _ <- tracer.spanBuilder("two").build.use_
      spans <- testkit.finishedSpans.map(_.sortBy(_.getName))
    } yield {
      val result = TraceExpectations.check(
        spans,
        TraceForestExpectation.unordered(
          TraceExpectation.leaf(SpanExpectation.name("one").noParentSpanContext)
        )
      )

      assertEquals(
        result,
        Left(
          cats.data.NonEmptyList.one(
            TraceForestExpectation.Mismatch.RootCountMismatch(1, 2, List("one", "two"))
          )
        )
      )
    }
  }

  test("check matches an empty forest") {
    assertEquals(
      TraceExpectations.check(Nil, TraceForestExpectation.empty),
      Right(())
    )
  }

  testkitTest("check reports root count mismatch for non-empty forest against empty expectation") { testkit =>
    for {
      tracer <- testkit.tracerProvider.get("service")
      _ <- tracer.spanBuilder("one").build.use_
      spans <- testkit.finishedSpans
    } yield {
      assertEquals(
        TraceExpectations.check(spans, TraceForestExpectation.empty),
        Left(
          cats.data.NonEmptyList.one(
            TraceForestExpectation.Mismatch.RootCountMismatch(0, 1, List("one"))
          )
        )
      )
    }
  }

  testkitTest("check ordered children require matching order") { testkit =>
    for {
      tracer <- testkit.tracerProvider.get("service")
      _ <- tracer.spanBuilder("root").build.use { root =>
        tracer.spanBuilder("first").withParent(root.context).build.use_ >>
          tracer.spanBuilder("second").withParent(root.context).build.use_
      }
      spans <- testkit.finishedSpans.map(reorderByName("second", "root", "first"))
    } yield {
      val result = TraceExpectations.check(
        spans,
        TraceForestExpectation.unordered(
          TraceExpectation.ordered(
            SpanExpectation.name("root").noParentSpanContext,
            TraceExpectation.leaf(SpanExpectation.name("first")),
            TraceExpectation.leaf(SpanExpectation.name("second"))
          )
        )
      )

      assert(result.isLeft)
      assert(TraceExpectations.format(result.swap.toOption.get).contains("trace subtree mismatch"))
    }
  }

  testkitTest("check ordered roots require matching order") { testkit =>
    for {
      tracer <- testkit.tracerProvider.get("service")
      _ <- tracer.spanBuilder("one").build.use_
      _ <- tracer.spanBuilder("two").build.use_
      spans <- testkit.finishedSpans.map(_.sortBy(_.getName))
    } yield {
      val result = TraceExpectations.check(
        spans,
        TraceForestExpectation.ordered(
          TraceExpectation.leaf(SpanExpectation.name("two").noParentSpanContext),
          TraceExpectation.leaf(SpanExpectation.name("one").noParentSpanContext)
        )
      )

      assert(result.isLeft)
      assert(TraceExpectations.format(result.swap.toOption.get).contains("trace mismatch for root 'one'"))
    }
  }

  testkitTest("check includes trace expectation clue in subtree failures") { testkit =>
    for {
      tracer <- testkit.tracerProvider.get("service")
      _ <- tracer.spanBuilder("outer").build.use_
      spans <- testkit.finishedSpans
    } yield {
      val result = TraceExpectations.check(
        spans,
        TraceForestExpectation.unordered(
          TraceExpectation
            .unordered(
              SpanExpectation.name("outer").noParentSpanContext,
              TraceExpectation.leaf(SpanExpectation.name("body"))
            )
            .clue("outer subtree")
        )
      )

      assert(result.isLeft)
      assert(TraceExpectations.format(result.swap.toOption.get).contains("trace expectation mismatch [outer subtree]"))
    }
  }

  testkitTest("check includes traces expectation clue in root failures") { testkit =>
    for {
      tracer <- testkit.tracerProvider.get("service")
      _ <- tracer.spanBuilder("one").build.use_
      spans <- testkit.finishedSpans
    } yield {
      val result = TraceExpectations.check(
        spans,
        TraceForestExpectation
          .unordered(
            TraceExpectation.leaf(SpanExpectation.name("missing").noParentSpanContext)
          )
          .clue("entire export")
      )

      assert(result.isLeft)
      assert(TraceExpectations.format(result.swap.toOption.get).contains("trace expectations mismatch [entire export]"))
    }
  }

  private def reorderByName(names: String*)(spans: List[SpanData]): List[SpanData] = {
    val byName = spans.map(span => span.getName -> span).toMap
    names.toList.map(byName)
  }
}
