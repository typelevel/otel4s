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
      Severity.trace,
      Severity.trace2,
      Severity.trace3,
      Severity.trace4,
      Severity.debug,
      Severity.debug2,
      Severity.debug3,
      Severity.debug4,
      Severity.info,
      Severity.info2,
      Severity.info3,
      Severity.info4,
      Severity.warn,
      Severity.warn2,
      Severity.warn3,
      Severity.warn4,
      Severity.error,
      Severity.error2,
      Severity.error3,
      Severity.error4,
      Severity.fatal,
      Severity.fatal2,
      Severity.fatal3,
      Severity.fatal4,
    )

}

object Gens extends Gens
