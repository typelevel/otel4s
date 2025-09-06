/*
 * Copyright 2025 Typelevel
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

package org.typelevel.otel4s.sdk.logs.processor

import cats.data.NonEmptyList
import cats.effect.IO
import munit.FunSuite
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.logs.LogRecordRef

class LogRecordProcessorSuite extends FunSuite {

  test("create a no-op instance") {
    val processor = LogRecordProcessor.noop[IO]

    assertEquals(processor.toString, "LogRecordProcessor.Noop")
  }

  test("of (empty input) - use noop") {
    val composite = LogRecordProcessor.of[IO]()

    assertEquals(composite.toString, "LogRecordProcessor.Noop")
  }

  test("of (single input) - use this input") {
    val processor = LogRecordProcessor.of(testProcessor("test"))

    assertEquals(processor.toString, "test")
  }

  test("of (multiple) - create a multi instance") {
    val processorA = LogRecordProcessor.of(testProcessor("testA"))
    val processorB = LogRecordProcessor.of(testProcessor("testB"))

    val composite = LogRecordProcessor.of(processorA, processorB)

    assertEquals(composite.toString, "LogRecordProcessor.Multi(testA, testB)")
  }

  test("of (multiple) - flatten out nested multi instances") {
    val processorA = LogRecordProcessor.of(testProcessor("testA"))
    val processorB = LogRecordProcessor.of(testProcessor("testB"))

    val composite1 = LogRecordProcessor.of(processorA, processorB)
    val composite2 = LogRecordProcessor.of(processorA, processorB)

    val composite = LogRecordProcessor.of(composite1, composite2)

    assertEquals(
      composite.toString,
      "LogRecordProcessor.Multi(testA, testB, testA, testB)"
    )
  }

  test("of (multiple) - single failure - rethrow a single failure") {
    val onEmit = new RuntimeException("cannot emit")
    val onFlush = new RuntimeException("cannot flush")

    val failing = testProcessor(
      processorName = "error-prone",
      emit = IO.raiseError(onEmit),
      flush = IO.raiseError(onFlush)
    )

    val processor = LogRecordProcessor.of(testProcessor("success"), failing)

    def expected(e: Throwable) =
      LogRecordProcessor.ProcessorFailure("error-prone", e)

    for {
      emit <- processor.onEmit(Context.root, null).attempt
      flush <- processor.forceFlush.attempt
    } yield {
      assertEquals(emit, Left(expected(onEmit)))
      assertEquals(flush, Left(expected(onFlush)))
    }
  }

  test("of (multiple) - multiple failures - rethrow a composite failure") {
    val onEmit = new RuntimeException("cannot emit")
    val onFlush = new RuntimeException("cannot flush")

    def failing(name: String) = testProcessor(
      processorName = name,
      emit = IO.raiseError(onEmit),
      flush = IO.raiseError(onFlush)
    )

    val processor = LogRecordProcessor.of(
      failing("error-prone-1"),
      failing("error-prone-2")
    )

    def expected(e: Throwable) =
      LogRecordProcessor.CompositeProcessorFailure(
        LogRecordProcessor.ProcessorFailure("error-prone-1", e),
        NonEmptyList.of(LogRecordProcessor.ProcessorFailure("error-prone-2", e))
      )

    for {
      emit <- processor.onEmit(Context.root, null).attempt
      flush <- processor.forceFlush.attempt
    } yield {
      assertEquals(emit, Left(expected(onEmit)))
      assertEquals(flush, Left(expected(onFlush)))
    }
  }

  private def testProcessor(
      processorName: String,
      emit: IO[Unit] = IO.unit,
      flush: IO[Unit] = IO.unit,
  ): LogRecordProcessor[IO] =
    new LogRecordProcessor.Unsealed[IO] {
      def name: String = processorName
      def onEmit(context: Context, logRecord: LogRecordRef[IO]): IO[Unit] = emit
      def forceFlush: IO[Unit] = flush
    }

}
