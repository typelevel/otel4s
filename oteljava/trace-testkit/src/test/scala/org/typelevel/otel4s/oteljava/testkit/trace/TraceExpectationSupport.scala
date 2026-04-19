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

import cats.data.NonEmptyList
import cats.effect.IO
import io.opentelemetry.sdk.resources.{Resource => JResource}
import io.opentelemetry.sdk.trace.data.SpanData
import munit.CatsEffectSuite
import munit.Location
import munit.TestOptions
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.oteljava.AttributeConverters._
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.StatusCode
import org.typelevel.otel4s.trace.TraceFlags
import org.typelevel.otel4s.trace.TraceState
import scodec.bits.ByteVector

import scala.util.chaining._

trait TraceExpectationSupport extends CatsEffectSuite {

  protected def testkitTest(name: String)(
      body: TracesTestkit[IO] => IO[Unit]
  )(implicit loc: Location): Unit =
    test(TestOptions(name)) {
      TracesTestkit.inMemory[IO]().use(body)
    }

  protected def testkitTest(name: String, resourceAttributes: Attribute[_]*)(
      body: TracesTestkit[IO] => IO[Unit]
  )(implicit loc: Location): Unit =
    test(TestOptions(name)) {
      TracesTestkit
        .inMemory[IO](
          _.addTracerProviderCustomizer(
            _.setResource(JResource.create(Attributes(resourceAttributes: _*).toJavaAttributes))
          )
        )
        .use(body)
    }

  protected def exportedSingleSpan(testkit: TracesTestkit[IO]): IO[SpanData] =
    testkit.finishedSpans.flatMap {
      case span :: Nil => IO.pure(span)
      case other       => IO.raiseError(new AssertionError(s"expected one span, got ${other.length}"))
    }

  protected def spanContext(
      traceIdHex: String,
      spanIdHex: String,
      sampled: Boolean = false,
      remote: Boolean = false
  ): SpanContext =
    SpanContext(
      traceId = ByteVector.fromValidHex(traceIdHex),
      spanId = ByteVector.fromValidHex(spanIdHex),
      traceFlags = TraceFlags.Default.withSampled(sampled),
      traceState = TraceState.empty,
      remote = remote
    )

  protected def assertSuccess[A](result: Either[NonEmptyList[A], Unit]): Unit =
    assertEquals(result, Right(()))

  protected def assertFirstMismatch[A](result: Either[NonEmptyList[A], Unit]): A =
    result match {
      case Left(mismatches) => mismatches.head
      case Right(_)         => fail("expected mismatches, got success")
    }

  protected def buildSpan(
      testkit: TracesTestkit[IO],
      tracerName: String = "service",
      tracerVersion: Option[String] = None,
      spanName: String = "span",
      kind: SpanKind = SpanKind.Internal,
      parent: Option[SpanContext] = None,
      links: List[(SpanContext, Attributes)] = Nil,
      status: Option[(StatusCode, Option[String])] = None,
      addEvents: Span[IO] => IO[Unit] = _ => IO.unit
  ): IO[SpanData] = {
    for {
      tracer <- testkit.tracerProvider
        .tracer(tracerName)
        .pipe(builder => tracerVersion.fold(builder)(builder.withVersion))
        .get
      _ <- tracer
        .spanBuilder(spanName)
        .withSpanKind(kind)
        .pipe(builder => parent.fold(builder)(p => builder.withParent(p)))
        .pipe(builder => links.foldLeft(builder) { case (acc, (ctx, attributes)) => acc.addLink(ctx, attributes) })
        .build
        .use { span =>
          addEvents(span) >> status.fold(IO.unit) {
            case (code, Some(description)) => span.setStatus(code, description)
            case (code, None)              => span.setStatus(code)
          }
        }
      span <- exportedSingleSpan(testkit)
    } yield span
  }
}
