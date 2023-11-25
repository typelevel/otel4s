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

  private def testProcessor(name: String): SpanProcessor[IO] =
    new SpanProcessor[IO] {
      def onStart(
          parentContext: Option[SpanContext],
          span: SpanRef[IO]
      ): IO[Unit] =
        IO.unit

      def isStartRequired: Boolean =
        false

      def onEnd(span: SpanData): IO[Unit] =
        IO.unit

      def isEndRequired: Boolean =
        false

      def forceFlush: IO[Unit] =
        IO.unit

      override def toString: String = name
    }

}
