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

package org.typelevel.otel4s.java.trace

import cats.effect.Sync
import io.opentelemetry.api.trace.{TracerProvider => JTracerProvider}
import org.typelevel.otel4s.trace._

private[trace] final case class TracerBuilderImpl[F[_]](
    jTracerProvider: JTracerProvider,
    scope: TraceScope[F],
    name: String,
    version: Option[String] = None,
    schemaUrl: Option[String] = None
)(implicit F: Sync[F])
    extends TracerBuilder[F] {

  def withVersion(version: String): TracerBuilder[F] =
    copy(version = Option(version))

  def withSchemaUrl(schemaUrl: String): TracerBuilder[F] =
    copy(schemaUrl = Option(schemaUrl))

  def get: F[Tracer[F]] = F.delay {
    val b = jTracerProvider.tracerBuilder(name)
    version.foreach(b.setInstrumentationVersion)
    schemaUrl.foreach(b.setSchemaUrl)
    new TracerImpl(b.build(), scope)
  }

}
