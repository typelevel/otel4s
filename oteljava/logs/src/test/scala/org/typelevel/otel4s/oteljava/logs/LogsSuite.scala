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
import io.opentelemetry.api.common.{AttributeKey => JAttributeKey}
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
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.logs.Severity
import org.typelevel.otel4s.logs.scalacheck.Arbitraries._
import org.typelevel.otel4s.oteljava.AnyValueConverters._
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
        val logs = createLogsModule(exporter)

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
              body.flatMap(b => Option(b.toJava.asInstanceOf[JValue[Any]])).orNull
            )
          }
        }
    }
  }

  test("withException emits exception attributes") {
    val exporter = InMemoryLogRecordExporter.create()
    val logs = createLogsModule(exporter)
    val exception = new RuntimeException("error")

    for {
      logger <- logs.loggerProvider.logger("test-logger").get
      _ <- logger.logRecordBuilder.withException(exception).emit
      items <- IO.delay(exporter.getFinishedLogRecordItems.asScala.toList)
    } yield {
      assertEquals(items.size, 1)
      val attributes = items.head.getAttributes

      assertEquals(
        attributes.get(JAttributeKey.stringKey("exception.type")),
        classOf[RuntimeException].getCanonicalName
      )
      assertEquals(
        attributes.get(JAttributeKey.stringKey("exception.message")),
        "error"
      )
      assert(
        attributes.get(JAttributeKey.stringKey("exception.stacktrace")) != null
      )
    }
  }

  test("withException keeps user-supplied exception attributes") {
    val exporter = InMemoryLogRecordExporter.create()
    val logs = createLogsModule(exporter)

    for {
      logger <- logs.loggerProvider.logger("test-logger").get
      _ <- logger.logRecordBuilder
        .addAttribute(Attribute("exception.message", "custom message"))
        .withException(new RuntimeException("error"))
        .emit
      items <- IO.delay(exporter.getFinishedLogRecordItems.asScala.toList)
    } yield {
      assertEquals(items.size, 1)
      val attributes = items.head.getAttributes

      assertEquals(
        attributes.get(JAttributeKey.stringKey("exception.type")),
        classOf[RuntimeException].getCanonicalName
      )
      assertEquals(
        attributes.get(JAttributeKey.stringKey("exception.message")),
        "custom message"
      )
      assert(
        attributes.get(JAttributeKey.stringKey("exception.stacktrace")) != null
      )
    }
  }

  test("addAttribute converts AnyValue attributes like addAttributes") {
    val exporter = InMemoryLogRecordExporter.create()
    val logs = createLogsModule(exporter)
    val attribute =
      Attribute("payload", AnyValue.map(Map("nested" -> AnyValue.seq(Seq(AnyValue.string("x"))))): AnyValue)

    for {
      logger <- logs.loggerProvider.logger("test-logger").get
      _ <- logger.logRecordBuilder.addAttribute(attribute).emit
      single <- IO.delay(exporter.getFinishedLogRecordItems.asScala.toList)
      _ <- IO.delay(exporter.reset())
      _ <- logger.logRecordBuilder.addAttributes(attribute).emit
      bulk <- IO.delay(exporter.getFinishedLogRecordItems.asScala.toList)
    } yield {
      assertEquals(single.size, 1)
      assertEquals(bulk.size, 1)

      val key = JAttributeKey.valueKey(attribute.key.name)
      val expected = attribute.value.toJava.asInstanceOf[JValue[Any]]

      assertEquals(single.head.getAttributes.get(key).asInstanceOf[JValue[Any]], expected)
      assertEquals(
        single.head.getAttributes.get(key).asInstanceOf[JValue[Any]],
        bulk.head.getAttributes.get(key).asInstanceOf[JValue[Any]]
      )
    }
  }

  private def createLogsModule(exporter: InMemoryLogRecordExporter): Logs[IO] = {
    val sdk = OpenTelemetrySdk
      .builder()
      .setLoggerProvider(
        SdkLoggerProvider.builder().addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter)).build()
      )
      .build()

    Logs.fromJOpenTelemetry[IO](sdk)
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

        case (ValueType.EMPTY, ValueType.EMPTY) =>
          true

        case (_, _) =>
          false
      }
    }

}
