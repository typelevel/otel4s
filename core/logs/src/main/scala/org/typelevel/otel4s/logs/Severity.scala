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

package org.typelevel.otel4s.logs

import cats.Hash
import cats.Show

/** Represents the severity of a log record.
  *
  * @param severity
  *   smaller numerical values correspond to less severe events (such as debug events), larger numerical values
  *   correspond to more severe events (such as errors and critical events)
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/logs/data-model/#field-severitynumber]]
  */
sealed abstract class Severity(val severity: Int) {

  override final def hashCode(): Int =
    Hash[Severity].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: Severity => Hash[Severity].eqv(this, other)
      case _               => false
    }

  override final def toString: String =
    Show[Severity].show(this)

}

object Severity {

  /** A fine-grained debugging event. Typically disabled in default configurations.
    */
  sealed abstract class Trace(severity: Int) extends Severity(severity)
  object Trace {
    def trace1: Trace = Trace1
    def trace2: Trace = Trace2
    def trace3: Trace = Trace3
    def trace4: Trace = Trace4

    private[otel4s] case object Trace1 extends Trace(1)
    private[otel4s] case object Trace2 extends Trace(2)
    private[otel4s] case object Trace3 extends Trace(3)
    private[otel4s] case object Trace4 extends Trace(4)
  }

  /** A debugging event.
    */
  sealed abstract class Debug(severity: Int) extends Severity(severity)
  object Debug {
    def debug1: Debug = Debug1
    def debug2: Debug = Debug2
    def debug3: Debug = Debug3
    def debug4: Debug = Debug4

    private[otel4s] case object Debug1 extends Debug(5)
    private[otel4s] case object Debug2 extends Debug(6)
    private[otel4s] case object Debug3 extends Debug(7)
    private[otel4s] case object Debug4 extends Debug(8)
  }

  /** An informational event. Indicates that an event happened.
    */
  sealed abstract class Info(severity: Int) extends Severity(severity)
  object Info {
    def info1: Info = Info1
    def info2: Info = Info2
    def info3: Info = Info3
    def info4: Info = Info4

    private[otel4s] case object Info1 extends Info(9)
    private[otel4s] case object Info2 extends Info(10)
    private[otel4s] case object Info3 extends Info(11)
    private[otel4s] case object Info4 extends Info(12)
  }

  /** A warning event. Not an error but is likely more important than an informational event.
    */
  sealed abstract class Warn(severity: Int) extends Severity(severity)
  object Warn {
    def warn1: Warn = Warn1
    def warn2: Warn = Warn2
    def warn3: Warn = Warn3
    def warn4: Warn = Warn4

    private[otel4s] case object Warn1 extends Warn(13)
    private[otel4s] case object Warn2 extends Warn(14)
    private[otel4s] case object Warn3 extends Warn(15)
    private[otel4s] case object Warn4 extends Warn(16)
  }

  /** An error event. Something went wrong.
    */
  sealed abstract class Error(severity: Int) extends Severity(severity)
  object Error {
    def error1: Error = Error1
    def error2: Error = Error2
    def error3: Error = Error3
    def error4: Error = Error4

    private[otel4s] case object Error1 extends Error(17)
    private[otel4s] case object Error2 extends Error(18)
    private[otel4s] case object Error3 extends Error(19)
    private[otel4s] case object Error4 extends Error(20)
  }

  /** A fatal error such as application or system crash.
    */
  sealed abstract class Fatal(severity: Int) extends Severity(severity)
  object Fatal {
    def fatal1: Fatal = Fatal1
    def fatal2: Fatal = Fatal2
    def fatal3: Fatal = Fatal3
    def fatal4: Fatal = Fatal4

    private[otel4s] case object Fatal1 extends Fatal(21)
    private[otel4s] case object Fatal2 extends Fatal(22)
    private[otel4s] case object Fatal3 extends Fatal(23)
    private[otel4s] case object Fatal4 extends Fatal(24)
  }

  implicit val severityHash: Hash[Severity] = Hash.by(_.severity)

  /** @see
    *   [[https://opentelemetry.io/docs/specs/otel/logs/data-model/#displaying-severity]]
    */
  implicit val severityShow: Show[Severity] = {
    case Severity.Trace.Trace1 => "TRACE"
    case Severity.Trace.Trace2 => "TRACE2"
    case Severity.Trace.Trace3 => "TRACE3"
    case Severity.Trace.Trace4 => "TRACE4"
    case Severity.Debug.Debug1 => "DEBUG"
    case Severity.Debug.Debug2 => "DEBUG2"
    case Severity.Debug.Debug3 => "DEBUG3"
    case Severity.Debug.Debug4 => "DEBUG4"
    case Severity.Info.Info1   => "INFO"
    case Severity.Info.Info2   => "INFO2"
    case Severity.Info.Info3   => "INFO3"
    case Severity.Info.Info4   => "INFO4"
    case Severity.Warn.Warn1   => "WARN"
    case Severity.Warn.Warn2   => "WARN2"
    case Severity.Warn.Warn3   => "WARN3"
    case Severity.Warn.Warn4   => "WARN4"
    case Severity.Error.Error1 => "ERROR"
    case Severity.Error.Error2 => "ERROR2"
    case Severity.Error.Error3 => "ERROR3"
    case Severity.Error.Error4 => "ERROR4"
    case Severity.Fatal.Fatal1 => "FATAL"
    case Severity.Fatal.Fatal2 => "FATAL2"
    case Severity.Fatal.Fatal3 => "FATAL3"
    case Severity.Fatal.Fatal4 => "FATAL4"
  }
}
