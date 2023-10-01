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

package org.typelevel.otel4s.sdk.context.propagation

import cats.effect.SyncIO
import org.typelevel.otel4s.TextMapGetter
import org.typelevel.otel4s.TextMapPropagator
import org.typelevel.otel4s.TextMapUpdater
import org.typelevel.otel4s.sdk.context.Context

class PassThroughPropagator(
    val fields: List[String]
) extends TextMapPropagator[Context] {
  import PassThroughPropagator.ExtractedKeyValuesKey

  def extract[A: TextMapGetter](ctx: Context, carrier: A): Context = {
    val extracted = fields.flatMap { field =>
      TextMapGetter[A].get(carrier, field).map(value => (field, value))
    }

    if (extracted.nonEmpty) ctx.set(ExtractedKeyValuesKey, extracted) else ctx
  }

  def injected[A: TextMapUpdater](ctx: Context, carrier: A): A =
    ctx.get(ExtractedKeyValuesKey) match {
      case Some(extracted) =>
        extracted.foldLeft(carrier) { case (carrier, (key, value)) =>
          TextMapUpdater[A].updated(carrier, key, value)
        }

      case None =>
        carrier
    }

}

object PassThroughPropagator {

  private val ExtractedKeyValuesKey =
    Context.Key
      .unique[SyncIO, List[(String, String)]](
        "otel4s-passthroughpropagator-keyvalues"
      )
      .unsafeRunSync()

  def create(fields: String*): PassThroughPropagator =
    new PassThroughPropagator(fields.toList)

}
