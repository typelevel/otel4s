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

trait TextMapPropagator[F[_]] {
  type Context

  def extract[A: TextMapGetter](ctx: Context, carrier: A): Context

  def inject[A: TextMapSetter](ctx: Context, carrier: A): F[Unit]
}

object TextMapPropagator {
  type Aux[F[_], C] = TextMapPropagator[F] { type Context = C }

  def noop[F[_]: Applicative, C]: TextMapPropagator.Aux[F, C] =
    new TextMapPropagator[F] {
      type Context = C

      def extract[A: TextMapGetter](ctx: Context, carrier: A): Context =
        ctx

      def inject[A: TextMapSetter](ctx: Context, carrier: A): F[Unit] =
        Applicative[F].unit
    }
}
