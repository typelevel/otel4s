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

import io.opentelemetry.api.trace.{TraceState => JTraceState}
import org.openjdk.jmh.annotations._
import org.typelevel.otel4s.trace.TraceState

import java.util.concurrent.TimeUnit

// benchmarks/Jmh/run org.typelevel.otel4s.benchmarks.TraceStateBenchmark -prof gc
@BenchmarkMode(Array(Mode.AverageTime))
@Fork(1)
@Measurement(iterations = 15, time = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
class TraceStateBenchmark {

  @Benchmark
  def java_oneItem(): JTraceState = {
    val builder = JTraceState.builder()
    builder.put("key1", "val")
    builder.build()
  }

  @Benchmark
  def java_fiveItems(): JTraceState = {
    val builder = JTraceState.builder()
    builder.put("key1", "val")
    builder.put("key2", "val")
    builder.put("key3", "val")
    builder.put("key4", "val")
    builder.put("key5", "val")
    builder.build()
  }

  @Benchmark
  def java_fiveItemsWithRemoval(): JTraceState = {
    val builder = JTraceState.builder()
    builder.put("key1", "val")
    builder.put("key2", "val")
    builder.put("key3", "val")
    builder.remove("key2")
    builder.remove("key3")
    builder.put("key2", "val")
    builder.put("key3", "val")
    builder.put("key4", "val")
    builder.put("key5", "val")
    builder.build()
  }

  @Benchmark
  def otel4s_oneItem(): TraceState = {
    TraceState.empty.updated("key1", "val")
  }

  @Benchmark
  def otel4s_fiveItems(): TraceState = {
    TraceState.empty
      .updated("key1", "val")
      .updated("key2", "val")
      .updated("key3", "val")
      .updated("key4", "val")
      .updated("key5", "val")
  }

  @Benchmark
  def otel4s_fiveItemsWithRemoval(): TraceState = {
    TraceState.empty
      .updated("key1", "val")
      .updated("key2", "val")
      .updated("key3", "val")
      .removed("key2")
      .removed("key3")
      .updated("key2", "val")
      .updated("key3", "val")
      .updated("key4", "val")
      .updated("key5", "val")
  }
}
