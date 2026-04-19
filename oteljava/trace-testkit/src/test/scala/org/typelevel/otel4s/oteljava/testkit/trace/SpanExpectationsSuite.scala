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

import org.typelevel.otel4s.trace.SpanKind

class SpanExpectationsSuite extends TraceExpectationSupport {

  testkitTest("checkAllDistinct requires distinct matches and format renders failures") { testkit =>
    for {
      tracer <- testkit.tracerProvider.get("service")
      _ <- tracer.spanBuilder("db.query").withSpanKind(SpanKind.Client).build.use_
      _ <- tracer.spanBuilder("render").withSpanKind(SpanKind.Internal).build.use_
      spans <- testkit.finishedSpans
    } yield {
      assertSuccess(
        SpanExpectations.checkAllDistinct(
          spans,
          SpanExpectation.client("db.query"),
          SpanExpectation.internal("render")
        )
      )

      val mismatches = SpanExpectations
        .checkAllDistinct(
          spans.take(1),
          List(SpanExpectation.client("db.query"), SpanExpectation.client("db.query"))
        )
        .left
        .toOption
        .get

      val rendered = SpanExpectations.format(mismatches)
      assert(rendered.contains("Span expectations failed:"))
      assert(rendered.contains("no distinct span remained"))
    }
  }
}
