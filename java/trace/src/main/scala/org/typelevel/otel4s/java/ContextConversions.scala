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

import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.context.{Context => JContext}
import org.typelevel.otel4s.java.trace.Scope
import org.typelevel.otel4s.java.trace.SpanConversions
import org.typelevel.vault.Vault

private[otel4s] object ContextConversions {

  def toJContext(context: Vault): JContext =
    Scope.fromContext(context) match {
      case Scope.Root(ctx) =>
        ctx
      case Scope.Span(ctx, jSpan) =>
        jSpan.storeInContext(ctx)
      case Scope.Noop =>
        JSpan.getInvalid.storeInContext(JContext.root())
    }

  def fromJContext(jContext: JContext): Vault =
    JSpan.fromContextOrNull(jContext) match {
      case null =>
        Scope.Root(jContext).storeInContext(Vault.empty)
      case jSpan =>
        if (jSpan.getSpanContext.isValid) {
          var context = Vault.empty
          context = Scope.Span(jContext, jSpan).storeInContext(context)
          new WrappedSpanContext(jSpan.getSpanContext).storeInContext(context)
        } else {
          Scope.Noop.storeInContext(Vault.empty)
        }
    }
}
