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

package org.typelevel.otel4s.logs.scalacheck

import org.scalacheck.Gen
import org.typelevel.otel4s.logs.Severity

trait Gens {

  val severity: Gen[Severity] =
    Gen.oneOf(
      Severity.Trace.trace1,
      Severity.Trace.trace2,
      Severity.Trace.trace3,
      Severity.Trace.trace4,
      Severity.Debug.debug1,
      Severity.Debug.debug2,
      Severity.Debug.debug3,
      Severity.Debug.debug4,
      Severity.Info.info1,
      Severity.Info.info2,
      Severity.Info.info3,
      Severity.Info.info4,
      Severity.Warn.warn1,
      Severity.Warn.warn2,
      Severity.Warn.warn3,
      Severity.Warn.warn4,
      Severity.Error.error1,
      Severity.Error.error2,
      Severity.Error.error3,
      Severity.Error.error4,
      Severity.Fatal.fatal1,
      Severity.Fatal.fatal2,
      Severity.Fatal.fatal3,
      Severity.Fatal.fatal4,
    )

}

object Gens extends Gens
