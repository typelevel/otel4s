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
package sdk
package exporter.otlp

import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Gen.listOf
import org.scalacheck.Gen.nonEmptyListOf
import org.typelevel.otel4s.sdk.common.InstrumentationScopeInfo
import org.typelevel.otel4s.sdk.trace.data._
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.Status
import org.typelevel.otel4s.trace.TraceFlags
import scodec.bits.ByteVector

// copy-pasted from the sdk-test
object arbitrary extends ArbitraryInstances
trait ArbitraryInstances {

  val nonEmptyString: Gen[String] =
    Arbitrary.arbitrary[String].suchThat(_.nonEmpty)

  implicit val booleanAttribute: Gen[Attribute[Boolean]] = for {
    b <- Arbitrary.arbitrary[Boolean]
    k <- nonEmptyString
  } yield Attribute(k, b)

  implicit val stringAttribute: Gen[Attribute[String]] = for {
    s <- nonEmptyString
    k <- nonEmptyString
  } yield Attribute(k, s)

  implicit val longAttribute: Gen[Attribute[Long]] = for {
    l <- Arbitrary.arbitrary[Long]
    k <- nonEmptyString
  } yield Attribute(k, l)

  implicit val doubleAttribute: Gen[Attribute[Double]] = for {
    d <- Arbitrary.arbitrary[Double]
    k <- nonEmptyString
  } yield Attribute(k, d)

  implicit val stringListAttribute: Gen[Attribute[List[String]]] = for {
    l <- nonEmptyListOf(nonEmptyString)
    k <- nonEmptyString
  } yield Attribute(k, l)

  implicit val booleanListAttribute: Gen[Attribute[List[Boolean]]] = for {
    l <- nonEmptyListOf(Arbitrary.arbitrary[Boolean])
    k <- nonEmptyString
  } yield Attribute(k, l)

  implicit val longListAttribute: Gen[Attribute[List[Long]]] = for {
    l <- nonEmptyListOf(Arbitrary.arbitrary[Long])
    k <- nonEmptyString
  } yield Attribute(k, l)

  implicit val doubleListAttribute: Gen[Attribute[List[Double]]] = for {
    l <- nonEmptyListOf(Arbitrary.arbitrary[Double])
    k <- nonEmptyString
  } yield Attribute(k, l)

  implicit val attribute: Arbitrary[Attribute[_]] = Arbitrary(
    Gen.oneOf(
      booleanAttribute,
      stringAttribute,
      longAttribute,
      doubleAttribute,
      stringListAttribute,
      booleanListAttribute,
      longListAttribute,
      doubleListAttribute
    )
  )

  implicit val attributes: Arbitrary[Attributes] = Arbitrary(
    listOf(attribute.arbitrary).map(Attributes(_: _*))
  )

  implicit val resource: Arbitrary[Resource] =
    Arbitrary(
      for {
        attrs <- attributes.arbitrary
        schemaUrl <- Gen.option(nonEmptyString)
      } yield Resource(attrs, schemaUrl)
    )

  implicit val instrumentationScopeInfo: Arbitrary[InstrumentationScopeInfo] =
    Arbitrary(
      for {
        name <- nonEmptyString
        version <- Gen.option(nonEmptyString)
        schemaUrl <- Gen.option(nonEmptyString)
        attributes <- attributes.arbitrary
      } yield InstrumentationScopeInfo.create(
        name,
        version,
        schemaUrl,
        attributes
      )
    )

  implicit val spanKind: Arbitrary[SpanKind] =
    Arbitrary(
      Gen.oneOf(
        SpanKind.Internal,
        SpanKind.Server,
        SpanKind.Client,
        SpanKind.Producer,
        SpanKind.Consumer
      )
    )

  implicit val status: Arbitrary[Status] =
    Arbitrary(Gen.oneOf(Status.Unset, Status.Unset, Status.Ok))

  implicit val spanContext: Arbitrary[SpanContext] =
    Arbitrary(
      for {
        traceId <- Gen.stringOfN(32, Gen.hexChar)
        spanId <- Gen.stringOfN(16, Gen.hexChar)
        traceFlags <- Gen.oneOf(TraceFlags.Default, TraceFlags.Sampled)
        remote <- Arbitrary.arbitrary[Boolean]
      } yield SpanContext.create(
        traceId = ByteVector.fromValidHex(traceId),
        spanId = ByteVector.fromValidHex(spanId),
        traceFlags = traceFlags,
        remote = remote
      )
    )

  implicit val statusData: Arbitrary[StatusData] =
    Arbitrary(
      for {
        status <- Arbitrary.arbitrary[Status]
        description <- Arbitrary.arbitrary[String]
      } yield StatusData.create(status, description)
    )

  implicit val eventData: Arbitrary[EventData] =
    Arbitrary(
      for {
        name <- nonEmptyString
        epochNanos <- Gen.posNum[Long]
        attributes <- Arbitrary.arbitrary[Attributes]
      } yield EventData.general(name, epochNanos, attributes)
    )

  implicit val linkData: Arbitrary[LinkData] =
    Arbitrary(
      for {
        spanContext <- Arbitrary.arbitrary[SpanContext]
        attributes <- Arbitrary.arbitrary[Attributes]
      } yield LinkData.create(spanContext, attributes)
    )

  implicit val spanData: Arbitrary[SpanData] =
    Arbitrary(
      for {
        name <- nonEmptyString
        kind <- Arbitrary.arbitrary[SpanKind]
        context <- Arbitrary.arbitrary[SpanContext]
        parentSpanContext <- Gen.option(Arbitrary.arbitrary[SpanContext])
        status <- Arbitrary.arbitrary[StatusData]
        startEpochNanos <- Gen.posNum[Long]
        attributes <- Arbitrary.arbitrary[Attributes]
        events <- Arbitrary.arbitrary[List[EventData]]
        links <- Arbitrary.arbitrary[List[LinkData]]
        endEpochNanos <- Gen.posNum[Long]
        hasEnded <- Arbitrary.arbitrary[Boolean]
        totalRecordedEvents <- Gen.posNum[Int]
        totalRecordedLinks <- Gen.posNum[Int]
        totalAttributeCount <- Gen.posNum[Int]
        instrumentationScope <- Arbitrary.arbitrary[InstrumentationScopeInfo]
        resource <- Arbitrary.arbitrary[Resource]
      } yield SpanData.create(
        name = name,
        kind = kind,
        spanContext = context,
        parentSpanContext = parentSpanContext,
        status = status,
        startEpochNanos = startEpochNanos,
        attributes = attributes,
        events = events,
        links = links,
        endEpochNanos = endEpochNanos,
        hasEnded = hasEnded,
        totalRecordedEvents = totalRecordedEvents,
        totalRecordedLinks = totalRecordedLinks,
        totalAttributeCount = totalAttributeCount,
        instrumentationScopeInfo = instrumentationScope,
        resource = resource
      )
    )

}
