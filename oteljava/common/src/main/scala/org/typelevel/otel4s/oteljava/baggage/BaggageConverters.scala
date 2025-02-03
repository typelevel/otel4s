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

package org.typelevel.otel4s.oteljava.baggage

import io.opentelemetry.api.baggage.{Baggage => JBaggage}
import io.opentelemetry.api.baggage.BaggageEntry
import io.opentelemetry.api.baggage.BaggageEntryMetadata
import org.typelevel.otel4s.baggage.Baggage

/** This object provides extension methods that convert between Scala and Java `Baggage`s, Java `BaggageEntry`s, and
  * Scala `Baggage.Entry`s using `toScala` and `toJava` extension methods.
  *
  * {{{
  *   import io.opentelemetry.api.baggage.{Baggage => JBaggage}
  *   import org.typelevel.baggage.Baggage
  *   import org.typelevel.otel4s.oteljava.baggage.BaggageConverters._
  *
  *   val baggage: Baggage =
  *     JBaggage.builder()
  *       .put("key", "value")
  *       .build()
  *       .toScala
  * }}}
  *
  * The conversions do not return wrappers.
  */
object BaggageConverters {

  implicit final class BaggageHasToJava(private val baggage: Baggage) extends AnyVal {

    /** Converts a Scala `Baggage` to a Java `Baggage`. */
    def toJava: JBaggage = Explicit.toJava(baggage)
  }

  implicit final class BaggageEntryHasToScala(private val entry: BaggageEntry) extends AnyVal {

    /** Converts a Java `BaggageEntry` to a Scala `Baggage.Entry`. */
    def toScala: Baggage.Entry = Explicit.toScala(entry)
  }

  implicit final class BaggageHasToScala(private val baggage: JBaggage) extends AnyVal {

    /** Converts a Java `Baggage` to a Scala `Baggage`. */
    def toScala: Baggage = Explicit.toScala(baggage)
  }

  private[this] object Explicit {
    def toJava(metadata: Option[Baggage.Metadata]): BaggageEntryMetadata =
      metadata.fold(BaggageEntryMetadata.empty()) { m =>
        BaggageEntryMetadata.create(m.value)
      }

    def toJava(baggage: Baggage): JBaggage = {
      val builder = JBaggage.builder()
      baggage.asMap.foreach { case (key, entry) =>
        builder.put(key, entry.value, toJava(entry.metadata))
      }
      builder.build()
    }

    def toScala(metadata: BaggageEntryMetadata): Option[Baggage.Metadata] = {
      val value = metadata.getValue
      Option.unless(value.isEmpty)(Baggage.Metadata(value))
    }

    def toScala(entry: BaggageEntry): Baggage.Entry =
      Baggage.Entry(entry.getValue, toScala(entry.getMetadata))

    def toScala(baggage: JBaggage): Baggage = {
      var res = Baggage.empty
      baggage.forEach { (key, entry) =>
        val metadata = entry.getMetadata.getValue
        res = res.updated(key, entry.getValue, Option.unless(metadata.isEmpty)(metadata))
      }
      res
    }
  }
}
