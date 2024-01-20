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

package org.typelevel.otel4s
package sdk
package trace

import cats.Functor
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.internal.ComponentRegistry
import org.typelevel.otel4s.trace.Tracer
import org.typelevel.otel4s.trace.TracerBuilder

private final case class SdkTracerBuilder[F[_]: Functor](
    registry: ComponentRegistry[F, SdkTracer[F]],
    name: String,
    version: Option[String] = None,
    schemaUrl: Option[String] = None
) extends TracerBuilder[F] {

  def withVersion(version: String): TracerBuilder[F] =
    copy(version = Option(version))

  def withSchemaUrl(schemaUrl: String): TracerBuilder[F] =
    copy(schemaUrl = Option(schemaUrl))

  def get: F[Tracer[F]] =
    registry.get(name, version, schemaUrl, Attributes.empty).widen
}
