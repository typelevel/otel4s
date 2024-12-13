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

package org.typelevel.otel4s.sdk.exporter.otlp

import cats.Show

sealed trait PayloadCompression {
  override def toString: String =
    Show[PayloadCompression].show(this)
}

object PayloadCompression {

  def gzip: PayloadCompression =
    Gzip

  def none: PayloadCompression =
    NoCompression

  implicit val payloadCompressionSHow: Show[PayloadCompression] =
    Show {
      case Gzip          => "gzip"
      case NoCompression => "none"
    }

  private[otlp] case object Gzip extends PayloadCompression
  private[otlp] case object NoCompression extends PayloadCompression

}
