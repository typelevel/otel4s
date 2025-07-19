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

package org.typelevel.otel4s.sdk.trace.data

import cats.Hash
import cats.Show
import cats.syntax.show._
import org.typelevel.otel4s.trace.StatusCode

/** Defines the status of a span by providing a standard status in conjunction with an optional description message.
  *
  * @param status
  *   status code
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/trace/api/#set-status]]
  */
sealed abstract class StatusData(val status: StatusCode) {

  /** The description of this status for human consumption.
    */
  def description: Option[String]

  override final lazy val hashCode: Int =
    Hash[StatusData].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: StatusData => Hash[StatusData].eqv(this, other)
      case _                 => false
    }

  override final def toString: String =
    Show[StatusData].show(this)
}

object StatusData {

  /** Indicates the successfully completed operation, validated by an application developer or operator.
    */
  case object Ok extends StatusData(StatusCode.Ok) {
    def description: Option[String] = None
  }

  /** The default state.
    */
  case object Unset extends StatusData(StatusCode.Unset) {
    def description: Option[String] = None
  }

  /** Indicates an occurred error.
    */
  final case class Error(description: Option[String]) extends StatusData(StatusCode.Error)

  /** Returns [[StatusData]] for the given `status`.
    */
  def apply(status: StatusCode): StatusData =
    status match {
      case StatusCode.Ok    => Ok
      case StatusCode.Unset => Unset
      case StatusCode.Error => Error(None)
    }

  /** Creates [[StatusData]] using the given `status` and `description`.
    *
    * @param status
    *   the status of the [[StatusData]]
    * @param description
    *   the description of the [[StatusData]]. Effective only for the
    *   [[org.typelevel.otel4s.trace.StatusCode.Error Status.Error]].
    */
  def apply(status: StatusCode, description: String): StatusData =
    status match {
      case StatusCode.Ok    => Ok
      case StatusCode.Unset => Unset
      case StatusCode.Error =>
        Error(Option.when(description.nonEmpty)(description))
    }

  implicit val statusDataHash: Hash[StatusData] =
    Hash.by(a => (a.status, a.description))

  implicit val statusDataShow: Show[StatusData] = Show.show { data =>
    data.description match {
      case Some(d) => show"StatusData{status=${data.status}, description=$d}"
      case None    => show"StatusData{status=${data.status}}"
    }
  }

}
