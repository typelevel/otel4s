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
import org.typelevel.otel4s.trace.Status

/** Defines the status of a Span by providing a standard Status in
  * conjunction with an optional descriptive message.
  */
sealed trait StatusData {

  /** The status code
    */
  def status: Status

  /** The description of this status for human consumption
    */
  def description: String

  override final def hashCode(): Int =
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

  private final case class StatusDataImpl(
      status: Status,
      description: String
  ) extends StatusData

  /** Indicates the successfully completed operation,validated by an application
    * developer or operator.
    */
  val Ok: StatusData = StatusDataImpl(Status.Ok, "")

  /** The default state.
    */
  val Unset: StatusData = StatusDataImpl(Status.Unset, "")

  /** Indicates an occurred error.
    */
  val Error: StatusData = StatusDataImpl(Status.Error, "")

  def create(status: Status): StatusData =
    status match {
      case Status.Ok    => Ok
      case Status.Unset => Unset
      case Status.Error => Error
    }

  def create(status: Status, description: String): StatusData =
    if (description.isEmpty) create(status)
    else StatusDataImpl(status, description)

  implicit val statusDataHash: Hash[StatusData] =
    Hash.by(a => (a.status, a.description))

  implicit val statusDataShow: Show[StatusData] = Show.show { data =>
    show"StatusData{status=${data.status}, description=${data.description}}"
  }

}
