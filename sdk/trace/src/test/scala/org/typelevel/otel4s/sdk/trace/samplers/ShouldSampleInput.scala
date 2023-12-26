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

package org.typelevel.otel4s
package sdk.trace
package samplers

import org.scalacheck.Gen
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import scodec.bits.ByteVector

final case class ShouldSampleInput(
    parentContext: Option[SpanContext],
    traceId: ByteVector,
    name: String,
    spanKind: SpanKind,
    attributes: Attributes,
    parentLinks: List[LinkData]
)

object ShouldSampleInput {

  val shouldSampleInputGen: Gen[ShouldSampleInput] =
    for {
      parentContext <- Gen.option(Gens.spanContext)
      traceId <- Gens.traceId
      name <- Gen.alphaNumStr
      kind <- Gens.spanKind
      attributes <- Gens.attributes
      parentLinks <- Gen.listOf(Gens.linkData)
    } yield ShouldSampleInput(
      parentContext,
      traceId,
      name,
      kind,
      attributes,
      parentLinks
    )

}
