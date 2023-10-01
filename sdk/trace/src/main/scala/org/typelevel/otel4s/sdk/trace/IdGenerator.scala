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
import cats.effect.std.Random
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.monad._
import org.typelevel.otel4s.trace.SpanContext.SpanId
import org.typelevel.otel4s.trace.SpanContext.TraceId

trait IdGenerator[F[_]] {
  def generateSpanId: F[String]
  def generateTraceId: F[String]
}

object IdGenerator {

  private val InvalidId = 0

  private[trace] final class RandomIdGenerator[F[_]: Monad: Random]
      extends IdGenerator[F] {

    def generateSpanId: F[String] =
      Random[F].nextLong
        .iterateUntil(_ != InvalidId)
        .map(SpanId.fromLong)

    def generateTraceId: F[String] =
      for {
        hi <- Random[F].nextLong
        lo <- Random[F].nextLong.iterateUntil(_ != InvalidId)
      } yield TraceId.fromLongs(hi, lo)
  }

  def random[F[_]: Monad: Random]: IdGenerator[F] =
    new RandomIdGenerator[F]

}
