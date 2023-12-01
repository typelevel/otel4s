/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk.trace

import cats.data.NonEmptyList
import cats.effect.IO
import munit.FunSuite
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.trace.SpanContext

class SpanProcessorSuite extends FunSuite {

  test("create a no-op instance") {
    val processor = SpanProcessor.noop[IO]

    assertEquals(processor.toString, "SpanProcessor.Noop")
  }

  test("of (empty input) - use noop") {
    val composite = SpanProcessor.of[IO]()

    assertEquals(composite.toString, "SpanProcessor.Noop")
  }

  test("of (single input) - use this input") {
    val processor = SpanProcessor.of(testProcessor("test"))

    assertEquals(processor.toString, "test")
  }

  test("of (multiple) - create a multi instance") {
    val processorA = SpanProcessor.of(testProcessor("testA"))
    val processorB = SpanProcessor.of(testProcessor("testB"))

    val composite = SpanProcessor.of(processorA, processorB)

    assertEquals(composite.toString, "SpanProcessor.Multi(testA, testB)")
  }

  test("of (multiple) - flatten out nested multi instances") {
    val processorA = SpanProcessor.of(testProcessor("testA"))
    val processorB = SpanProcessor.of(testProcessor("testB"))

    val composite1 = SpanProcessor.of(processorA, processorB)
    val composite2 = SpanProcessor.of(processorA, processorB)

    val composite = SpanProcessor.of(composite1, composite2)

    assertEquals(
      composite.toString,
      "SpanProcessor.Multi(testA, testB, testA, testB)"
    )
  }

  test("of (multiple) - single failure - rethrow a single failure") {
    val onStart = new RuntimeException("cannot start")
    val onEnd = new RuntimeException("cannot end")
    val onFlush = new RuntimeException("cannot flush")

    val failing = testProcessor(
      processorName = "error-prone",
      start = IO.raiseError(onStart),
      end = IO.raiseError(onEnd),
      flush = IO.raiseError(onFlush)
    )

    val processor = SpanProcessor.of(testProcessor("success"), failing)

    def expected(e: Throwable) =
      SpanProcessor.ProcessorFailure("error-prone", e)

    for {
      start <- processor.onStart(None, null: SpanRef[IO]).attempt
      end <- processor.onEnd(null).attempt
      flush <- processor.forceFlush.attempt
    } yield {
      assertEquals(start, Left(expected(onStart)))
      assertEquals(end, Left(expected(onEnd)))
      assertEquals(flush, Left(expected(onFlush)))
    }
  }

  test("of (multiple) - multiple failures - rethrow a composite failure") {
    val onStart = new RuntimeException("cannot start")
    val onEnd = new RuntimeException("cannot end")
    val onFlush = new RuntimeException("cannot flush")

    def failing(name: String) = testProcessor(
      processorName = name,
      start = IO.raiseError(onStart),
      end = IO.raiseError(onEnd),
      flush = IO.raiseError(onFlush)
    )

    val processor = SpanProcessor.of(
      failing("error-prone-1"),
      failing("error-prone-2")
    )

    def expected(e: Throwable) =
      SpanProcessor.CompositeProcessorFailure(
        SpanProcessor.ProcessorFailure("error-prone-1", e),
        NonEmptyList.of(SpanProcessor.ProcessorFailure("error-prone-2", e))
      )

    for {
      start <- processor.onStart(None, null).attempt
      end <- processor.onEnd(null).attempt
      flush <- processor.forceFlush.attempt
    } yield {
      assertEquals(start, Left(expected(onStart)))
      assertEquals(end, Left(expected(onEnd)))
      assertEquals(flush, Left(expected(onFlush)))
    }
  }

  private def testProcessor(
      processorName: String,
      start: IO[Unit] = IO.unit,
      end: IO[Unit] = IO.unit,
      flush: IO[Unit] = IO.unit,
  ): SpanProcessor[IO] =
    new SpanProcessor[IO] {
      def name: String =
        processorName

      def onStart(
          parentContext: Option[SpanContext],
          span: SpanRef[IO]
      ): IO[Unit] =
        start

      def isStartRequired: Boolean =
        false

      def onEnd(span: SpanData): IO[Unit] =
        end

      def isEndRequired: Boolean =
        false

      def forceFlush: IO[Unit] =
        flush
    }

}
