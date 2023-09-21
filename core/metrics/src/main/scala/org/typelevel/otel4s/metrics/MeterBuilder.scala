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

package org.typelevel.otel4s.metrics

import cats.Applicative

trait MeterBuilder[F[_]] {

  /** Assigns a version to the resulting Meter.
    *
    * @param version
    *   the version of the instrumentation scope
    */
  def withVersion(version: String): MeterBuilder[F]

  /** Assigns an OpenTelemetry schema URL to the resulting Meter.
    *
    * @param schemaUrl
    *   the URL of the OpenTelemetry schema
    */
  def withSchemaUrl(schemaUrl: String): MeterBuilder[F]

  /** Creates a [[Meter]] with the given `version` and `schemaUrl` (if any).
    */
  def get: F[Meter[F]]
}

object MeterBuilder {

  /** Creates a no-op implementation of the [[MeterBuilder]].
    *
    * A [[Meter]] has no-op implementation too.
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    */
  def noop[F[_]](implicit F: Applicative[F]): MeterBuilder[F] =
    new MeterBuilder[F] {
      def withVersion(version: String): MeterBuilder[F] = this
      def withSchemaUrl(schemaUrl: String): MeterBuilder[F] = this
      def get: F[Meter[F]] = F.pure(Meter.noop)
    }
}
