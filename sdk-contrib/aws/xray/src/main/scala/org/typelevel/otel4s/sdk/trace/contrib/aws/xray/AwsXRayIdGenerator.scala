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

/*
 * Based on https://github.com/Dwolla/dwolla-otel-natchez/blob/64c3ffffe4e7c949291c4ebaff205e66c4557d1c/aws-xray-id-generator/src/main/scala/com/dwolla/tracing/AwsXrayIdGenerator.scala,
 * which was originally based on https://github.com/open-telemetry/opentelemetry-java-contrib/blob/eece7e8ef04170fb463ddf692f61d4527b50febf/aws-xray/src/main/java/io/opentelemetry/contrib/awsxray/AwsXrayIdGenerator.java
 * SPDX-License-Identifier: Apache-2.0
 */
package org.typelevel.otel4s.sdk.trace.contrib.aws.xray

import cats._
import cats.effect._
import cats.effect.std._
import cats.syntax.all._
import org.typelevel.otel4s.sdk.trace.IdGenerator
import org.typelevel.otel4s.trace.SpanContext._
import scodec.bits.ByteVector

object AwsXRayIdGenerator {

  /** Generates trace IDs that are compatible with AWS X-Ray tracing spec.
    *
    * @example
    *   {{{
    * Random.scalaUtilRandom[IO].flatMap { implicit random =>
    *   OpenTelemetrySdk
    *     .autoConfigured[IO](
    *       // register OTLP exporters configurer
    *       _.addExportersConfigurer(OtlpExportersAutoConfigure[IO])
    *       // set AWS X-Ray ID generator
    *        .addTracerProviderCustomizer((b, _) => b.withIdGenerator(AwsXRayIdGenerator[IO]))
    *     )
    *     .use { autoConfigured =>
    *       val sdk = autoConfigured.sdk
    *       ???
    *     }
    * }
    *   }}}
    *
    * @see
    *   [[https://docs.aws.amazon.com/xray/latest/devguide/xray-api-sendingdata.html#xray-api-traceids]]
    */
  def apply[F[_]: Monad: Clock: Random]: AwsXRayIdGenerator[F] = new AwsXRayIdGenerator
}

/** Generates trace IDs that are compatible with AWS X-Ray tracing spec.
  *
  * According to the X-Ray spec, the first 32 bits of the trace ID represent the Unix epoch time in seconds.
  *
  * @see
  *   [[https://docs.aws.amazon.com/xray/latest/devguide/xray-api-sendingdata.html#xray-api-traceids]]
  */
class AwsXRayIdGenerator[F[_]: Monad: Clock: Random] extends IdGenerator.Unsealed[F] {
  override def generateSpanId: F[ByteVector] =
    Random[F].nextLong
      .iterateUntil(_ != 0L)
      .map(SpanId.fromLong)

  override def generateTraceId: F[ByteVector] =
    (Clock[F].realTime.map(_.toSeconds), Random[F].nextInt.map(_ & 0xffffffffL), Random[F].nextLong).mapN {
      case (timestampSecs, hiRandom, lowRandom) =>
        TraceId.fromLongs(timestampSecs << 32 | hiRandom, lowRandom)
    }

  override private[trace] val canSkipIdValidation: Boolean = true
}
