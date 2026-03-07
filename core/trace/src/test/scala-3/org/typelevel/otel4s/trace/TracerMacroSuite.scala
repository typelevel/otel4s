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

class TracerMacroSuite extends CatsEffectSuite {
  import TracerMacroSuite._

  test("span and rootSpan support mixed Scala 3 varargs") {
    val a1 = Attribute("k1", "v1")
    val a2 = Attribute("k2", "v2")
    val a3 = Attribute("k3", "v3")
    val mixed = List(a2, a3)
    val tracer = new ProxyTracer(Tracer.noop[IO])

    val expected = Vector(
      Vector(
        BuilderOp.Init("span"),
        BuilderOp.ModifyState(SpanBuilder.State.init.addAttributes(List(a1, a2, a3))),
        BuilderOp.Build
      ),
      Vector(
        BuilderOp.Init("root"),
        BuilderOp.ModifyState(SpanBuilder.State.init.addAttributes(List(a1, a2, a3))),
        BuilderOp.ModifyState(
          SpanBuilder.State.init.addAttributes(List(a1, a2, a3)).withParent(SpanBuilder.Parent.root)
        ),
        BuilderOp.Build
      )
    )

    for {
      _ <- tracer.span("span", a1, mixed).use_
      _ <- tracer.rootSpan("root", a1, mixed).use_
    } yield assertEquals(tracer.builders.map(_.ops), expected)
  }

}

object TracerMacroSuite {

  private sealed trait BuilderOp
  private object BuilderOp {
    final case class Init(name: String) extends BuilderOp
    final case class ModifyState(state: SpanBuilder.State) extends BuilderOp
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
    def withCurrentSpanOrNoop[A](f: Span[F] => F[A]): F[A] = underlying.withCurrentSpanOrNoop(f)
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
