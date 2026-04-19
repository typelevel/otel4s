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

import io.opentelemetry.sdk.trace.data.SpanData

import scala.annotation.nowarn

/** Transforms OpenTelemetry's SpanData into arbitrary type `A`.
  */
@deprecated(
  "Use `finishedSpans` without a type parameter to work with OpenTelemetry Java `SpanData`, or use the expectation API for assertions.",
  "1.0.0-RC1"
)
sealed trait FromSpanData[A] {
  def from(spanData: SpanData): A
}

@nowarn("cat=deprecation")
object FromSpanData {

  @deprecated(
    "Use `finishedSpans` without a type parameter to work with OpenTelemetry Java `SpanData`, or use the expectation API for assertions.",
    "1.0.0-RC1"
  )
  def apply[A](implicit ev: FromSpanData[A]): FromSpanData[A] = ev

  implicit val toOtelJavaSpanData: FromSpanData[SpanData] =
    new FromSpanData[SpanData] {
      def from(spanData: SpanData): SpanData = spanData
    }

}
