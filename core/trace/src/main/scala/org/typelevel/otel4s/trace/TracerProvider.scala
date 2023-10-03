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
package trace

import cats.Applicative

/** The entry point of the tracing API. Provides access to [[Tracer]].
  */
trait TracerProvider[F[_]] {

  /** Creates a named [[Tracer]].
    *
    * @example
    *   {{{
    * val tracerProvider: TracerProvider[IO] = ???
    * val tracer: IO[Tracer[IO]] = tracerProvider.get("com.service.runtime")
    *   }}}
    *
    * @param name
    *   the name of the instrumentation scope, such as the instrumentation
    *   library, package, or fully qualified class name
    */
  def get(name: String): F[Tracer[F]] =
    tracer(name).get

  /** Creates a [[TracerBuilder]] for a named [[Tracer]] instance.
    *
    * @example
    *   {{{
    * val tracerProvider: TracerProvider[IO] = ???
    * val tracer: IO[Tracer[IO]] = tracerProvider
    *   .tracer("com.service.runtime")
    *   .withVersion("1.0.0")
    *   .withSchemaUrl("https://opentelemetry.io/schema/v1.1.0")
    *   .get
    *   }}}
    *
    * @param name
    *   the name of the instrumentation scope, such as the instrumentation
    *   library, package, or fully qualified class name
    */
  def tracer(name: String): TracerBuilder[F]
}

object TracerProvider {

  /** Creates a no-op implementation of the [[TracerProvider]].
    *
    * A [[TracerBuilder]] has no-op implementation too.
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    */
  def noop[F[_]: Applicative]: TracerProvider[F] =
    new TracerProvider[F] {
      def tracer(name: String): TracerBuilder[F] =
        TracerBuilder.noop
    }
}
