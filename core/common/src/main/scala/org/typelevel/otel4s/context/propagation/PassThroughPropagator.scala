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

package org.typelevel.otel4s.context
package propagation

import cats.effect.SyncIO
import org.typelevel.otel4s.context.syntax._

/** A [[TextMapPropagator]] that extracts a specified collection of fields and stores them in a context, and extracts
  * them from a context later for injection. It does not interact with telemetry.
  */
final class PassThroughPropagator[Ctx, K[X] <: Key[X]] private (
    val fields: Iterable[String],
    entriesKey: K[List[(String, String)]]
)(implicit c: Contextual.Keyed[Ctx, K])
    extends TextMapPropagator[Ctx] {

  def extract[A](ctx: Ctx, carrier: A)(implicit
      getter: TextMapGetter[A]
  ): Ctx = {
    val list = fields.view
      .flatMap(k => getter.get(carrier, k).map(k -> _))
      .toList
    if (list.isEmpty) ctx else ctx.updated(entriesKey, list)
  }

  def inject[A](ctx: Ctx, carrier: A)(implicit updater: TextMapUpdater[A]): A =
    ctx
      .getOrElse(entriesKey, Nil)
      .foldLeft(carrier) { case (c, k -> v) => updater.updated(c, k, v) }

  override def toString: String =
    s"PassThroughPropagator{fields=${fields.mkString("[", ", ", "]")}}"
}

object PassThroughPropagator {
  private def forDistinctFields[Ctx, K[X] <: Key[X]](fields: Seq[String])(implicit
      c: Contextual.Keyed[Ctx, K],
      kp: Key.Provider[SyncIO, K]
  ): TextMapPropagator[Ctx] =
    if (fields.isEmpty) TextMapPropagator.noop
    else {
      new PassThroughPropagator(
        fields,
        kp.uniqueKey[List[(String, String)]](
          "otel4s-PassThroughPropagator-entries"
        ).unsafeRunSync()
      )
    }

  /** Creates a `PassThroughPropagator` that propagates the given fields. */
  def apply[Ctx, K[X] <: Key[X]](fields: String*)(implicit
      c: Contextual.Keyed[Ctx, K],
      kp: Key.Provider[SyncIO, K]
  ): TextMapPropagator[Ctx] =
    forDistinctFields(fields.distinct)

  /** Creates a `PassThroughPropagator` that propagates the given fields. */
  def apply[Ctx, K[X] <: Key[X]](fields: Iterable[String])(implicit
      c: Contextual.Keyed[Ctx, K],
      kp: Key.Provider[SyncIO, K]
  ): TextMapPropagator[Ctx] =
    forDistinctFields(fields.iterator.distinct.toSeq)
}
