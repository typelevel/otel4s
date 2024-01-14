package org.typelevel.otel4s.sdk.metrics

import cats.Functor
import cats.syntax.functor._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.metrics.MeterBuilder
import org.typelevel.otel4s.sdk.internal.ComponentRegistry

private final case class SdkMeterBuilder[F[_]: Functor](
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
