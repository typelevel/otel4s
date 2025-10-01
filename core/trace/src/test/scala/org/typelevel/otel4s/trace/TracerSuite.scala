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

package org.typelevel.otel4s
package trace

import cats.Applicative
import cats.effect.IO
import munit.CatsEffectSuite
import org.typelevel.otel4s.context.propagation.TextMapGetter
import org.typelevel.otel4s.context.propagation.TextMapUpdater
import org.typelevel.otel4s.trace.meta.InstrumentMeta

import scala.concurrent.duration._

class TracerSuite extends CatsEffectSuite {

  test("do not allocate attributes when instrument is noop") {
    val tracer = Tracer.noop[IO]

    var allocated = false

    def text = {
      allocated = true
      "text"
    }

    def status = {
      allocated = true
      StatusCode.Ok
    }

    def timestamp = {
      allocated = true
      100.millis
    }

    def attribute: List[Attribute[String]] = {
      allocated = true
      List(Attribute("key", "value"))
    }

    def exception = {
      allocated = true
      new RuntimeException("exception")
    }

    // test varargs and Iterable overloads
    for {
      _ <- tracer.span("span", attribute: _*).use { span =>
        for {
          _ <- span.addAttributes(attribute: _*)
          _ <- span.addAttributes(attribute)
          _ <- span.addEvent(text, attribute: _*)
          _ <- span.addEvent(text, attribute)
          _ <- span.addEvent(text, timestamp, attribute: _*)
          _ <- span.addEvent(text, timestamp, attribute)
          _ <- span.recordException(exception, attribute: _*)
          _ <- span.recordException(exception, attribute)
          _ <- span.setStatus(status)
          _ <- span.setStatus(status, text)
        } yield ()
      }
      _ <- tracer.span("span", attribute).use_
      _ <- tracer.rootSpan("span", attribute: _*).use { span =>
        for {
          _ <- span.addAttributes(attribute: _*)
          _ <- span.addAttributes(attribute)
          _ <- span.addEvent(text, attribute: _*)
          _ <- span.addEvent(text, attribute)
          _ <- span.addEvent(text, timestamp, attribute: _*)
          _ <- span.addEvent(text, timestamp, attribute)
          _ <- span.recordException(exception, attribute: _*)
          _ <- span.recordException(exception, attribute)
          _ <- span.setStatus(status)
          _ <- span.setStatus(status, text)
        } yield ()
      }
      _ <- tracer.rootSpan("span", attribute).use_
    } yield assert(!allocated)
  }

  test("eliminate 'addAttributes' when varargs are empty") {
    val tracer = new ProxyTracer(Tracer.noop[IO])
    val attribute = Attribute("key", "value")

    val expected = Vector(
      Vector(
        BuilderOp.Init("span"),
        BuilderOp.Build
      ),
      Vector(
        BuilderOp.Init("span-varargs"),
        BuilderOp.ModifyState(SpanBuilder.State.init.addAttribute(Attribute("key", "value"))),
        BuilderOp.Build
      ),
      Vector(
        BuilderOp.Init("root-span"),
        BuilderOp.Build
      ),
      Vector(
        BuilderOp.Init("root-span-varargs"),
        BuilderOp.ModifyState(SpanBuilder.State.init.addAttribute(Attribute("key", "value"))),
        BuilderOp.Build
      )
    )

    for {
      _ <- tracer.span("span").use_
      _ <- tracer.span("span-varargs", attribute).use_
      _ <- tracer.span("root-span").use_
      _ <- tracer.span("root-span-varargs", attribute).use_
    } yield assertEquals(tracer.builders.map(_.ops), expected)
  }

  test("`currentSpanOrNoop` is not valid when instrument is noop") {
    val tracer = Tracer.noop[IO]
    for (span <- tracer.currentSpanOrNoop)
      yield assert(!span.context.isValid)
  }

  // utility

  private sealed trait BuilderOp
  private object BuilderOp {
    case class Init(name: String) extends BuilderOp
    case class ModifyState(state: SpanBuilder.State) extends BuilderOp
    case object Build extends BuilderOp
  }

  private final class ProxyBuilder[F[_]: Applicative](
      name: String,
      var underlying: SpanBuilder[F]
  ) extends SpanBuilder.Unsealed[F] {
    private var state: SpanBuilder.State = SpanBuilder.State.init
    private val builderOps = Vector.newBuilder[BuilderOp]
    builderOps.addOne(BuilderOp.Init(name))

    def ops: Vector[BuilderOp] = builderOps.result()

    def meta: InstrumentMeta[F] = InstrumentMeta.enabled[F]

    def modifyState(f: SpanBuilder.State => SpanBuilder.State): SpanBuilder[F] = {
      state = f(state)
      underlying = underlying.modifyState(f)
      builderOps.addOne(BuilderOp.ModifyState(state))
      this
    }

    def build: SpanOps[F] = {
      builderOps.addOne(BuilderOp.Build)
      underlying.build
    }
  }

  private class ProxyTracer[F[_]: Applicative](underlying: Tracer[F]) extends Tracer.Unsealed[F] {
    private val proxyBuilders = Vector.newBuilder[ProxyBuilder[F]]

    def meta: InstrumentMeta[F] = InstrumentMeta.enabled[F]
    def currentSpanContext: F[Option[SpanContext]] = underlying.currentSpanContext
    def currentSpanOrNoop: F[Span[F]] = underlying.currentSpanOrNoop
    def currentSpanOrThrow: F[Span[F]] = underlying.currentSpanOrThrow
    def childScope[A](parent: SpanContext)(fa: F[A]): F[A] = underlying.childScope(parent)(fa)
    def joinOrRoot[A, C: TextMapGetter](carrier: C)(fa: F[A]): F[A] = underlying.joinOrRoot(carrier)(fa)
    def rootScope[A](fa: F[A]): F[A] = underlying.rootScope(fa)
    def noopScope[A](fa: F[A]): F[A] = underlying.noopScope(fa)
    def propagate[C: TextMapUpdater](carrier: C): F[C] = underlying.propagate(carrier)

    def spanBuilder(name: String): SpanBuilder[F] = {
      val builder = new ProxyBuilder[F](name, underlying.spanBuilder(name))
      proxyBuilders.addOne(builder)
      builder
    }

    def builders: Vector[ProxyBuilder[F]] =
      proxyBuilders.result()
  }
}
