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
package processor

import cats.Foldable
import cats.effect.IO
import cats.effect.std.Console
import cats.syntax.foldable._
import cats.syntax.traverse._
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Test
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.exporter.InMemorySpanExporter
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter
import org.typelevel.otel4s.sdk.trace.scalacheck.Arbitraries._
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.StatusCode

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration

class SimpleSpanProcessorSuite
    extends CatsEffectSuite
    with ScalaCheckEffectSuite {

  private implicit val noopConsole: Console[IO] = new NoopConsole[IO]

  test("show details in the name") {
    val exporter = new FailingExporter(
      "error-prone",
      new RuntimeException("something went wrong")
    )

    val processor = SimpleSpanProcessor(exporter, exportOnlySampled = false)

    val expected =
      "SimpleSpanProcessor{exporter=error-prone, exportOnlySampled=false}"

    assertEquals(processor.name, expected)
  }

  test("do nothing on start") {
    PropF.forAllF { (spans: List[SpanData]) =>
      for {
        exporter <- InMemorySpanExporter.create[IO](None)
        processor = SimpleSpanProcessor(exporter, exportOnlySampled = true)
        _ <- spans.traverse_(s => processor.onStart(None, constSpanRef(s)))
        exported <- exporter.finishedSpans
      } yield assert(exported.isEmpty)
    }
  }

  test("export only sampled spans on end (exportOnlySampled = true)") {
    PropF.forAllF { (spans: List[SpanData]) =>
      val sampled = spans.filter(_.spanContext.isSampled)

      for {
        exporter <- InMemorySpanExporter.create[IO](None)
        processor = SimpleSpanProcessor(exporter, exportOnlySampled = true)
        _ <- spans.traverse_(span => processor.onEnd(span))
        exported <- exporter.finishedSpans
      } yield assertEquals(exported, sampled)
    }
  }

  test("export all spans on end (exportOnlySampled = false)") {
    PropF.forAllF { (spans: List[SpanData]) =>
      for {
        exporter <- InMemorySpanExporter.create[IO](None)
        processor = SimpleSpanProcessor(exporter, exportOnlySampled = false)
        _ <- spans.traverse_(span => processor.onEnd(span))
        exported <- exporter.finishedSpans
      } yield assertEquals(
        exported.map(_.spanContext.spanIdHex),
        spans.map(_.spanContext.spanIdHex)
      )
    }
  }

  test("do not rethrow export errors") {
    PropF.forAllF { (spans: List[SpanData]) =>
      val error = new RuntimeException("something went wrong")
      val exporter = new FailingExporter("error-prone", error)
      val processor = SimpleSpanProcessor(exporter, exportOnlySampled = false)

      for {
        attempts <- spans.traverse(span => processor.onEnd(span).attempt)
      } yield assertEquals(attempts, List.fill(spans.size)(Right(())))
    }
  }

  private def constSpanRef(data: SpanData): SpanRef[IO] = {
    new SpanRef[IO] with Span.Backend[IO] {
      private val noopBackend = Span.Backend.noop[IO]

      def kind: SpanKind =
        data.kind

      def scopeInfo: InstrumentationScope =
        data.instrumentationScope

      def parentSpanContext: Option[SpanContext] =
        data.parentSpanContext

      def name: IO[String] =
        IO(data.name)

      def toSpanData: IO[SpanData] =
        IO(data)

      def hasEnded: IO[Boolean] =
        IO(false)

      def duration: IO[FiniteDuration] =
        IO(data.startTimestamp)

      def getAttribute[A](key: AttributeKey[A]): IO[Option[A]] =
        IO(data.attributes.get(key).map(_.value))

      // span.backend

      val meta: InstrumentMeta[IO] =
        noopBackend.meta

      val context: SpanContext =
        noopBackend.context

      def updateName(name: String): IO[Unit] =
        noopBackend.updateName(name)

      def addAttributes(
          attributes: immutable.Iterable[Attribute[_]]
      ): IO[Unit] =
        noopBackend.addAttributes(attributes)

      def addEvent(
          name: String,
          attributes: immutable.Iterable[Attribute[_]]
      ): IO[Unit] =
        noopBackend.addEvent(name, attributes)

      def addEvent(
          name: String,
          timestamp: FiniteDuration,
          attributes: immutable.Iterable[Attribute[_]]
      ): IO[Unit] =
        noopBackend.addEvent(name, timestamp, attributes)

      def addLink(
          spanContext: SpanContext,
          attributes: immutable.Iterable[Attribute[_]]
      ): IO[Unit] =
        noopBackend.addLink(spanContext, attributes)

      def recordException(
          exception: Throwable,
          attributes: immutable.Iterable[Attribute[_]]
      ): IO[Unit] =
        noopBackend.recordException(exception, attributes)

      def setStatus(status: StatusCode): IO[Unit] =
        noopBackend.setStatus(status)

      def setStatus(status: StatusCode, description: String): IO[Unit] =
        noopBackend.setStatus(status, description)

      private[otel4s] def end: IO[Unit] =
        noopBackend.end

      private[otel4s] def end(timestamp: FiniteDuration): IO[Unit] =
        noopBackend.end(timestamp)
    }
  }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(10)
      .withMaxSize(10)

  private class FailingExporter(
      exporterName: String,
      onExport: Throwable
  ) extends SpanExporter[IO] {
    def name: String = exporterName

    def exportSpans[G[_]: Foldable](spans: G[SpanData]): IO[Unit] =
      IO.raiseError(onExport)

    def flush: IO[Unit] =
      IO.unit
  }

}
