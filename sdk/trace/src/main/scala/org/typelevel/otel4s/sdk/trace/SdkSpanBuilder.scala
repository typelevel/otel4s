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

import cats.Applicative
import cats.arrow.FunctionK
import cats.effect.Concurrent
import cats.effect.Resource
import cats.effect.Temporal
import cats.effect.std.Console
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import cats.syntax.semigroup._
import cats.~>
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.sdk.Attributes
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanBuilder
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanFinalizer
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.SpanOps
import org.typelevel.otel4s.trace.TraceFlags
import org.typelevel.otel4s.trace.TraceState

import scala.concurrent.duration.FiniteDuration

private[trace] final case class SdkSpanBuilder[F[_]: Temporal: Console](
    name: String,
    scopeInfo: InstrumentationScope,
    tracerSharedState: TracerSharedState[F],
    scope: SdkTraceScope[F],
    parent: SdkSpanBuilder.Parent = SdkSpanBuilder.Parent.Propagate,
    finalizationStrategy: SpanFinalizer.Strategy =
      SpanFinalizer.Strategy.reportAbnormal,
    kind: Option[SpanKind] = None,
    links: Vector[LinkData] = Vector.empty,
    attributes: Seq[Attribute[_]] = Nil,
    startTimestamp: Option[FiniteDuration] = None
) extends SpanBuilder[F] {
  import SdkSpanBuilder._

  def withSpanKind(spanKind: SpanKind): SpanBuilder[F] =
    copy(kind = Some(spanKind))

  def addAttribute[A](attribute: Attribute[A]): SpanBuilder[F] =
    copy(attributes = attributes :+ attribute)

  def addAttributes(attributes: Attribute[_]*): SpanBuilder[F] =
    copy(attributes = this.attributes ++ attributes)

  def addLink(
      spanContext: SpanContext,
      attributes: Attribute[_]*
  ): SpanBuilder[F] =
    copy(links = links :+ LinkData(spanContext, Attributes(attributes: _*)))

  def root: SpanBuilder[F] =
    copy(parent = Parent.Root)

  def withParent(parent: SpanContext): SpanBuilder[F] =
    copy(parent = Parent.Explicit(parent))

  def withStartTimestamp(timestamp: FiniteDuration): SpanBuilder[F] =
    copy(startTimestamp = Some(timestamp))

  def withFinalizationStrategy(
      strategy: SpanFinalizer.Strategy
  ): SpanBuilder[F] =
    copy(finalizationStrategy = strategy)

  def build: SpanOps[F] = new SpanOps[F] {
    def startUnmanaged: F[Span[F]] =
      start.map(backend => Span.fromBackend(backend))

    def resource: Resource[F, SpanOps.Res[F]] =
      startAsRes

    def use[A](f: Span[F] => F[A]): F[A] =
      resource.use(res => res.trace(f(res.span)))

    def use_ : F[Unit] =
      use(_ => Applicative[F].unit)
  }

  private def startAsRes: Resource[F, SpanOps.Res[F]] =
    Resource.eval(scope.current).flatMap {
      case spanContext if spanContext.forall(_.isValid) =>
        startManaged.map { case (back, nt) =>
          SpanOps.Res(Span.fromBackend(back), nt)
        }

      case _ =>
        Resource.pure(
          SpanOps.Res(Span.fromBackend(Span.Backend.noop), FunctionK.id)
        )
    }

  private def startManaged: Resource[F, (Span.Backend[F], F ~> F)] = {
    def acquire: F[Span.Backend[F]] =
      start

    def release(backend: Span.Backend[F], ec: Resource.ExitCase): F[Unit] =
      for {
        _ <- finalizationStrategy
          .lift(ec)
          .foldMapM(SpanFinalizer.run(backend, _))
        _ <- backend.end
      } yield ()

    for {
      backend <- Resource.makeCase(acquire) { case (b, ec) => release(b, ec) }
      nt <- Resource.eval(scope.makeScope(backend.context))
    } yield (backend, nt)
  }

  private def chooseParentSpanContext: F[Option[SpanContext]] =
    parent match {
      case Parent.Root             => Concurrent[F].pure(None)
      case Parent.Propagate        => scope.current
      case Parent.Explicit(parent) => Concurrent[F].pure(Some(parent))
    }

  private[trace] def start: F[Span.Backend[F]] = {
    val idGenerator = tracerSharedState.idGenerator

    for {
      parentSpanContext <- chooseParentSpanContext

      spanId <- idGenerator.generateSpanId

      traceId <-
        parentSpanContext
          .filter(_.isValid)
          .fold(idGenerator.generateTraceId) { spanContext =>
            Concurrent[F].pure(spanContext.traceId)
          }

      backend <- {
        val samplingResult =
          tracerSharedState.sampler.shouldSample(parentSpanContext, traceId)

        val samplingDecision = samplingResult.decision

        val traceFlags =
          if (samplingDecision.isSampled) TraceFlags.Sampled
          else TraceFlags.Default

        val traceState = parentSpanContext.fold(TraceState.empty) { ctx =>
          samplingResult.traceStateUpdater.update(ctx.traceState)
        }

        val spanContext = {
          if (tracerSharedState.idGenerator.canSkipIdValidation) {
            SpanContext.createInternal(
              traceId = traceId,
              spanId = spanId,
              traceFlags = traceFlags,
              traceState = traceState,
              remote = false,
              isValid = true
            )
          } else {
            SpanContext(
              traceId = traceId,
              spanId = spanId,
              traceFlags = traceFlags,
              traceState = traceState,
              remote = false
            )
          }
        }

        if (!samplingDecision.isRecording) {
          Applicative[F].pure(Span.Backend.propagating(spanContext))
        } else {
          val samplingAttributes = samplingResult.attributes

          val recordedAttributes =
            Attributes.fromSpecific(attributes) |+| samplingAttributes

          SdkSpanBackend.start[F](
            context = spanContext,
            name = name,
            scopeInfo = scopeInfo,
            resource = tracerSharedState.resource,
            kind = kind.getOrElse(SpanKind.Internal),
            parentContext = parentSpanContext,
            spanLimits = tracerSharedState.spanLimits,
            processor = tracerSharedState.activeSpanProcessor,
            attributes = recordedAttributes,
            links = links,
            totalRecordedLinks = links.size,
            userStartTimestamp = startTimestamp
          )
        }
      }
    } yield backend

  }
}

private[trace] object SdkSpanBuilder {

  sealed trait Parent
  object Parent {
    case object Propagate extends Parent
    case object Root extends Parent
    final case class Explicit(parent: SpanContext) extends Parent
  }

}
