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

import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.trace.TracerProvider

trait Otel4s[F[_]] {
  def propagators: ContextPropagators[F]

  /** A registry for creating named meters.
    */
  def meterProvider: MeterProvider[F]

  /** An entry point of the tracing API.
    */
  def tracerProvider: TracerProvider[F]

  /** TODO -- how do I write comments?
    */
  override def toString(): String =
    return "TODO -- what goes here?"
}
