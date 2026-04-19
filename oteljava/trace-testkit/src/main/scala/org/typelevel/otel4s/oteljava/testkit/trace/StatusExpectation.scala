/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.oteljava.testkit.trace

import cats.data.NonEmptyList
import io.opentelemetry.api.trace.{StatusCode => JStatusCode}
import io.opentelemetry.sdk.trace.data.{StatusData => JStatusData}
import org.typelevel.otel4s.oteljava.testkit.ExpectationChecks
import org.typelevel.otel4s.trace.StatusCode

/** A partial expectation for OpenTelemetry Java `StatusData`. */
sealed trait StatusExpectation {

  /** Requires the status code to match exactly. */
  def code(code: StatusCode): StatusExpectation

  /** Requires the status description to match exactly. */
  def description(description: String): StatusExpectation

  /** Requires the status description to match exactly. */
  def description(description: Option[String]): StatusExpectation

  /** Checks the given status and returns structured failures when the expectation does not match. */
  def check(status: JStatusData): Either[NonEmptyList[StatusExpectation.Mismatch], Unit]

  /** Returns `true` if this expectation matches the given status. */
  final def matches(status: JStatusData): Boolean =
    check(status).isRight
}

object StatusExpectation {

  sealed trait Mismatch extends Product with Serializable {
    def message: String
  }

  object Mismatch {

    private[trace] final case class CodeMismatch(expected: StatusCode, actual: StatusCode) extends Mismatch {
      def message: String =
        s"status code mismatch: expected '$expected', got '$actual'"
    }

    private[trace] final case class DescriptionMismatch(expected: Option[String], actual: Option[String])
        extends Mismatch {
      def message: String = {
        val exp = expected.fold("<missing>")(v => s"'$v'")
        val act = actual.fold("<missing>")(v => s"'$v'")
        s"status description mismatch: expected $exp, got $act"
      }
    }
  }

  /** Creates an expectation that matches any status. */
  def any: StatusExpectation =
    Impl()

  /** Creates an expectation that matches an unset status. */
  def unset: StatusExpectation =
    code(StatusCode.Unset)

  /** Creates an expectation that matches an ok status. */
  def ok: StatusExpectation =
    code(StatusCode.Ok)

  /** Creates an expectation that matches an error status. */
  def error: StatusExpectation =
    code(StatusCode.Error)

  /** Creates an expectation that matches the given status code. */
  def code(code: StatusCode): StatusExpectation =
    Impl(expectedCode = Some(code))

  private final case class Impl(
      expectedCode: Option[StatusCode] = None,
      expectedDescription: Option[Option[String]] = None
  ) extends StatusExpectation {

    def code(code: StatusCode): StatusExpectation =
      copy(expectedCode = Some(code))

    def description(description: String): StatusExpectation =
      this.description(Some(description))

    def description(description: Option[String]): StatusExpectation =
      copy(expectedDescription = Some(description))

    def check(status: JStatusData): Either[NonEmptyList[Mismatch], Unit] =
      ExpectationChecks.combine(
        expectedCode.fold(ExpectationChecks.success[Mismatch]) { expected =>
          val actual = toScalaCode(status)
          if (expected == actual) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.CodeMismatch(expected, actual))
        },
        ExpectationChecks.compareOption(expectedDescription, normalizedDescription(status))(
          Mismatch.DescriptionMismatch(_, _)
        )
      )
  }

  private def toScalaCode(status: JStatusData): StatusCode =
    status.getStatusCode match {
      case JStatusCode.UNSET => StatusCode.Unset
      case JStatusCode.OK    => StatusCode.Ok
      case JStatusCode.ERROR => StatusCode.Error
    }

  private def normalizedDescription(status: JStatusData): Option[String] =
    Option(status.getDescription).filter(_.nonEmpty)
}
