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
package java

import io.opentelemetry.context.{Context => JContext}
import io.opentelemetry.context.propagation.{
  ContextPropagators => JContextPropagators
}
import org.typelevel.vault.Vault

private[java] class ContextPropagatorsImpl(
    propagators: JContextPropagators,
    toJContext: Vault => JContext,
    fromJContext: JContext => Vault
) extends ContextPropagators[Vault] {
  val textMapPropagator: TextMapPropagator[Vault] =
    new TextMapPropagatorImpl(
      propagators.getTextMapPropagator,
      toJContext,
      fromJContext
    )
}
