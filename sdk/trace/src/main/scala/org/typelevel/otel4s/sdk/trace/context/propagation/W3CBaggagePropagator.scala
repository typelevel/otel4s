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

package org.typelevel.otel4s.sdk.trace.context.propagation

import cats.syntax.foldable._
import org.typelevel.otel4s.baggage.Baggage
import org.typelevel.otel4s.context.propagation.TextMapGetter
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.context.propagation.TextMapUpdater
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.SdkContextKeys

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** A W3C baggage propagator.
  *
  * @see
  *   [[https://www.w3.org/TR/baggage/]]
  */
private final class W3CBaggagePropagator extends TextMapPropagator[Context] {

  import W3CBaggagePropagator.Const
  import W3CBaggagePropagator.Headers

  val fields: List[String] = List(Headers.Baggage)

  def extract[A: TextMapGetter](ctx: Context, carrier: A): Context =
    TextMapGetter[A].get(carrier, Headers.Baggage) match {
      case Some(headerContent) =>
        val baggage = decode(headerContent)

        if (baggage.isEmpty) ctx
        else ctx.updated(SdkContextKeys.BaggageKey, baggage)

      case None =>
        ctx
    }

  def inject[A: TextMapUpdater](ctx: Context, carrier: A): A =
    ctx.get(SdkContextKeys.BaggageKey) match {
      case Some(baggage) =>
        val headerContent = encode(baggage)

        if (headerContent.nonEmpty)
          TextMapUpdater[A].updated(carrier, Headers.Baggage, headerContent)
        else
          carrier

      case None =>
        carrier
    }

  private def encode(baggage: Baggage): String =
    baggage.asMap
      .collect {
        case (key, entry) if isValidKey(key) =>
          val meta = entry.metadata
            .filter(_.value.nonEmpty)
            .foldMap(m => Const.MetadataDelimiter + urlEncode(m.value))

          key + Const.ValueDelimiter + urlEncode(entry.value) + meta
      }
      .mkString(",")

  private def decode(headerContent: String): Baggage =
    headerContent.split(Const.Delimiter).foldLeft(Baggage.empty) {
      case (baggage, entry) =>
        val parts = entry.split(Const.ValueDelimiter, 2)
        if (parts.length == 2) {
          val key = parts(0).trim
          val value = parts(1).trim

          if (isValidKey(key)) {
            val entryParts = value.split(Const.MetadataDelimiter, 2)
            if (entryParts.nonEmpty) {
              val value = entryParts(0).trim
              if (isValidValue(value)) {
                val decoded = urlDecode(value)
                val meta = entryParts
                  .lift(1)
                  .map(_.trim)
                  .filter(_.nonEmpty)
                  .map(urlDecode)
                baggage.updated(key, decoded, meta)
              } else {
                baggage
              }
            } else {
              baggage
            }
          } else {
            baggage
          }
        } else {
          baggage
        }
    }

  private def urlEncode(input: String): String =
    URLEncoder.encode(input, Const.Charset)

  private def urlDecode(input: String): String =
    URLDecoder.decode(input, Const.Charset)

  private def isValidKey(key: String): Boolean =
    key.nonEmpty && key.forall(Const.AllowedKeyChars)

  private def isValidValue(value: String): Boolean =
    value.nonEmpty && value.forall(Const.AllowedValueChars)

  override def toString: String = "W3CBaggagePropagator"
}

object W3CBaggagePropagator {

  private val Default = new W3CBaggagePropagator

  private object Headers {
    val Baggage = "baggage"
  }

  private object Const {
    val Delimiter = ","
    val ValueDelimiter = "="
    val MetadataDelimiter = ";"
    val Charset = StandardCharsets.UTF_8.name()

    // see https://datatracker.ietf.org/doc/html/rfc7230#section-3.2.6
    val AllowedKeyChars: Set[Char] =
      Set('!', '#', '$', '%', '&', '\'', '*', '+', '-', '.', '^', '_', '`', '|',
        '~') ++
        ('0' to '9') ++
        ('a' to 'z') ++
        ('A' to 'Z')

    // see https://w3c.github.io/baggage/#definition
    val AllowedValueChars: Set[Char] =
      Set(0x21.toChar) ++ // '!'
        (0x23.toChar to 0x2b.toChar) ++ // #$%&'()*+
        (0x2d.toChar to 0x3a.toChar) ++ // -./0123456789:
        (0x3c.toChar to 0x5b.toChar) ++ // <=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[
        (0x5d.toChar to 0x7e.toChar) //    ]^_`abcdefghijklmnopqrstuvwxyz{|}~

  }

  /** Returns an instance of the W3CBaggagePropagator.
    */
  def default: TextMapPropagator[Context] = Default

}
