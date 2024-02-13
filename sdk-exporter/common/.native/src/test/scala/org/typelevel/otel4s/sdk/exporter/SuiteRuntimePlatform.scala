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

package org.typelevel.otel4s.sdk.exporter

import cats.effect.unsafe.IORuntime
import epollcat.unsafe.EpollRuntime
import munit.CatsEffectSuite

// can be removed once CE 3.6.x is released
trait SuiteRuntimePlatform { self: CatsEffectSuite =>
  override def munitIORuntime: IORuntime = EpollRuntime.global
}
