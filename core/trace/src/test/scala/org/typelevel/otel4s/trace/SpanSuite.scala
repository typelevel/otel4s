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
import munit._
import org.typelevel.otel4s.Attribute

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration

class SpanSuite extends CatsEffectSuite {
  import SpanSuite._

  test("addAttributes: eliminate empty varargs calls") {
    for {
      ops <- IO.ref(Vector.empty[BackendOp])
      span = Span.fromBackend(new OpsBackend(ops))
      _ <- span.addAttributes()
      result <- ops.get
    } yield assertEquals(result, Vector.empty)
  }

}

object SpanSuite {

  private sealed trait BackendOp
  private object BackendOp {
    final case class UpdateName(name: String) extends BackendOp
    final case class AddAttributes(attributes: immutable.Iterable[Attribute[_]]) extends BackendOp
    final case class AddEvent(
        name: String,
        timestamp: Option[FiniteDuration],
        attributes: immutable.Iterable[Attribute[_]]
    ) extends BackendOp
    final case class AddLink(
        spanContext: SpanContext,
        attributes: immutable.Iterable[Attribute[_]]
    ) extends BackendOp
    final case class RecordException(
        exception: Throwable,
        attributes: immutable.Iterable[Attribute[_]]
    ) extends BackendOp
    final case class SetStatus(status: StatusCode, description: Option[String]) extends BackendOp
    final case class End(timestamp: Option[FiniteDuration]) extends BackendOp
  }

  private class OpsBackend(state: Ref[IO, Vector[BackendOp]]) extends Span.Backend.Unsealed[IO] {
    import BackendOp._

    val meta: Span.Meta[IO] = Span.Meta.enabled

    def context: SpanContext = ???

    def updateName(name: String): IO[Unit] =
      state.update(_ :+ UpdateName(name))

    def addAttributes(attributes: immutable.Iterable[Attribute[_]]): IO[Unit] =
      state.update(_ :+ AddAttributes(attributes))

    def addEvent(name: String, attributes: immutable.Iterable[Attribute[_]]): IO[Unit] =
      state.update(_ :+ AddEvent(name, None, attributes))

    def addEvent(name: String, timestamp: FiniteDuration, attributes: immutable.Iterable[Attribute[_]]): IO[Unit] =
      state.update(_ :+ AddEvent(name, Some(timestamp), attributes))

    def addLink(spanContext: SpanContext, attributes: immutable.Iterable[Attribute[_]]): IO[Unit] =
      state.update(_ :+ AddLink(spanContext, attributes))

    def recordException(exception: Throwable, attributes: immutable.Iterable[Attribute[_]]): IO[Unit] =
      state.update(_ :+ RecordException(exception, attributes))

    def setStatus(status: StatusCode): IO[Unit] =
      state.update(_ :+ SetStatus(status, None))

    def setStatus(status: StatusCode, description: String): IO[Unit] =
      state.update(_ :+ SetStatus(status, Some(description)))

    def end: IO[Unit] =
      state.update(_ :+ End(None))

    def end(timestamp: FiniteDuration): IO[Unit] =
      state.update(_ :+ End(Some(timestamp)))

  }

}
