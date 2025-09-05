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

package org.typelevel.otel4s.oteljava.logs

import cats.effect.IO
import io.opentelemetry.api.common.{Value => JValue}
import io.opentelemetry.api.common.KeyValue
import io.opentelemetry.api.common.ValueType
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanContext
import io.opentelemetry.api.trace.TraceFlags
import io.opentelemetry.api.trace.TraceState
import io.opentelemetry.context.{Context => JContext}
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.`export`.SimpleLogRecordProcessor
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter
import munit.CatsEffectSuite
import munit.Compare
import munit.ScalaCheckEffectSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.AnyValue
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.logs.Severity
import org.typelevel.otel4s.logs.scalacheck.Arbitraries._
import org.typelevel.otel4s.oteljava.AttributeConverters._
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.scalacheck.Arbitraries._

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.util.chaining._

class LogsSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  private implicit val timestampArbitrary: Arbitrary[FiniteDuration] =
    Arbitrary(Gen.chooseNum(1L, Long.MaxValue).map(_.nanos))

  test("build a record") {
    PropF.forAllF {
      (
          timestamp: FiniteDuration,
          observedTimestamp: FiniteDuration,
          severity: Option[Severity],
          severityText: Option[String],
          body: Option[AnyValue],
          eventName: Option[String],
          attributes: Attributes,
      ) =>
        val exporter = InMemoryLogRecordExporter.create()
        val sdk = OpenTelemetrySdk
          .builder()
          .setLoggerProvider(
            SdkLoggerProvider.builder().addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter)).build()
          )
          .build()

        val logs = Logs.fromJOpenTelemetry[IO](sdk)

        val loggerName = "test-logger"
        val loggerVersion = "1.0.0"
        val loggerSchemaUrl = "schema-url"

        val spanContext = SpanContext.create("", "", TraceFlags.getDefault, TraceState.getDefault)
        val context = Context.wrap(JContext.root().`with`(Span.wrap(spanContext)))

        for {
          logger <- logs.loggerProvider
            .logger(loggerName)
            .withVersion(loggerVersion)
            .withSchemaUrl(loggerSchemaUrl)
            .get

          _ <- logger.logRecordBuilder
            .withTimestamp(timestamp)
            .withObservedTimestamp(observedTimestamp)
            .withContext(context)
            .addAttributes(attributes)
            .pipe(b => severity.fold(b)(b.withSeverity))
            .pipe(b => severityText.fold(b)(b.withSeverityText))
            .pipe(b => body.fold(b)(b.withBody))
            .pipe(b => eventName.fold(b)(b.withEventName))
            .emit

          items <- IO.delay(exporter.getFinishedLogRecordItems.asScala.toList)
          _ <- IO.delay(exporter.reset())
        } yield {
          assertEquals(items.size, 1)
          val item = items.head

          assertEquals(item.getInstrumentationScopeInfo.getName, loggerName)
          assertEquals(item.getInstrumentationScopeInfo.getVersion, loggerVersion)
          assertEquals(item.getInstrumentationScopeInfo.getSchemaUrl, loggerSchemaUrl)

          assertEquals(item.getTimestampEpochNanos, timestamp.toNanos)
          assertEquals(item.getObservedTimestampEpochNanos, observedTimestamp.toNanos)
          assertEquals(item.getSeverity.getSeverityNumber, severity.map(_.value).getOrElse(0))
          assertEquals(item.getSeverityText, severityText.orNull)
          assertEquals(item.getEventName, eventName.orNull)
          assertEquals(item.getAttributes, attributes.toJavaAttributes)
          assertEquals(item.getSpanContext, spanContext)

          if (item.getBodyValue != null) {
            assertEquals(
              item.getBodyValue.asInstanceOf[JValue[Any]],
              body.flatMap(b => Option(toJValue(b).asInstanceOf[JValue[Any]])).orNull
            )
          }
        }
    }
  }

  private def toJValue(value: AnyValue): JValue[_] =
    value match {
      case string: AnyValue.StringValue =>
        JValue.of(string.value)

      case boolean: AnyValue.BooleanValue =>
        JValue.of(boolean.value)

      case long: AnyValue.LongValue =>
        JValue.of(long.value)

      case double: AnyValue.DoubleValue =>
        JValue.of(double.value)

      case AnyValue.ByteArrayValueImpl(bytes) =>
        JValue.of(bytes)

      case list: AnyValue.SeqValue =>
        JValue.of(list.value.map(toJValue).filter(_ != null).toList.asJava)

      case map: AnyValue.MapValue =>
        JValue.of(
          map.value.view.mapValues(value => toJValue(value)).filter(_._2 != null).toMap.asJava
        )

      case _: AnyValue.EmptyValue =>
        null
    }

  private implicit val compareJValue: Compare[JValue[Any], JValue[Any]] =
    (left, right) => {
      (left.getType, right.getType) match {
        case (ValueType.STRING, ValueType.STRING)   => left.getValue == right.getValue
        case (ValueType.BOOLEAN, ValueType.BOOLEAN) => left.getValue == right.getValue
        case (ValueType.LONG, ValueType.LONG)       => left.getValue == right.getValue
        case (ValueType.DOUBLE, ValueType.DOUBLE)   => left.getValue == right.getValue
        case (ValueType.BYTES, ValueType.BYTES)     => left.getValue == right.getValue

        case (ValueType.ARRAY, ValueType.ARRAY) =>
          val l = left.getValue.asInstanceOf[java.util.List[JValue[_]]].asScala
          val r = right.getValue.asInstanceOf[java.util.List[JValue[_]]].asScala
          l.diff(r).isEmpty

        case (ValueType.KEY_VALUE_LIST, ValueType.KEY_VALUE_LIST) =>
          val l = left.getValue.asInstanceOf[java.util.List[KeyValue]].asScala
          val r = right.getValue.asInstanceOf[java.util.List[KeyValue]].asScala
          l.diff(r).isEmpty

        case (_, _) =>
          false
      }
    }

}
