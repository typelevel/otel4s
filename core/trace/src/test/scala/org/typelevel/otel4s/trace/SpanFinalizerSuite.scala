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
import cats.effect.Resource
import cats.syntax.semigroup._
import munit.CatsEffectSuite
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes

import scala.collection.immutable
import scala.concurrent.duration._

class SpanFinalizerSuite extends CatsEffectSuite {
  import SpanFinalizerSuite._

  test("run: no-op when span is disabled") {
    for {
      ops <- IO.ref(Vector.empty[BackendOp])
      backend = new OpsBackend(ops, Span.Meta.disabled)
      finalizer = SpanFinalizer.addAttribute(Attribute("key", "value"))
      _ <- SpanFinalizer.run(backend, finalizer)
      result <- ops.get
    } yield assertEquals(result, Vector.empty)
  }

  test("run: applies finalizers in order") {
    val attr1 = Attribute("first", "value")
    val attr2 = Attribute("second", "value")
    val linkAttrs = List(Attribute("link", "value"))
    val spanContext = SpanContext.invalid

    val finalizer =
      SpanFinalizer.addAttribute(attr1) |+|
        SpanFinalizer.addEvent("event", attr2) |+|
        SpanFinalizer.addLink(spanContext, linkAttrs) |+|
        SpanFinalizer.updateName("updated") |+|
        SpanFinalizer.setStatus(StatusCode.Error, "boom")

    for {
      ops <- IO.ref(Vector.empty[BackendOp])
      backend = new OpsBackend(ops, Span.Meta.enabled)
      _ <- SpanFinalizer.run(backend, finalizer)
      result <- ops.get
    } yield assertEquals(
      result,
      Vector(
        BackendOp.AddAttributes(Attributes(attr1)),
        BackendOp.AddEvent("event", None, Attributes(attr2)),
        BackendOp.AddLink(spanContext, Attributes.fromSpecific(linkAttrs)),
        BackendOp.UpdateName("updated"),
        BackendOp.SetStatus(StatusCode.Error, Some("boom"))
      )
    )
  }

  test("run: uses explicit event timestamp") {
    val timestamp = 123.millis
    val attributes = List(Attribute("key", "value"))

    val finalizer = SpanFinalizer.addEvent("event", timestamp, attributes)

    for {
      ops <- IO.ref(Vector.empty[BackendOp])
      backend = new OpsBackend(ops, Span.Meta.enabled)
      _ <- SpanFinalizer.run(backend, finalizer)
      result <- ops.get
    } yield assertEquals(
      result,
      Vector(BackendOp.AddEvent("event", Some(timestamp), Attributes.fromSpecific(attributes)))
    )
  }

  test("strategy.reportAbnormal: errored") {
    val error = new RuntimeException("boom")
    val strategy = SpanFinalizer.Strategy.reportAbnormal

    for {
      ops <- IO.ref(Vector.empty[BackendOp])
      backend = new OpsBackend(ops, Span.Meta.enabled)
      _ <- SpanFinalizer.run(backend, strategy(Resource.ExitCase.Errored(error)))
      result <- ops.get
    } yield assertEquals(
      result,
      Vector(
        BackendOp.RecordException(error, Attributes.empty),
        BackendOp.SetStatus(StatusCode.Error, None)
      )
    )
  }

  test("strategy.reportAbnormal: canceled") {
    val strategy = SpanFinalizer.Strategy.reportAbnormal

    for {
      ops <- IO.ref(Vector.empty[BackendOp])
      backend = new OpsBackend(ops, Span.Meta.enabled)
      _ <- SpanFinalizer.run(backend, strategy(Resource.ExitCase.Canceled))
      result <- ops.get
    } yield assertEquals(
      result,
      Vector(BackendOp.SetStatus(StatusCode.Error, Some("canceled")))
    )
  }

  test("strategy.reportAbnormal: succeeded is ignored") {
    assert(!SpanFinalizer.Strategy.reportAbnormal.isDefinedAt(Resource.ExitCase.Succeeded))
  }

}

object SpanFinalizerSuite {

  private sealed trait BackendOp
  private object BackendOp {
    final case class UpdateName(name: String) extends BackendOp
    final case class AddAttributes(attributes: Attributes) extends BackendOp
    final case class AddEvent(
        name: String,
        timestamp: Option[FiniteDuration],
        attributes: Attributes
    ) extends BackendOp
    final case class AddLink(spanContext: SpanContext, attributes: Attributes) extends BackendOp
    final case class RecordException(exception: Throwable, attributes: Attributes) extends BackendOp
    final case class SetStatus(status: StatusCode, description: Option[String]) extends BackendOp
  }

  private class OpsBackend(state: Ref[IO, Vector[BackendOp]], m: Span.Meta[IO]) extends Span.Backend.Unsealed[IO] {
    import BackendOp._

    val meta: Span.Meta[IO] = m

    def context: SpanContext = SpanContext.invalid

    def updateName(name: String): IO[Unit] =
      state.update(_ :+ UpdateName(name))

    def addAttributes(attributes: immutable.Iterable[Attribute[_]]): IO[Unit] =
      state.update(_ :+ AddAttributes(Attributes.fromSpecific(attributes)))

    def addEvent(name: String, attributes: immutable.Iterable[Attribute[_]]): IO[Unit] =
      state.update(_ :+ AddEvent(name, None, Attributes.fromSpecific(attributes)))

    def addEvent(name: String, timestamp: FiniteDuration, attributes: immutable.Iterable[Attribute[_]]): IO[Unit] =
      state.update(_ :+ AddEvent(name, Some(timestamp), Attributes.fromSpecific(attributes)))

    def addLink(spanContext: SpanContext, attributes: immutable.Iterable[Attribute[_]]): IO[Unit] =
      state.update(_ :+ AddLink(spanContext, Attributes.fromSpecific(attributes)))

    def recordException(exception: Throwable, attributes: immutable.Iterable[Attribute[_]]): IO[Unit] =
      state.update(_ :+ RecordException(exception, Attributes.fromSpecific(attributes)))

    def setStatus(status: StatusCode): IO[Unit] =
      state.update(_ :+ SetStatus(status, None))

    def setStatus(status: StatusCode, description: String): IO[Unit] =
      state.update(_ :+ SetStatus(status, Some(description)))

    def end: IO[Unit] =
      IO.unit

    def end(timestamp: FiniteDuration): IO[Unit] =
      IO.unit
  }

}
