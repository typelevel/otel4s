package com.example

import cats.effect.IO
import io.opentelemetry.sdk.metrics._
import munit.CatsEffectSuite
import org.typelevel.otel4s.java.OtelJava
import org.typelevel.otel4s.testkit._
import org.typelevel.otel4s.{Attribute, AttributeKey}

import scala.jdk.CollectionConverters._

class JavaOtelSuite extends CatsEffectSuite {

  test("Counter record a proper data") {
    val expected = Metric(
      "counter",
      Some("description"),
      Some("unit"),
      MetricData.LongSum(List(1))
    )

    for {
      sdk <- IO.delay(makeSdk)
      otel4s <- IO.delay(OtelJava.forSync[IO](sdk.sdk))
      meter <- otel4s.meterProvider.get("java.otel.suite")

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
    } yield assertEquals(metrics, List(expected))
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
