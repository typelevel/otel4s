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
package trace

import cats.Applicative
import cats.Semigroup
import cats.data.NonEmptyList
import cats.effect.kernel.Resource
import cats.syntax.applicative._
import cats.syntax.foldable._
import cats.syntax.semigroup._

sealed trait SpanFinalizer

object SpanFinalizer {

  type Strategy = PartialFunction[Resource.ExitCase, SpanFinalizer]

  object Strategy {
    def empty: Strategy = PartialFunction.empty

    def reportAbnormal: Strategy = {
      case Resource.ExitCase.Errored(e) =>
        recordException(e) |+| setStatus(Status.Error)

      case Resource.ExitCase.Canceled =>
        setStatus(Status.Error, "canceled")
    }
  }

  final class RecordException private[SpanFinalizer] (
      val throwable: Throwable
  ) extends SpanFinalizer

  final class SetStatus private[SpanFinalizer] (
      val status: Status,
      val description: Option[String]
  ) extends SpanFinalizer

  final class SetAttributes private[SpanFinalizer] (
      val attributes: Seq[Attribute[_]]
  ) extends SpanFinalizer

  final class Multiple private[SpanFinalizer] (
      val finalizers: NonEmptyList[SpanFinalizer]
  ) extends SpanFinalizer

  def recordException(throwable: Throwable): SpanFinalizer =
    new RecordException(throwable)

  def setStatus(status: Status): SpanFinalizer =
    new SetStatus(status, None)

  def setStatus(status: Status, description: String): SpanFinalizer =
    new SetStatus(status, Some(description))

  def setAttribute[A](attribute: Attribute[A]): SpanFinalizer =
    new SetAttributes(List(attribute))

  def setAttributes(attributes: Attribute[_]*): SpanFinalizer =
    new SetAttributes(attributes)

  def multiple(head: SpanFinalizer, tail: SpanFinalizer*): Multiple =
    new Multiple(NonEmptyList.of(head, tail: _*))

  implicit val spanFinalizerSemigroup: Semigroup[SpanFinalizer] = {
    case (left: Multiple, right: Multiple) =>
      new Multiple(left.finalizers.concatNel(right.finalizers))

    case (left: Multiple, right: SpanFinalizer) =>
      new Multiple(left.finalizers.append(right))

    case (left: SpanFinalizer, right: Multiple) =>
      new Multiple(right.finalizers.prepend(left))

    case (left, right) =>
      new Multiple(NonEmptyList.of(left, right))
  }

  private[otel4s] def run[F[_]: Applicative](
      backend: Span.Backend[F],
      finalizer: SpanFinalizer
  ): F[Unit] = {

    def loop(input: SpanFinalizer): F[Unit] =
      input match {
        case r: RecordException =>
          backend.recordException(r.throwable)

        case s: SetStatus =>
          s.description match {
            case Some(desc) => backend.setStatus(s.status, desc)
            case None       => backend.setStatus(s.status)
          }

        case s: SetAttributes =>
          backend.setAttributes(s.attributes: _*)

        case m: Multiple =>
          m.finalizers.traverse_(strategy => loop(strategy))
      }

    loop(finalizer).whenA(backend.meta.isEnabled)
  }

}
