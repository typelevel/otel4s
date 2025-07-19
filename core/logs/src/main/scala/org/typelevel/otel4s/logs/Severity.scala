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
  * @param value
  *   smaller numerical values correspond to less severe events (such as debug events), larger numerical values
  *   correspond to more severe events (such as errors and critical events)
  *
  * @param name
  *   a human-readable name, see [[https://opentelemetry.io/docs/specs/otel/logs/data-model/#displaying-severity]]
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/logs/data-model/#field-severitynumber]]
  */
sealed abstract class Severity(val value: Int, val name: String) {

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
  sealed abstract class Trace(value: Int, name: String) extends Severity(value, name)

  /** A debugging event.
    */
  sealed abstract class Debug(value: Int, name: String) extends Severity(value, name)

  /** An informational event. Indicates that an event happened.
    */
  sealed abstract class Info(value: Int, name: String) extends Severity(value, name)

  /** A warning event. Not an error but is likely more important than an informational event.
    */
  sealed abstract class Warn(value: Int, name: String) extends Severity(value, name)

  /** An error event. Something went wrong.
    */
  sealed abstract class Error(value: Int, name: String) extends Severity(value, name)

  /** A fatal error such as application or system crash.
    */
  sealed abstract class Fatal(value: Int, name: String) extends Severity(value, name)

  /** A fine-grained debugging event. Typically disabled in default configurations.
    */
  def trace: Trace = Trace1
  def trace2: Trace = Trace2
  def trace3: Trace = Trace3
  def trace4: Trace = Trace4

  /** A debugging event.
    */
  def debug: Debug = Debug1
  def debug2: Debug = Debug2
  def debug3: Debug = Debug3
  def debug4: Debug = Debug4

  /** An informational event. Indicates that an event happened.
    */
  def info: Info = Info1
  def info2: Info = Info2
  def info3: Info = Info3
  def info4: Info = Info4

  /** A warning event. Not an error but is likely more important than an informational event.
    */
  def warn: Warn = Warn1
  def warn2: Warn = Warn2
  def warn3: Warn = Warn3
  def warn4: Warn = Warn4

  /** An error event. Something went wrong.
    */
  def error: Error = Error1
  def error2: Error = Error2
  def error3: Error = Error3
  def error4: Error = Error4

  /** A fatal error such as application or system crash.
    */
  def fatal: Fatal = Fatal1
  def fatal2: Fatal = Fatal2
  def fatal3: Fatal = Fatal3
  def fatal4: Fatal = Fatal4

  implicit val severityHash: Hash[Severity] = Hash.by(_.value)

  /** @see
    *   [[https://opentelemetry.io/docs/specs/otel/logs/data-model/#displaying-severity]]
    */
  implicit val severityShow: Show[Severity] = Show.show(_.name)

  private[otel4s] case object Trace1 extends Trace(1, "TRACE")
  private[otel4s] case object Trace2 extends Trace(2, "TRACE2")
  private[otel4s] case object Trace3 extends Trace(3, "TRACE3")
  private[otel4s] case object Trace4 extends Trace(4, "TRACE4")

  private[otel4s] case object Debug1 extends Debug(5, "DEBUG")
  private[otel4s] case object Debug2 extends Debug(6, "DEBUG2")
  private[otel4s] case object Debug3 extends Debug(7, "DEBUG3")
  private[otel4s] case object Debug4 extends Debug(8, "DEBUG4")

  private[otel4s] case object Warn1 extends Warn(13, "WARN")
  private[otel4s] case object Warn2 extends Warn(14, "WARN2")
  private[otel4s] case object Warn3 extends Warn(15, "WARN3")
  private[otel4s] case object Warn4 extends Warn(16, "WARN4")

  private[otel4s] case object Info1 extends Info(9, "INFO")
  private[otel4s] case object Info2 extends Info(10, "INFO2")
  private[otel4s] case object Info3 extends Info(11, "INFO3")
  private[otel4s] case object Info4 extends Info(12, "INFO4")

  private[otel4s] case object Error1 extends Error(17, "ERROR")
  private[otel4s] case object Error2 extends Error(18, "ERROR2")
  private[otel4s] case object Error3 extends Error(19, "ERROR3")
  private[otel4s] case object Error4 extends Error(20, "ERROR4")

  private[otel4s] case object Fatal1 extends Fatal(21, "FATAL")
  private[otel4s] case object Fatal2 extends Fatal(22, "FATAL2")
  private[otel4s] case object Fatal3 extends Fatal(23, "FATAL3")
  private[otel4s] case object Fatal4 extends Fatal(24, "FATAL4")
}
