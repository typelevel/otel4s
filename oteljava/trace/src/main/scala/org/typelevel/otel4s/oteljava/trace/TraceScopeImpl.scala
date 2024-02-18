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

package org.typelevel.otel4s.oteljava.trace

import io.opentelemetry.api.trace.{Span => JSpan}
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.context.LocalContext
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.TraceScope

private object TraceScopeImpl {

  def fromLocal[F[_]: LocalContext]: TraceScope[F, Context] = {
    def getSpanContext(context: Context): Option[SpanContext] =
      Option(JSpan.fromContextOrNull(context.underlying))
        .map(jSpan => SpanContextConversions.toScala(jSpan.getSpanContext))

    def setSpanContext(
        context: Context,
        spanContext: SpanContext
    ): Context =
      context.map { ctx =>
        JSpan
          .wrap(SpanContextConversions.toJava(spanContext))
          .storeInContext(ctx)
      }

    TraceScope.fromLocal(getSpanContext, setSpanContext)
  }
}
