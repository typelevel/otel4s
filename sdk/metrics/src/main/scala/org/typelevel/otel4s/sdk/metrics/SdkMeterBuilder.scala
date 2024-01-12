package org.typelevel.otel4s.sdk.metrics

import cats.Monad
import cats.effect.kernel.Clock
import cats.effect.std.Console
import cats.syntax.functor._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.{Meter, MeterBuilder}
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.internal.ComponentRegistry

private final case class SdkMeterBuilder[
    F[_]: Monad: Clock: Console: AskContext
](
    componentRegistry: ComponentRegistry[F, SdkMeter[F]],
    name: String,
    version: Option[String] = None,
    schemaUrl: Option[String] = None
) extends MeterBuilder[F] {

  def withVersion(version: String): MeterBuilder[F] =
    copy(version = Option(version))

  def withSchemaUrl(schemaUrl: String): MeterBuilder[F] =
    copy(schemaUrl = Option(schemaUrl))

  def get: F[Meter[F]] =
    componentRegistry.get(name, version, schemaUrl, Attributes.empty).widen
}
