package org.typelevel.otel4s.sdk.context.propagation

import cats.effect.SyncIO
import org.typelevel.otel4s.TextMapGetter
import org.typelevel.otel4s.TextMapUpdater
import org.typelevel.otel4s.sdk.context.Context

class PassThroughPropagator(
    val fields: List[String]
) extends TextMapPropagator {
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
