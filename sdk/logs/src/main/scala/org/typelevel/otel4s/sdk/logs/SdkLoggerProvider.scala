/*
 * Copyright 2025 Typelevel
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

package org.typelevel.otel4s.sdk.logs

import cats.Applicative
import cats.Monad
import cats.Parallel
import cats.effect.Temporal
import cats.effect.std.Console
import cats.syntax.functor._
import org.typelevel.otel4s.logs.LoggerBuilder
import org.typelevel.otel4s.logs.LoggerProvider
import org.typelevel.otel4s.logs.meta.InstrumentMeta
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.TraceContext
import org.typelevel.otel4s.sdk.internal.ComponentRegistry
import org.typelevel.otel4s.sdk.logs.processor.LogRecordProcessor

/** SDK implementation of the [[LoggerProvider]].
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/logs/sdk/#loggerprovider]]
  */
private final class SdkLoggerProvider[F[_]: Applicative](
    componentRegistry: ComponentRegistry[F, SdkLogger[F]],
    resource: TelemetryResource,
    limits: LogRecordLimits,
    processor: LogRecordProcessor[F]
) extends LoggerProvider.Unsealed[F, Context] {
  import SdkLoggerProvider.DefaultLoggerName

  def logger(name: String): LoggerBuilder[F, Context] = {
    val loggerName = if (name.trim.isEmpty) DefaultLoggerName else name
    SdkLoggerBuilder(componentRegistry, loggerName)
  }

  override def toString: String =
    s"SdkLoggerProvider{resource=$resource, logRecordLimits=$limits, logRecordProcessor=$processor}"
}

object SdkLoggerProvider {

  private val DefaultLoggerName = "unknown"

  /** Builder for [[org.typelevel.otel4s.logs.LoggerProvider LoggerProvider]].
    */
  sealed trait Builder[F[_]] {

    /** Sets a [[TelemetryResource]] to be attached to all logs created by [[org.typelevel.otel4s.logs.Logger Logger]].
      *
      * @note
      *   on multiple subsequent calls, the resource from the last call will be retained.
      *
      * @param resource
      *   the [[TelemetryResource]] to use
      */
    def withResource(resource: TelemetryResource): Builder[F]

    /** Merges the given [[TelemetryResource]] with the current one.
      *
      * @note
      *   if both resources have different non-empty `schemaUrl`, the merge will fail.
      *
      * @see
      *   [[TelemetryResource.mergeUnsafe]]
      *
      * @param resource
      *   the [[TelemetryResource]] to merge the current one with
      */
    def addResource(resource: TelemetryResource): Builder[F]

    /** Sets the [[LogRecordLimits]] to be used by the logger provider.
      *
      * @note
      *   on multiple subsequent calls, the limits from the last call will be retained.
      *
      * @param limits
      *   the [[LogRecordLimits]] to use
      */
    def withLogRecordLimits(limits: LogRecordLimits): Builder[F]

    /** Sets a [[org.typelevel.otel4s.sdk.context.TraceContext.Lookup TraceContext.Lookup]] to be used by exemplars.
      *
      * @param lookup
      *   the [[org.typelevel.otel4s.sdk.context.TraceContext.Lookup TraceContext.Lookup]] to use
      */
    private[sdk] def withTraceContextLookup(lookup: TraceContext.Lookup): Builder[F]

    /** Adds a [[org.typelevel.otel4s.sdk.logs.processor.LogRecordProcessor LogRecordProcessor]] to the log record
      * processing pipeline that will be built.
      *
      * The processor will be called each time a [[org.typelevel.otel4s.sdk.logs.LogRecordRef LogRecordRef]] is emitted.
      *
      * @note
      *   the span processor must be thread-safe and return immediately (no remote calls, as contention free as
      *   possible).
      *
      * @param processor
      *   the processor to add
      */
    def addLogRecordProcessor(processor: LogRecordProcessor[F]): Builder[F]

    /** Creates [[org.typelevel.otel4s.logs.LoggerProvider LoggerProvider]] with the configuration of this builder.
      */
    def build: F[LoggerProvider[F, Context]]
  }

  /** Creates a new [[Builder]] with default configuration.
    */
  def builder[F[_]: Temporal: Parallel: AskContext: Console]: Builder[F] =
    BuilderImpl(
      resource = TelemetryResource.default,
      traceContextLookup = TraceContext.Lookup.noop,
      logRecordLimits = LogRecordLimits.default,
      logRecordProcessors = Nil
    )

  private final case class BuilderImpl[F[_]: Temporal: Parallel: AskContext: Console](
      resource: TelemetryResource,
      traceContextLookup: TraceContext.Lookup,
      logRecordLimits: LogRecordLimits,
      logRecordProcessors: List[LogRecordProcessor[F]]
  ) extends Builder[F] {

    def withResource(resource: TelemetryResource): Builder[F] =
      copy(resource = resource)

    def addResource(resource: TelemetryResource): Builder[F] =
      copy(resource = this.resource.mergeUnsafe(resource))

    def withLogRecordLimits(limits: LogRecordLimits): Builder[F] =
      copy(logRecordLimits = limits)

    def addLogRecordProcessor(processor: LogRecordProcessor[F]): Builder[F] =
      copy(logRecordProcessors = logRecordProcessors :+ processor)

    private[sdk] def withTraceContextLookup(traceContextLookup: TraceContext.Lookup): Builder[F] =
      copy(traceContextLookup = traceContextLookup)

    def build: F[LoggerProvider[F, Context]] =
      if (logRecordProcessors.isEmpty) Temporal[F].pure(LoggerProvider.noop)
      else create

    private def create: F[LoggerProvider[F, Context]] = {
      val processor = LogRecordProcessor.of(logRecordProcessors: _*)

      def createLogger(scope: InstrumentationScope): F[SdkLogger[F]] =
        Monad[F].pure(
          new SdkLogger[F](
            meta = InstrumentMeta.enabled,
            instrumentationScope = scope,
            resource = resource,
            traceContextLookup = traceContextLookup,
            logRecordLimits = logRecordLimits,
            processor = processor
          )
        )

      for {
        registry <- ComponentRegistry.create(createLogger)
      } yield new SdkLoggerProvider(registry, resource, logRecordLimits, processor)
    }
  }
}
