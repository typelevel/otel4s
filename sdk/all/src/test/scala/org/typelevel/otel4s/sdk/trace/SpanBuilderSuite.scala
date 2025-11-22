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

package org.typelevel.otel4s.sdk.trace

import cats.effect.IO
import cats.effect.Resource
import org.typelevel.otel4s.sdk.testkit.trace.TracesTestkit
import org.typelevel.otel4s.trace.BaseSpanBuilderSuite
import org.typelevel.otel4s.trace.TracerProvider

class SpanBuilderSuite extends BaseSpanBuilderSuite {

  // todo: dynamically enable/disable an instrument when Meta.Dynamic is fully supported
  protected def makeTracerProvider(enabled: Boolean): Resource[IO, TracerProvider[IO]] =
    if (enabled) TracesTestkit.inMemory[IO]().map(_.tracerProvider)
    else Resource.pure(TracerProvider.noop[IO])

}
