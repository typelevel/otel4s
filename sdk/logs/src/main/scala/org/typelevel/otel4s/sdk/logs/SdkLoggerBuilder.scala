/*
 * Copyright 2025 Typelevel
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

package org.typelevel.otel4s.sdk.logs

import cats.Functor
import cats.syntax.functor._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.logs.Logger
import org.typelevel.otel4s.logs.LoggerBuilder
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.internal.ComponentRegistry

/** SDK implementation of the [[LoggerBuilder]].
  */
private final case class SdkLoggerBuilder[F[_]: Functor](
    componentRegistry: ComponentRegistry[F, SdkLogger[F]],
    name: String,
    version: Option[String] = None,
    schemaUrl: Option[String] = None
) extends LoggerBuilder.Unsealed[F, Context] {

  def withVersion(version: String): LoggerBuilder[F, Context] =
    copy(version = Option(version))

  def withSchemaUrl(schemaUrl: String): LoggerBuilder[F, Context] =
    copy(schemaUrl = Option(schemaUrl))

  def get: F[Logger[F, Context]] =
    componentRegistry.get(name, version, schemaUrl, Attributes.empty).widen
}
