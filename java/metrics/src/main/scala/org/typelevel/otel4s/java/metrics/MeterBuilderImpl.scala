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
package java
package metrics

import cats.effect.kernel.Async
import io.opentelemetry.api.{OpenTelemetry => JOpenTelemetry}
import org.typelevel.otel4s.metrics._

private[java] case class MeterBuilderImpl[F[_]](
    jOtel: JOpenTelemetry,
    name: String,
    version: Option[String] = None,
    schemaUrl: Option[String] = None
)(implicit F: Async[F])
    extends MeterBuilder[F] {
  def withVersion(version: String): MeterBuilder[F] =
    copy(version = Option(version))

  def withSchemaUrl(schemaUrl: String): MeterBuilder[F] =
    copy(schemaUrl = Option(schemaUrl))

  def get: F[Meter[F]] = F.delay {
    val b = jOtel.meterBuilder(name)
    version.foreach(b.setInstrumentationVersion)
    schemaUrl.foreach(b.setSchemaUrl)
    new MeterImpl(b.build())
  }
}
