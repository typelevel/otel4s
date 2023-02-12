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

import cats.Applicative
import org.typelevel.vault.Vault

trait TextMapPropagator[F[_]] {
  def extract[A: TextMapGetter](ctx: Vault, carrier: A): Vault

  def inject[A: TextMapSetter](ctx: Vault, carrier: A): F[Unit]
}

object TextMapPropagator {
  def noop[F[_]: Applicative, C]: TextMapPropagator[F] =
    new TextMapPropagator[F] {
      def extract[A: TextMapGetter](ctx: Vault, carrier: A): Vault =
        ctx

      def inject[A: TextMapSetter](ctx: Vault, carrier: A): F[Unit] =
        Applicative[F].unit
    }
}
