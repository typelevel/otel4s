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

import cats.effect.Temporal
import org.typelevel.otel4s.logs.LogRecordBuilder
import org.typelevel.otel4s.logs.Logger
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.TraceContext
import org.typelevel.otel4s.sdk.logs.processor.LogRecordProcessor

/** SDK implementation of the [[Logger]].
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/logs/sdk/#logger]]
  */
private final class SdkLogger[F[_]: Temporal: AskContext](
    val meta: Logger.Meta[F, Context],
    instrumentationScope: InstrumentationScope,
    resource: TelemetryResource,
    traceContextLookup: TraceContext.Lookup,
    logRecordLimits: LogRecordLimits,
    processor: LogRecordProcessor[F]
) extends Logger.Unsealed[F, Context] {

  def logRecordBuilder: LogRecordBuilder[F, Context] =
    SdkLogRecordBuilder.empty(processor, instrumentationScope, resource, traceContextLookup, logRecordLimits)

}
