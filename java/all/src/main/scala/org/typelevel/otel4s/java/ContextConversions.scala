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

package org.typelevel.otel4s.java

import cats.effect.Sync
import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.context.{Context => JContext}
import org.typelevel.otel4s.java.trace.SpanBackendImpl
import org.typelevel.otel4s.java.trace.WrappedSpanContext
import org.typelevel.otel4s.trace.Span
import org.typelevel.vault.Vault

private[otel4s] object ContextConversions {

  def toJContext[F[_]](context: Vault): JContext = {
    var jContext = JContext.root()
    Span.fromContext(context).foreach { (span: Span[F]) =>
      span.backend match {
        case impl: SpanBackendImpl[F] =>
          jContext = impl.jSpan.storeInContext(jContext)
      }
    }
    jContext
  }

  def fromJContext[F[_]: Sync](jContext: JContext): Vault = {
    var context = Vault.empty
    JSpan.fromContextOrNull(jContext) match {
      case null =>
        ()
      case jSpan =>
        context = Span
          .fromBackend(
            new SpanBackendImpl[F](
              jSpan,
              new WrappedSpanContext(jSpan.getSpanContext)
            )
          )
          .storeInContext(context)
    }
    context
  }
}
