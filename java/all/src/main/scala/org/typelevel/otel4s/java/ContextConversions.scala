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
import org.typelevel.otel4s.java.trace.WrappedSpanContext
import org.typelevel.vault.Vault

private[otel4s] object ContextConversions {

  def toJContext(context: Vault): JContext = {
    var jContext = JContext.root()
    jContext = Scope.fromContext(context) match {
      case Scope.Root(_) =>
        jContext
      case Scope.Span(_, jSpan, _) =>
        jSpan.storeInContext(jContext)
      case Scope.Noop =>
        JSpan.getInvalid.storeInContext(jContext)
    }
    jContext
  }

  def fromJContext(jContext: JContext): Vault = {
    var context = Vault.empty
    context = (JSpan.fromContextOrNull(jContext) match {
      case null =>
        Scope.root
      case jSpan =>
        if (jSpan.getSpanContext.isValid)
          Scope.Span(
            jContext,
            jSpan,
            new WrappedSpanContext(jSpan.getSpanContext)
          )
        else
          Scope.Noop
    }).storeInContext(context)
    context
  }
}
