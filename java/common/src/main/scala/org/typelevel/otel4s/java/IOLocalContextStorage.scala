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

package org.typelevel.otel4s.java

import cats.Eval
import cats.effect.IOLocal
import cats.effect.unsafe.IOLocals
import io.opentelemetry.context.Context
import io.opentelemetry.context.ContextStorage
import io.opentelemetry.context.Scope

class IOLocalContextStorage(
    _ioLocal: => IOLocal[Context]
) extends ContextStorage {
  private[this] lazy val ioLocal = _ioLocal
  override def attach(toAttach: Context): Scope = {
    val previous = current()
    IOLocals.set(ioLocal.value, toAttach)
    new Scope {
      def close() = IOLocals.set(ioLocal.value, previous)
    }
  }

  override def current(): Context =
    IOLocals.get(ioLocal.value)

}
