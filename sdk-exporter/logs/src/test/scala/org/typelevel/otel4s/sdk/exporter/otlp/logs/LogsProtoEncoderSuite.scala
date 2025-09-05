/*
 * Copyright 2025 Typelevel
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
package logs

import io.circe.Json
import io.circe.syntax._
import munit._
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.Test
import org.typelevel.otel4s.sdk.logs.scalacheck.Gens
import scodec.bits.ByteVector

class LogsProtoEncoderSuite extends ScalaCheckSuite {
  import LogsJsonCodecs._
  import LogsProtoEncoder._

  test("encode LogRecordData") {
    Prop.forAll(Gens.logRecordData) { logRecord =>
      val expected = Json
        .obj(
          "timeUnixNano" := logRecord.timestamp.map(_.toNanos).filter(_ > 0).map(_.toString),
          "observedTimeUnixNano" := logRecord.observedTimestamp.toNanos.toString,
          "severityNumber" := logRecord.severity.map(_.value).filter(_ > 0),
          "severityText" := logRecord.severityText.filter(_.nonEmpty),
          "body" := logRecord.body.map(v => encodeValue(v)),
          "attributes" := logRecord.attributes.elements,
          "droppedAttributesCount" := logRecord.attributes.dropped,
          "traceId" := logRecord.traceContext.map(_.traceId.toHex),
          "spanId" := logRecord.traceContext.map(_.spanId.toHex)
        )
        .dropNullValues

      assertEquals(ProtoEncoder.toJson(logRecord), expected)
    }
  }

  test("encode List[LogRecordData]") {
    Prop.forAll(Gen.listOf(Gens.logRecordData)) { logs =>
      val resourceSpans =
        logs.groupBy(_.resource).map { case (resource, resourceLogs) =>
          val scopeLogs: Iterable[Json] =
            resourceLogs
              .groupBy(_.instrumentationScope)
              .map { case (scope, logs) =>
                Json
                  .obj(
                    "scope" := scope,
                    "logRecords" := logs.map(_.asJson),
                    "schemaUrl" := scope.schemaUrl
                  )
                  .dropNullValues
              }

          Json
            .obj(
              "resource" := resource,
              "scopeLogs" := scopeLogs,
              "schemaUrl" := resource.schemaUrl
            )
            .dropNullValues
        }

      val expected =
        Json.obj("resourceLogs" := resourceSpans).dropEmptyValues

      assertEquals(
        ProtoEncoder.toJson(logs).noSpacesSortKeys,
        expected.noSpacesSortKeys
      )
    }
  }

  private def encodeValue(value: AnyValue): Json = {
    value match {
      case AnyValue.StringValueImpl(v)    => Json.obj("stringValue" := v)
      case AnyValue.BooleanValueImpl(v)   => Json.obj("boolValue" := v)
      case AnyValue.LongValueImpl(v)      => Json.obj("intValue" := v.toString)
      case AnyValue.DoubleValueImpl(v)    => Json.obj("doubleValue" := v)
      case AnyValue.ByteArrayValueImpl(v) => Json.obj("bytesValue" := ByteVector(v).toBase64)
      case AnyValue.ListValueImpl(values) =>
        val v =
          if (values.isEmpty) Json.obj()
          else Json.obj("values" := values.map(encodeValue))

        Json.obj("arrayValue" := v)

      case AnyValue.MapValueImpl(values) =>
        val v =
          if (values.isEmpty) Json.obj()
          else Json.obj("values" := values.map { case (k, v) => Json.obj("key" := k, "value" := encodeValue(v)) })

        Json.obj("kvlistValue" := v)

      case AnyValue.EmptyValueImpl =>
        Json.obj()
    }
  }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(5)
      .withMaxSize(5)

}
