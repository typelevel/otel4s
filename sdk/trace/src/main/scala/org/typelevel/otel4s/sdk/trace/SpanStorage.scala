/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk.trace

import cats.effect.Concurrent
import cats.effect.std.MapRef
import cats.syntax.functor._
import org.typelevel.otel4s.trace.SpanContext

/** The span storage is used to keep the references of the active SpanRefs.
  *
  * @see
  *   [[SdkTracer.currentSpanOrNoop]]
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
private class SpanStorage[F[_]] private (
    storage: MapRef[F, SpanContext, Option[SpanRef[F]]]
) {

  def add(span: SpanRef[F]): F[Unit] =
    storage(span.context).set(Some(span))

  def remove(span: SpanRef[F]): F[Unit] =
    storage(span.context).set(None)

  def get(context: SpanContext): F[Option[SpanRef[F]]] =
    storage(context).get
}

private object SpanStorage {
  def create[F[_]: Concurrent]: F[SpanStorage[F]] =
    for {
      storage <- MapRef[F, SpanContext, SpanRef[F]]
    } yield new SpanStorage[F](storage)
}
