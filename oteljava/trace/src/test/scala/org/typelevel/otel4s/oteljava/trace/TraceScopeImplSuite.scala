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

package org.typelevel.otel4s.oteljava.trace

import cats.effect.IO
import cats.effect.IOLocal
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.trace.TraceScope
import org.typelevel.otel4s.trace.TraceScopeSuite

class TraceScopeImplSuite extends TraceScopeSuite[Context, Context.Key] {

  protected def createTraceScope: IO[TraceScope[IO, Context]] =
    IOLocal(Context.root).map(_.asLocal).map { implicit local =>
      TraceScopeImpl.fromLocal[IO]
    }

}
