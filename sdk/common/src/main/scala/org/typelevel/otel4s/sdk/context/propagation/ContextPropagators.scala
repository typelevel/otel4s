package org.typelevel.otel4s.sdk.context.propagation

trait ContextPropagators {
  def textMapPropagator: TextMapPropagator
}

object ContextPropagators {

  private object Noop extends ContextPropagators {
    val textMapPropagator: TextMapPropagator = TextMapPropagator.noop
  }

  private final class Default(
      val textMapPropagator: TextMapPropagator
  ) extends ContextPropagators

  def create(textMapPropagator: TextMapPropagator): ContextPropagators =
    new Default(textMapPropagator)

}
