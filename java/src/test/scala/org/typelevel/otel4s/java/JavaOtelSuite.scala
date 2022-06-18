package org.typelevel.otel4s.java

import cats.effect.IO
import io.opentelemetry.sdk.metrics._
import munit.CatsEffectSuite
import org.typelevel.otel4s.testkit._
import org.typelevel.otel4s.{Attribute, AttributeKey}

import scala.jdk.CollectionConverters._

class JavaOtelSuite extends CatsEffectSuite {

  test("Counter record a proper data") {
    for {
      sdk <- IO.delay(makeSdk)
      otel4s <- IO.delay(OtelJava.forSync[IO](sdk.sdk))
      meter <- otel4s.meterProvider
        .meter("java.otel.suite")
        .withVersion("1.0")
        .withSchemaUrl("https://localhost:8080")
        .get

      counter <- meter
        .counter("counter")
        .withUnit("unit")
        .withDescription("description")
        .create

      _ <- counter.add(
        1L,
        Attribute(AttributeKey.string("string-attribute"), "value")
      )

      metrics <- sdk.metrics
    } yield {
      val resourceAttributes = List(
        Attribute(AttributeKey.string("service.name"), "unknown_service:java"),
        Attribute(AttributeKey.string("telemetry.sdk.language"), "java"),
        Attribute(AttributeKey.string("telemetry.sdk.name"), "opentelemetry"),
        Attribute(AttributeKey.string("telemetry.sdk.version"), "1.15.0")
      )

      val scope = InstrumentationScope(
        name = "java.otel.suite",
        version = Some("1.0"),
        schemaUrl = Some("https://localhost:8080")
      )

      assertEquals(metrics.map(_.name), List("counter"))
      assertEquals(metrics.map(_.description), List(Some("description")))
      assertEquals(metrics.map(_.unit), List(Some("unit")))
      assertEquals(
        metrics.map(_.resource),
        List(MetricResource(None, resourceAttributes))
      )
      assertEquals(metrics.map(_.scope), List(scope))
      assertEquals[List[Any], List[Any]](
        metrics.map(_.data.points.map(_.value)),
        List(List(1L))
      )
    }
  }

  private def makeSdk: Sdk[IO] = {
    def customize(builder: SdkMeterProviderBuilder): SdkMeterProviderBuilder =
      builder.registerView(
        InstrumentSelector.builder().setType(InstrumentType.HISTOGRAM).build(),
        View
          .builder()
          .setAggregation(
            Aggregation.explicitBucketHistogram(
              HistogramBuckets.map(Double.box).asJava
            )
          )
          .build()
      )

    Sdk.create[IO](customize)
  }

  private val HistogramBuckets: List[Double] =
    List(0.01, 1, 100)

}
