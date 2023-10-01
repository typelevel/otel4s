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

import cats.Monad
import cats.effect.Sync
import cats.effect.std.Random
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.monad._
import org.typelevel.otel4s.trace.SpanContext

/** Used by the [[SdkTracer]] to generate new span and trace ids.
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
trait IdGenerator[F[_]] {

  /** Generates a valid span id. */
  def generateSpanId: F[String]

  /** Generates a valid trace id. */
  def generateTraceId: F[String]
}

object IdGenerator {

  private val InvalidId = 0

  private[trace] final class RandomIdGenerator[F[_]: Monad: Random]
      extends IdGenerator[F] {

    def generateSpanId: F[String] =
      Random[F].nextLong
        .iterateUntil(_ != InvalidId)
        .map(SpanContext.SpanId.fromLong)

    def generateTraceId: F[String] =
      for {
        hi <- Random[F].nextLong
        lo <- Random[F].nextLong.iterateUntil(_ != InvalidId)
      } yield SpanContext.TraceId.fromLongs(hi, lo)
  }

  def random[F[_]: Monad: Random]: IdGenerator[F] =
    new RandomIdGenerator[F]

  def default[F[_]: Sync]: F[IdGenerator[F]] =
    Random.scalaUtilRandom[F].map(implicit rnd => random[F])

}
