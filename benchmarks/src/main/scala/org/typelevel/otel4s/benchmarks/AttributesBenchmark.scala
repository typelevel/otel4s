/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s.benchmarks

import io.opentelemetry.api.common.{AttributeKey => JAttributeKey}
import io.opentelemetry.api.common.{Attributes => JAttributes}
import org.openjdk.jmh.annotations._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.sdk.Attributes

import java.util.concurrent.TimeUnit

// benchmarks/Jmh/run org.typelevel.otel4s.benchmarks.AttributesBenchmark -prof gc
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@Fork(1)
@Measurement(iterations = 15, time = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
class AttributesBenchmark {
  import AttributesBenchmark._

  @Benchmark
  def java_computeHashCode(): Unit = {
    for (a <- jAttributes) { a.hashCode() }
  }

  @Benchmark
  def java_ofOne(): JAttributes =
    JAttributes.of(jKeys.head, values.head)

  @Benchmark
  def java_ofFive(): JAttributes =
    JAttributes.of(
      jKeys(0),
      values(0),
      jKeys(1),
      values(1),
      jKeys(2),
      values(2),
      jKeys(3),
      values(3),
      jKeys(4),
      values(4)
    )

  @Benchmark
  def java_builderTenItem(): JAttributes = {
    val builder = JAttributes.builder()
    for (i <- 0 until 10) {
      builder.put(jKeys(i), values(i))
    }
    builder.build()
  }

  @Benchmark
  def otel4s_computeHashCode(): Unit = {
    for (a <- attributes) {
      a.hashCode()
    }
  }

  @Benchmark
  def otel4s_ofOne(): Attributes =
    Attributes(Attribute(keys.head, values.head))

  @Benchmark
  def otel4s_ofFive(): Attributes =
    Attributes(
      Attribute(keys(0), values(0)),
      Attribute(keys(1), values(1)),
      Attribute(keys(2), values(2)),
      Attribute(keys(3), values(3)),
      Attribute(keys(4), values(4))
    )

  @Benchmark
  def otel4s_builderTenItem(): Attributes = {
    val builder = Attributes.newBuilder
    for (i <- 0 until 10) {
      builder.addOne(keys(i), values(i))
    }
    builder.result()
  }
}

object AttributesBenchmark {
  private val values = List.tabulate(10)(i => s"value$i")

  private val jKeys = List.tabulate(10)(i => JAttributeKey.stringKey(s"key$i"))
  private val jAttributes = List.tabulate(10) { i =>
    val size = i + 1
    val pairs = jKeys.take(size).zip(values.take(size))

    pairs
      .foldLeft(JAttributes.builder()) { case (builder, (key, value)) =>
        builder.put(key, value)
      }
      .build()
  }

  private val keys = List.tabulate(10)(i => AttributeKey.string(s"key$i"))
  private val attributes = List.tabulate(10) { i =>
    val size = i + 1
    val pairs = keys.take(size).zip(values.take(size))

    pairs
      .foldLeft(Attributes.newBuilder) { case (builder, (key, value)) =>
        builder.addOne(key, value)
      }
      .result()
  }
}
