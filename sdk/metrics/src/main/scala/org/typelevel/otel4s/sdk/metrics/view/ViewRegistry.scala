/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.sdk.metrics.view

import cats.Monad
import cats.data.NonEmptyVector
import cats.effect.std.Console
import cats.syntax.functor._
import cats.syntax.traverse._
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor

import java.util.regex.Pattern

private[metrics] final class ViewRegistry[F[_]: Monad: Console](
    registeredViews: Vector[RegisteredView]
) {

  def findViews(
      descriptor: InstrumentDescriptor,
      scope: InstrumentationScope
  ): F[Option[NonEmptyVector[View]]] =
    registeredViews
      .filter(v => matchesSelector(v.selector, descriptor, scope))
      .flatTraverse[F, View] { entry =>
        val compatible = entry.view.aggregation.forall(
          _.compatibleWith(descriptor.instrumentType)
        )

        if (compatible) Monad[F].pure(Vector(entry.view))
        else warn(descriptor, entry.view).as(Vector.empty)
      }
      .map(views => NonEmptyVector.fromVector(views))

  private def matchesSelector(
      selector: InstrumentSelector,
      descriptor: InstrumentDescriptor,
      meterScope: InstrumentationScope
  ): Boolean = {
    selector.instrumentType.forall(tpe => descriptor.instrumentType == tpe) &&
    selector.instrumentUnit.forall(unit => descriptor.unit.contains(unit)) &&
    selector.instrumentName.forall(n =>
      ViewRegistry.toGlobPattern(n)(descriptor.name.toString)
    ) &&
    selector.meterName.forall(n => n == meterScope.name) &&
    selector.meterVersion.forall(v => meterScope.version.contains(v)) &&
    selector.meterSchemaUrl.forall(s => meterScope.schemaUrl.contains(s))
  }

  private def warn(descriptor: InstrumentDescriptor, view: View): F[Unit] =
    Console[F].println(
      s"$view aggregation is incompatible with instrument [${descriptor.name}] of type [${descriptor.instrumentType}]"
    )

}

private[metrics] object ViewRegistry {

  def apply[F[_]: Monad: Console](
      registeredViews: Vector[RegisteredView]
  ): ViewRegistry[F] =
    new ViewRegistry(registeredViews)

  private[view] def toGlobPattern(globPattern: String): String => Boolean =
    if (globPattern == "*") {
      Function.const(true)
    } else if (globPattern.exists(c => c == '*' || c == '?')) {
      val pattern = makePattern(globPattern)
      s => pattern.matcher(s).matches()
    } else { s =>
      globPattern.equalsIgnoreCase(s)
    }

  /** Transforms the `input` to a regex by converting `*` to `.*`, `?` to `.`,
    * and escaping other characters.
    */
  private def makePattern(input: String): Pattern = {
    def quote(str: String): String =
      if (str.nonEmpty) Pattern.quote(str) else ""

    val (preToken, result) = input.foldLeft(("", "")) {
      case ((preToken, result), char) if char == '*' || char == '?' =>
        val next = if (char == '*') ".*" else "."
        ("", result + quote(preToken) + next)

      case ((preToken, result), char) =>
        (preToken :+ char, result)
    }

    Pattern.compile(result + quote(preToken))
  }

}
