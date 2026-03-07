/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s.trace

import cats.effect.IO
import cats.effect.Ref
import munit.CatsEffectSuite
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes

import scala.collection.immutable
import scala.concurrent.duration.*
import scala.concurrent.duration.FiniteDuration

class SpanMacroSuite extends CatsEffectSuite {
  import SpanMacroSuite._

  test("Span macro accepts mixed Scala 3 varargs and normalizes attributes") {
    val a1 = Attribute("k1", "v1")
    val a2 = Attribute("k2", "v2")
    val a3 = Attribute("k3", "v3")
    val mixed = List(a2, a3)
    val ctx = SpanContext.invalid
    val ex = new RuntimeException("boom")
    val ts = 100.millis

    val expected = Vector(
      BackendOp.AddAttributes(Attributes(a1, a2, a3)),
      BackendOp.AddEvent("event", None, Attributes(a1, a2, a3)),
      BackendOp.AddEvent("event-ts", Some(ts), Attributes(a1, a2, a3)),
      BackendOp.AddLink(ctx, Attributes(a1, a2, a3)),
      BackendOp.RecordException(ex, Attributes(a1, a2, a3))
    )

    for {
      ops <- IO.ref(Vector.empty[BackendOp])
      span = Span.fromBackend(new OpsBackend(ops))
      _ <- span.addAttributes(a1, mixed)
      _ <- span.addEvent("event", a1, mixed)
      _ <- span.addEvent("event-ts", ts, a1, mixed)
      _ <- span.addLink(ctx, a1, mixed)
      _ <- span.recordException(ex, a1, mixed)
      result <- ops.get
    } yield assertEquals(result, expected)
  }
}

object SpanMacroSuite {

  private sealed trait BackendOp
  private object BackendOp {
    final case class AddAttributes(attributes: Attributes) extends BackendOp
    final case class AddEvent(
        name: String,
        timestamp: Option[FiniteDuration],
        attributes: Attributes
    ) extends BackendOp
    final case class AddLink(
        spanContext: SpanContext,
        attributes: Attributes
    ) extends BackendOp
    final case class RecordException(
        exception: Throwable,
        attributes: Attributes
    ) extends BackendOp
  }

  private class OpsBackend(state: Ref[IO, Vector[BackendOp]]) extends Span.Backend.Unsealed[IO] {
    import BackendOp._

    val meta: Span.Meta[IO] = Span.Meta.enabled

    def context: SpanContext = SpanContext.invalid
    def isRecording: IO[Boolean] = IO.pure(true)
    def updateName(name: String): IO[Unit] = IO.unit
    def setStatus(status: StatusCode): IO[Unit] = IO.unit
    def setStatus(status: StatusCode, description: String): IO[Unit] = IO.unit
    def end: IO[Unit] = IO.unit
    def end(timestamp: FiniteDuration): IO[Unit] = IO.unit

    def addAttributes(attributes: immutable.Iterable[Attribute[_]]): IO[Unit] =
      state.update(_ :+ AddAttributes(attributes.to(Attributes)))

    def addEvent(name: String, attributes: immutable.Iterable[Attribute[_]]): IO[Unit] =
      state.update(_ :+ AddEvent(name, None, attributes.to(Attributes)))

    def addEvent(name: String, timestamp: FiniteDuration, attributes: immutable.Iterable[Attribute[_]]): IO[Unit] =
      state.update(_ :+ AddEvent(name, Some(timestamp), attributes.to(Attributes)))

    def addLink(spanContext: SpanContext, attributes: immutable.Iterable[Attribute[_]]): IO[Unit] =
      state.update(_ :+ AddLink(spanContext, attributes.to(Attributes)))

    def recordException(exception: Throwable, attributes: immutable.Iterable[Attribute[_]]): IO[Unit] =
      state.update(_ :+ RecordException(exception, attributes.to(Attributes)))
  }
}
