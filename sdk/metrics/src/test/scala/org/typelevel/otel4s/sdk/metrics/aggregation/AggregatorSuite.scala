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

package org.typelevel.otel4s.sdk.metrics.aggregation

import cats.effect.IO
import cats.effect.std.Random
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Gen
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.sdk.metrics.Aggregation
import org.typelevel.otel4s.sdk.metrics.InstrumentType
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.MetricPoints
import org.typelevel.otel4s.sdk.metrics.exemplar.ExemplarFilter
import org.typelevel.otel4s.sdk.metrics.exemplar.TraceContextLookup
import org.typelevel.otel4s.sdk.metrics.internal.MetricDescriptor
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens

class AggregatorSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  private val synchronousAggregationGen: Gen[Aggregation.Synchronous] =
    Gen.oneOf(
      Gen.const(Aggregation.Default),
      Gen.const(Aggregation.Sum),
      Gen.const(Aggregation.LastValue),
      Gens.bucketBoundaries.map(Aggregation.ExplicitBucketHistogram.apply)
    )

  private val asynchronousAggregationGen: Gen[Aggregation.Asynchronous] =
    Gen.oneOf(
      Gen.const(Aggregation.Default),
      Gen.const(Aggregation.Sum),
      Gen.const(Aggregation.LastValue)
    )

  test("create an aggregator for a synchronous instrument") {
    PropF.forAllF(
      synchronousAggregationGen,
      Gens.synchronousInstrumentDescriptor,
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.aggregationTemporality
    ) { (aggregation, descriptor, resource, scope, temporality) =>
      Random.scalaUtilRandom[IO].flatMap { implicit R: Random[IO] =>
        val aggregator = Aggregator.synchronous[IO, Long](
          aggregation,
          descriptor,
          ExemplarFilter.alwaysOn,
          TraceContextLookup.noop
        )

        val expected = {
          def sum = {
            val monotonic =
              descriptor.instrumentType match {
                case InstrumentType.Counter   => true
                case InstrumentType.Histogram => true
                case _                        => false
              }

            MetricPoints.sum(Vector.empty, monotonic, temporality)
          }

          def lastValue =
            MetricPoints.gauge(Vector.empty)

          def histogram =
            MetricPoints.histogram(Vector.empty, temporality)

          val metricPoints = aggregation match {
            case Aggregation.Default =>
              descriptor.instrumentType match {
                case InstrumentType.Counter       => sum
                case InstrumentType.UpDownCounter => sum
                case InstrumentType.Histogram     => histogram
              }

            case Aggregation.Sum                        => sum
            case Aggregation.LastValue                  => lastValue
            case Aggregation.ExplicitBucketHistogram(_) => histogram
          }

          MetricData(
            resource = resource,
            scope = scope,
            name = descriptor.name.toString,
            description = descriptor.description,
            unit = descriptor.unit,
            data = metricPoints
          )
        }

        for {
          result <- aggregator.toMetricData(
            resource,
            scope,
            MetricDescriptor(None, descriptor),
            Vector.empty,
            temporality
          )
        } yield assertEquals(result, expected)
      }
    }
  }

  test("create an aggregator for an asynchronous instrument") {
    PropF.forAllF(
      asynchronousAggregationGen,
      Gens.asynchronousInstrumentDescriptor,
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.aggregationTemporality
    ) { (aggregation, descriptor, resource, scope, temporality) =>
      val aggregator = Aggregator.asynchronous[IO, Long](
        aggregation,
        descriptor
      )

      val expected = {
        def sum = {
          val monotonic =
            descriptor.instrumentType match {
              case InstrumentType.ObservableCounter => true
              case _                                => false
            }

          MetricPoints.sum(Vector.empty, monotonic, temporality)
        }

        def lastValue =
          MetricPoints.gauge(Vector.empty)

        val metricPoints = aggregation match {
          case Aggregation.Default =>
            descriptor.instrumentType match {
              case InstrumentType.ObservableCounter       => sum
              case InstrumentType.ObservableUpDownCounter => sum
              case InstrumentType.ObservableGauge         => lastValue
            }

          case Aggregation.Sum       => sum
          case Aggregation.LastValue => lastValue
        }

        MetricData(
          resource = resource,
          scope = scope,
          name = descriptor.name.toString,
          description = descriptor.description,
          unit = descriptor.unit,
          data = metricPoints
        )
      }

      for {
        result <- aggregator.toMetricData(
          resource,
          scope,
          MetricDescriptor(None, descriptor),
          Vector.empty,
          temporality
        )
      } yield assertEquals(result, expected)
    }
  }

}
