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
package oteljava
package metrics

import cats.effect.kernel.Async
import io.opentelemetry.api.metrics.{MeterProvider => JMeterProvider}
import org.typelevel.otel4s.metrics.MeterBuilder
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.oteljava.context.AskContext

private[oteljava] class MeterProviderImpl[F[_]: Async: AskContext](
    jMeterProvider: JMeterProvider
) extends MeterProvider.Unsealed[F] {

  def meter(name: String): MeterBuilder[F] =
    MeterBuilderImpl(jMeterProvider, name)

}
