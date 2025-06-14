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

package org.typelevel.otel4s
package sdk
package trace

import cats.arrow.FunctionK
import cats.effect.Resource
import cats.effect.Temporal
import cats.effect.std.Console
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import cats.~>
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.data.LimitedData
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.samplers.SamplingResult
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanBuilder
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanFinalizer
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.SpanOps
import org.typelevel.otel4s.trace.TraceFlags
import org.typelevel.otel4s.trace.TraceScope
import org.typelevel.otel4s.trace.TraceState
import scodec.bits.ByteVector

private final case class SdkSpanBuilder[F[_]: Temporal: Console] private (
    name: String,
    scopeInfo: InstrumentationScope,
    tracerSharedState: TracerSharedState[F],
    scope: TraceScope[F, Context],
    stateModifier: SpanBuilder.State => SpanBuilder.State
) extends SpanBuilder[F] {
  import SpanBuilder.Parent

  def meta: InstrumentMeta[F] = tracerSharedState.meta

  def modifyState(f: SpanBuilder.State => SpanBuilder.State): SpanBuilder[F] =
    copy(stateModifier = this.stateModifier.andThen(f))

  def build: SpanOps[F] = new SpanOps[F] {
    def startUnmanaged: F[Span[F]] =
      start(mkState).map(backend => Span.fromBackend(backend))

    def resource: Resource[F, SpanOps.Res[F]] =
      startAsRes

    def use[A](f: Span[F] => F[A]): F[A] =
      resource.use(res => res.trace(f(res.span)))

    def use_ : F[Unit] =
      use(_ => Temporal[F].unit)
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
    val state = mkState

    def acquire: F[Span.Backend[F]] =
      start(state)

    def release(backend: Span.Backend[F], ec: Resource.ExitCase): F[Unit] =
      for {
        _ <- state.finalizationStrategy.lift(ec).foldMapM(SpanFinalizer.run(backend, _))
        _ <- backend.end
      } yield ()

    for {
      backend <- Resource.makeCase(acquire) { case (b, ec) => release(b, ec) }
      nt <- Resource.eval(scope.childScope(backend.context))
    } yield (backend, nt)
  }

  private def start(state: SpanBuilder.State): F[Span.Backend[F]] = {
    val idGenerator = tracerSharedState.idGenerator
    val spanKind = state.spanKind.getOrElse(SpanKind.Internal)

    val attributes = LimitedData
      .attributes(
        tracerSharedState.spanLimits.maxNumberOfAttributes,
        tracerSharedState.spanLimits.maxAttributeValueLength
      )
      .appendAll(state.attributes)

    val links = {
      val linkAttributeLimits = LimitedData.attributes(
        tracerSharedState.spanLimits.maxNumberOfAttributesPerLink,
        tracerSharedState.spanLimits.maxAttributeValueLength
      )

      val links = state.links.map { case (ctx, attributes) =>
        LinkData(ctx, linkAttributeLimits.appendAll(attributes))
      }

      LimitedData
        .vector[LinkData](tracerSharedState.spanLimits.maxNumberOfLinks)
        .appendAll(links)
    }

    def genTraceId(parent: Option[SpanContext]): F[ByteVector] =
      parent
        .filter(_.isValid)
        .fold(idGenerator.generateTraceId)(ctx => Temporal[F].pure(ctx.traceId))

    def sample(parent: Option[SpanContext], traceId: ByteVector): F[SamplingResult] =
      tracerSharedState.sampler.shouldSample(
        parentContext = parent,
        traceId = traceId,
        name = name,
        spanKind = spanKind,
        attributes = attributes.elements,
        parentLinks = links.elements
      )

    for {
      parentSpanContext <- chooseParentSpanContext(state)
      spanId <- idGenerator.generateSpanId
      traceId <- genTraceId(parentSpanContext)

      samplingResult <- sample(parentSpanContext, traceId)
      backend <- {
        val samplingDecision = samplingResult.decision

        val traceFlags =
          if (samplingDecision.isSampled) TraceFlags.Sampled
          else TraceFlags.Default

        val traceState =
          parentSpanContext.fold(TraceState.empty) { ctx =>
            samplingResult.traceStateUpdater.update(ctx.traceState)
          }

        val spanContext =
          createSpanContext(traceId, spanId, traceFlags, traceState)

        if (!samplingDecision.isRecording) {
          Temporal[F].pure(Span.Backend.propagating(spanContext))
        } else {
          SdkSpanBackend
            .start[F](
              context = spanContext,
              name = name,
              scopeInfo = scopeInfo,
              resource = tracerSharedState.resource,
              kind = spanKind,
              parentContext = parentSpanContext,
              spanLimits = tracerSharedState.spanLimits,
              processor = tracerSharedState.spanProcessor,
              spanStorage = tracerSharedState.spanStorage,
              attributes = attributes.appendAll(samplingResult.attributes),
              links = links,
              userStartTimestamp = state.startTimestamp
            )
            .widen
        }
      }
    } yield backend
  }

  private def chooseParentSpanContext(state: SpanBuilder.State): F[Option[SpanContext]] =
    state.parent match {
      case Parent.Root             => Temporal[F].pure(None)
      case Parent.Propagate        => scope.current
      case Parent.Explicit(parent) => Temporal[F].pure(Some(parent))
    }

  private def createSpanContext(
      traceId: ByteVector,
      spanId: ByteVector,
      flags: TraceFlags,
      state: TraceState
  ): SpanContext =
    if (tracerSharedState.idGenerator.canSkipIdValidation) {
      SpanContext.createInternal(
        traceId,
        spanId,
        flags,
        state,
        remote = false,
        isValid = true
      )
    } else {
      SpanContext(traceId, spanId, flags, state, remote = false)
    }

  private[sdk] def mkState: SpanBuilder.State =
    stateModifier(SpanBuilder.State.init)

}

private object SdkSpanBuilder {

  def apply[F[_]: Temporal: Console](
      name: String,
      scopeInfo: InstrumentationScope,
      tracerSharedState: TracerSharedState[F],
      scope: TraceScope[F, Context]
  ): SdkSpanBuilder[F] =
    SdkSpanBuilder(
      name,
      scopeInfo,
      tracerSharedState,
      scope,
      identity
    )

}
