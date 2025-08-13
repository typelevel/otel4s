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
) extends MeterBuilder.Unsealed[F] {

  def withVersion(version: String): MeterBuilder[F] =
    copy(version = Option(version))

  def withSchemaUrl(schemaUrl: String): MeterBuilder[F] =
    copy(schemaUrl = Option(schemaUrl))

  def get: F[Meter[F]] =
    componentRegistry.get(name, version, schemaUrl, Attributes.empty).widen
}
