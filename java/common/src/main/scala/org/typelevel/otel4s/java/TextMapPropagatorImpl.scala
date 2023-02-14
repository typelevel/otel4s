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

import cats.effect.Sync
import io.opentelemetry.context.{Context => JContext}
import io.opentelemetry.context.propagation.{
  TextMapPropagator => JTextMapPropagator
}
import org.typelevel.otel4s.java.Conversions._
import org.typelevel.vault.Vault

private[java] class TextMapPropagatorImpl[F[_]: Sync](
    jPropagator: JTextMapPropagator,
    toJContext: Vault => JContext,
    fromJContext: JContext => Vault
) extends TextMapPropagator[F] {
  def extract[A: TextMapGetter](ctx: Vault, carrier: A): Vault =
    fromJContext(
      jPropagator.extract(toJContext(ctx), carrier, fromTextMapGetter)
    )

  def inject[A: TextMapSetter](ctx: Vault, carrier: A): F[Unit] =
    Sync[F].delay(
      jPropagator.inject(toJContext(ctx), carrier, fromTextMapSetter)
    )
}
