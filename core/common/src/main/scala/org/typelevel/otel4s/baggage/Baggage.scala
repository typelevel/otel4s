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

package org.typelevel.otel4s.baggage

import cats.Hash
import cats.Show
import cats.syntax.show._

/** A baggage can be used to attach log messages or debugging information to the context.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/baggage/api/]]
  *
  * @see
  *   [[https://opentelemetry.io/docs/concepts/signals/baggage/]]
  *
  * @see
  *   [[https://www.w3.org/TR/baggage/]]
  */
sealed trait Baggage {

  /** Returns the entry to which the specified key is mapped, or `None` if this map contains no mapping for the key.
    */
  def get(key: String): Option[Baggage.Entry]

  /** Adds or updates the entry that has the given `key` if it is present.
    *
    * @param key
    *   the key for the entry
    *
    * @param value
    *   the value for the entry to associate with the key
    *
    * @param metadata
    *   the optional metadata to associate with the key
    */
  def updated(key: String, value: String, metadata: Option[String]): Baggage

  /** Adds or updates the entry that has the given `key` if it is present.
    *
    * @param key
    *   the key for the entry
    *
    * @param value
    *   the value for the entry to associate with the key
    */
  final def updated(key: String, value: String): Baggage =
    updated(key, value, None)

  /** Removes the entry that has the given `key` if it is present.
    *
    * @param key
    *   the key for the entry to be removed
    */
  def removed(key: String): Baggage

  /** Returns the number of entries in this state. */
  def size: Int

  /** Returns whether this baggage is empty, containing no entries. */
  def isEmpty: Boolean

  /** Returns a map representation of this baggage. */
  def asMap: Map[String, Baggage.Entry]

  override final def hashCode(): Int =
    Hash[Baggage].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: Baggage => Hash[Baggage].eqv(this, other)
      case _              => false
    }

  override final def toString: String =
    Show[Baggage].show(this)

}

object Baggage {

  private val Empty: Baggage = Impl(Map.empty)

  /** An opaque wrapper for a string.
    */
  sealed trait Metadata {
    def value: String

    override final def hashCode(): Int =
      Hash[Metadata].hash(this)

    override final def equals(obj: Any): Boolean =
      obj match {
        case other: Metadata => Hash[Metadata].eqv(this, other)
        case _               => false
      }

    override final def toString: String =
      Show[Metadata].show(this)
  }

  object Metadata {
    def apply(value: String): Metadata =
      Impl(value)

    implicit val metadataHash: Hash[Metadata] = Hash.by(_.value)
    implicit val metadataShow: Show[Metadata] = Show.show(_.value)

    private final case class Impl(value: String) extends Metadata
  }

  /** A entry the [[Baggage]] holds associated with a key.
    */
  sealed trait Entry {

    /** The value of the entry.
      */
    def value: String

    /** The optional metadata of the entry.
      */
    def metadata: Option[Metadata]

    override final def hashCode(): Int =
      Hash[Entry].hash(this)

    override final def equals(obj: Any): Boolean =
      obj match {
        case other: Entry => Hash[Entry].eqv(this, other)
        case _            => false
      }

    override final def toString: String =
      Show[Entry].show(this)
  }

  object Entry {

    def apply(value: String, metadata: Option[Metadata]): Entry =
      Impl(value, metadata)

    implicit val entryHash: Hash[Entry] = Hash.by(e => (e.value, e.metadata))
    implicit val entryShow: Show[Entry] = Show.show { entry =>
      entry.metadata.foldLeft(entry.value)((v, m) => v + ";" + m.value)
    }

    private final case class Impl(
        value: String,
        metadata: Option[Metadata]
    ) extends Entry

  }

  /** An empty [[Baggage]].
    */
  def empty: Baggage = Empty

  implicit val baggageHash: Hash[Baggage] = Hash.by(_.asMap)

  implicit val baggageShow: Show[Baggage] = Show.show { baggage =>
    val entries = baggage.asMap
      .map { case (key, value) => show"$key=$value" }
      .mkString(",")

    s"Baggage{$entries}"
  }

  private final case class Impl(asMap: Map[String, Entry]) extends Baggage {
    def get(key: String): Option[Entry] =
      asMap.get(key)

    def updated(key: String, value: String, metadata: Option[String]): Baggage =
      copy(asMap = asMap.updated(key, Entry(value, metadata.map(Metadata(_)))))

    def removed(key: String): Baggage =
      copy(asMap = asMap.removed(key))

    def size: Int =
      asMap.size

    def isEmpty: Boolean =
      asMap.isEmpty
  }
}
