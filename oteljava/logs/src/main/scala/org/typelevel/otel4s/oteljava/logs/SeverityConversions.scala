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

package org.typelevel.otel4s.oteljava.logs

import io.opentelemetry.api.logs.{Severity => JSeverity}
import org.typelevel.otel4s.logs.Severity

private[oteljava] object SeverityConversions {

  def toJava(severity: Severity): JSeverity =
    severity match {
      case Severity.Trace1 => JSeverity.TRACE
      case Severity.Trace2 => JSeverity.TRACE2
      case Severity.Trace3 => JSeverity.TRACE3
      case Severity.Trace4 => JSeverity.TRACE4

      case Severity.Debug1 => JSeverity.DEBUG
      case Severity.Debug2 => JSeverity.DEBUG2
      case Severity.Debug3 => JSeverity.DEBUG3
      case Severity.Debug4 => JSeverity.DEBUG4

      case Severity.Info1 => JSeverity.INFO
      case Severity.Info2 => JSeverity.INFO2
      case Severity.Info3 => JSeverity.INFO3
      case Severity.Info4 => JSeverity.INFO4

      case Severity.Warn1 => JSeverity.WARN
      case Severity.Warn2 => JSeverity.WARN2
      case Severity.Warn3 => JSeverity.WARN3
      case Severity.Warn4 => JSeverity.WARN4

      case Severity.Error1 => JSeverity.ERROR
      case Severity.Error2 => JSeverity.ERROR2
      case Severity.Error3 => JSeverity.ERROR3
      case Severity.Error4 => JSeverity.ERROR4

      case Severity.Fatal1 => JSeverity.FATAL
      case Severity.Fatal2 => JSeverity.FATAL2
      case Severity.Fatal3 => JSeverity.FATAL3
      case Severity.Fatal4 => JSeverity.FATAL4
    }

  def toScala(severity: JSeverity): Option[Severity] =
    severity.getSeverityNumber match {
      case 0 => None

      case 1 => Some(Severity.trace)
      case 2 => Some(Severity.trace2)
      case 3 => Some(Severity.trace3)
      case 4 => Some(Severity.trace4)

      case 5 => Some(Severity.debug)
      case 6 => Some(Severity.debug2)
      case 7 => Some(Severity.debug3)
      case 8 => Some(Severity.debug4)

      case 9  => Some(Severity.info)
      case 10 => Some(Severity.info2)
      case 11 => Some(Severity.info3)
      case 12 => Some(Severity.info4)

      case 13 => Some(Severity.warn)
      case 14 => Some(Severity.warn2)
      case 15 => Some(Severity.warn3)
      case 16 => Some(Severity.warn4)

      case 17 => Some(Severity.error)
      case 18 => Some(Severity.error2)
      case 19 => Some(Severity.error3)
      case 20 => Some(Severity.error4)

      case 21 => Some(Severity.fatal)
      case 22 => Some(Severity.fatal2)
      case 23 => Some(Severity.fatal3)
      case 24 => Some(Severity.fatal4)

      case _ => None
    }

}
