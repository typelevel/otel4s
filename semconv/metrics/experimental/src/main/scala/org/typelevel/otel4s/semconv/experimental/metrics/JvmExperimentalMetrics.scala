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

package org.typelevel.otel4s
package semconv
package experimental
package metrics

import cats.effect.Resource
import org.typelevel.otel4s.metrics._
import org.typelevel.otel4s.semconv.attributes._
import org.typelevel.otel4s.semconv.experimental.attributes._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object JvmExperimentalMetrics {

  @annotation.nowarn("cat=deprecation")
  val specs: List[MetricSpec] = List(
    BufferCount,
    BufferMemoryLimit,
    BufferMemoryUsage,
    BufferMemoryUsed,
    ClassCount,
    ClassLoaded,
    ClassUnloaded,
    CpuCount,
    CpuRecentUtilization,
    CpuTime,
    FileDescriptorCount,
    GcDuration,
    MemoryCommitted,
    MemoryInit,
    MemoryLimit,
    MemoryUsed,
    MemoryUsedAfterLastGc,
    SystemCpuLoad1m,
    SystemCpuUtilization,
    ThreadCount,
  )

  /** Number of buffers in the pool.
    */
  object BufferCount extends MetricSpec.Unsealed {

    val name: String = "jvm.buffer.count"
    val description: String = "Number of buffers in the pool."
    val unit: String = "{buffer}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Name of the buffer pool.
        *
        * @note
        *   <p> Pool names are generally obtained via <a
        *   href="https://docs.oracle.com/en/java/javase/11/docs/api/java.management/java/lang/management/BufferPoolMXBean.html#getName()">BufferPoolMXBean#getName()</a>.
        */
      val jvmBufferPoolName: AttributeSpec[String] =
        AttributeSpec(
          JvmExperimentalAttributes.JvmBufferPoolName,
          List(
            "mapped",
            "direct",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          jvmBufferPoolName,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Measure of total memory capacity of buffers.
    */
  object BufferMemoryLimit extends MetricSpec.Unsealed {

    val name: String = "jvm.buffer.memory.limit"
    val description: String = "Measure of total memory capacity of buffers."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Name of the buffer pool.
        *
        * @note
        *   <p> Pool names are generally obtained via <a
        *   href="https://docs.oracle.com/en/java/javase/11/docs/api/java.management/java/lang/management/BufferPoolMXBean.html#getName()">BufferPoolMXBean#getName()</a>.
        */
      val jvmBufferPoolName: AttributeSpec[String] =
        AttributeSpec(
          JvmExperimentalAttributes.JvmBufferPoolName,
          List(
            "mapped",
            "direct",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          jvmBufferPoolName,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Deprecated, use `jvm.buffer.memory.used` instead.
    */
  @deprecated("Replaced by `jvm.buffer.memory.used`.", "")
  object BufferMemoryUsage extends MetricSpec.Unsealed {

    val name: String = "jvm.buffer.memory.usage"
    val description: String = "Deprecated, use `jvm.buffer.memory.used` instead."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Name of the buffer pool.
        *
        * @note
        *   <p> Pool names are generally obtained via <a
        *   href="https://docs.oracle.com/en/java/javase/11/docs/api/java.management/java/lang/management/BufferPoolMXBean.html#getName()">BufferPoolMXBean#getName()</a>.
        */
      val jvmBufferPoolName: AttributeSpec[String] =
        AttributeSpec(
          JvmExperimentalAttributes.JvmBufferPoolName,
          List(
            "mapped",
            "direct",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          jvmBufferPoolName,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Measure of memory used by buffers.
    */
  object BufferMemoryUsed extends MetricSpec.Unsealed {

    val name: String = "jvm.buffer.memory.used"
    val description: String = "Measure of memory used by buffers."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Name of the buffer pool.
        *
        * @note
        *   <p> Pool names are generally obtained via <a
        *   href="https://docs.oracle.com/en/java/javase/11/docs/api/java.management/java/lang/management/BufferPoolMXBean.html#getName()">BufferPoolMXBean#getName()</a>.
        */
      val jvmBufferPoolName: AttributeSpec[String] =
        AttributeSpec(
          JvmExperimentalAttributes.JvmBufferPoolName,
          List(
            "mapped",
            "direct",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          jvmBufferPoolName,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Number of classes currently loaded.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.ClassCount` instead.", "")
  object ClassCount extends MetricSpec.Unsealed {

    val name: String = "jvm.class.count"
    val description: String = "Number of classes currently loaded."
    val unit: String = "{class}"
    val stability: Stability = Stability.stable
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Number of classes loaded since JVM start.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.ClassLoaded` instead.", "")
  object ClassLoaded extends MetricSpec.Unsealed {

    val name: String = "jvm.class.loaded"
    val description: String = "Number of classes loaded since JVM start."
    val unit: String = "{class}"
    val stability: Stability = Stability.stable
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Number of classes unloaded since JVM start.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.ClassUnloaded` instead.", "")
  object ClassUnloaded extends MetricSpec.Unsealed {

    val name: String = "jvm.class.unloaded"
    val description: String = "Number of classes unloaded since JVM start."
    val unit: String = "{class}"
    val stability: Stability = Stability.stable
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Number of processors available to the Java virtual machine.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.CpuCount` instead.", "")
  object CpuCount extends MetricSpec.Unsealed {

    val name: String = "jvm.cpu.count"
    val description: String = "Number of processors available to the Java virtual machine."
    val unit: String = "{cpu}"
    val stability: Stability = Stability.stable
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Recent CPU utilization for the process as reported by the JVM.
    *
    * @note
    *   <p> The value range is [0.0,1.0]. This utilization is not defined as being for the specific interval since last
    *   measurement (unlike `system.cpu.utilization`). <a
    *   href="https://docs.oracle.com/en/java/javase/17/docs/api/jdk.management/com/sun/management/OperatingSystemMXBean.html#getProcessCpuLoad()">Reference</a>.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.CpuRecentUtilization` instead.", "")
  object CpuRecentUtilization extends MetricSpec.Unsealed {

    val name: String = "jvm.cpu.recent_utilization"
    val description: String = "Recent CPU utilization for the process as reported by the JVM."
    val unit: String = "1"
    val stability: Stability = Stability.stable
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** CPU time used by the process as reported by the JVM.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.CpuTime` instead.", "")
  object CpuTime extends MetricSpec.Unsealed {

    val name: String = "jvm.cpu.time"
    val description: String = "CPU time used by the process as reported by the JVM."
    val unit: String = "s"
    val stability: Stability = Stability.stable
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Number of open file descriptors as reported by the JVM.
    */
  object FileDescriptorCount extends MetricSpec.Unsealed {

    val name: String = "jvm.file_descriptor.count"
    val description: String = "Number of open file descriptors as reported by the JVM."
    val unit: String = "{file_descriptor}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Duration of JVM garbage collection actions.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.GcDuration` instead.", "")
  object GcDuration extends MetricSpec.Unsealed {

    val name: String = "jvm.gc.duration"
    val description: String = "Duration of JVM garbage collection actions."
    val unit: String = "s"
    val stability: Stability = Stability.stable
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Name of the garbage collector action.
        *
        * @note
        *   <p> Garbage collector action is generally obtained via <a
        *   href="https://docs.oracle.com/en/java/javase/11/docs/api/jdk.management/com/sun/management/GarbageCollectionNotificationInfo.html#getGcAction()">GarbageCollectionNotificationInfo#getGcAction()</a>.
        */
      val jvmGcAction: AttributeSpec[String] =
        AttributeSpec(
          JvmAttributes.JvmGcAction,
          List(
            "end of minor GC",
            "end of major GC",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** Name of the garbage collector cause.
        *
        * @note
        *   <p> Garbage collector cause is generally obtained via <a
        *   href="https://docs.oracle.com/en/java/javase/11/docs/api/jdk.management/com/sun/management/GarbageCollectionNotificationInfo.html#getGcCause()">GarbageCollectionNotificationInfo#getGcCause()</a>.
        */
      val jvmGcCause: AttributeSpec[String] =
        AttributeSpec(
          JvmExperimentalAttributes.JvmGcCause,
          List(
            "System.gc()",
            "Allocation Failure",
          ),
          Requirement.optIn,
          Stability.development
        )

      /** Name of the garbage collector.
        *
        * @note
        *   <p> Garbage collector name is generally obtained via <a
        *   href="https://docs.oracle.com/en/java/javase/11/docs/api/jdk.management/com/sun/management/GarbageCollectionNotificationInfo.html#getGcName()">GarbageCollectionNotificationInfo#getGcName()</a>.
        */
      val jvmGcName: AttributeSpec[String] =
        AttributeSpec(
          JvmAttributes.JvmGcName,
          List(
            "G1 Young Generation",
            "G1 Old Generation",
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          jvmGcAction,
          jvmGcCause,
          jvmGcName,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue](boundaries: BucketBoundaries): F[Histogram[F, A]] =
      Meter[F]
        .histogram[A](name)
        .withDescription(description)
        .withUnit(unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measure of memory committed.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.MemoryCommitted` instead.", "")
  object MemoryCommitted extends MetricSpec.Unsealed {

    val name: String = "jvm.memory.committed"
    val description: String = "Measure of memory committed."
    val unit: String = "By"
    val stability: Stability = Stability.stable
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Name of the memory pool.
        *
        * @note
        *   <p> Pool names are generally obtained via <a
        *   href="https://docs.oracle.com/en/java/javase/11/docs/api/java.management/java/lang/management/MemoryPoolMXBean.html#getName()">MemoryPoolMXBean#getName()</a>.
        */
      val jvmMemoryPoolName: AttributeSpec[String] =
        AttributeSpec(
          JvmAttributes.JvmMemoryPoolName,
          List(
            "G1 Old Gen",
            "G1 Eden space",
            "G1 Survivor Space",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The type of memory.
        */
      val jvmMemoryType: AttributeSpec[String] =
        AttributeSpec(
          JvmAttributes.JvmMemoryType,
          List(
            "heap",
            "non_heap",
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          jvmMemoryPoolName,
          jvmMemoryType,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Measure of initial memory requested.
    */
  object MemoryInit extends MetricSpec.Unsealed {

    val name: String = "jvm.memory.init"
    val description: String = "Measure of initial memory requested."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Name of the memory pool.
        *
        * @note
        *   <p> Pool names are generally obtained via <a
        *   href="https://docs.oracle.com/en/java/javase/11/docs/api/java.management/java/lang/management/MemoryPoolMXBean.html#getName()">MemoryPoolMXBean#getName()</a>.
        */
      val jvmMemoryPoolName: AttributeSpec[String] =
        AttributeSpec(
          JvmAttributes.JvmMemoryPoolName,
          List(
            "G1 Old Gen",
            "G1 Eden space",
            "G1 Survivor Space",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The type of memory.
        */
      val jvmMemoryType: AttributeSpec[String] =
        AttributeSpec(
          JvmAttributes.JvmMemoryType,
          List(
            "heap",
            "non_heap",
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          jvmMemoryPoolName,
          jvmMemoryType,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Measure of max obtainable memory.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.MemoryLimit` instead.", "")
  object MemoryLimit extends MetricSpec.Unsealed {

    val name: String = "jvm.memory.limit"
    val description: String = "Measure of max obtainable memory."
    val unit: String = "By"
    val stability: Stability = Stability.stable
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Name of the memory pool.
        *
        * @note
        *   <p> Pool names are generally obtained via <a
        *   href="https://docs.oracle.com/en/java/javase/11/docs/api/java.management/java/lang/management/MemoryPoolMXBean.html#getName()">MemoryPoolMXBean#getName()</a>.
        */
      val jvmMemoryPoolName: AttributeSpec[String] =
        AttributeSpec(
          JvmAttributes.JvmMemoryPoolName,
          List(
            "G1 Old Gen",
            "G1 Eden space",
            "G1 Survivor Space",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The type of memory.
        */
      val jvmMemoryType: AttributeSpec[String] =
        AttributeSpec(
          JvmAttributes.JvmMemoryType,
          List(
            "heap",
            "non_heap",
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          jvmMemoryPoolName,
          jvmMemoryType,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Measure of memory used.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.MemoryUsed` instead.", "")
  object MemoryUsed extends MetricSpec.Unsealed {

    val name: String = "jvm.memory.used"
    val description: String = "Measure of memory used."
    val unit: String = "By"
    val stability: Stability = Stability.stable
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Name of the memory pool.
        *
        * @note
        *   <p> Pool names are generally obtained via <a
        *   href="https://docs.oracle.com/en/java/javase/11/docs/api/java.management/java/lang/management/MemoryPoolMXBean.html#getName()">MemoryPoolMXBean#getName()</a>.
        */
      val jvmMemoryPoolName: AttributeSpec[String] =
        AttributeSpec(
          JvmAttributes.JvmMemoryPoolName,
          List(
            "G1 Old Gen",
            "G1 Eden space",
            "G1 Survivor Space",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The type of memory.
        */
      val jvmMemoryType: AttributeSpec[String] =
        AttributeSpec(
          JvmAttributes.JvmMemoryType,
          List(
            "heap",
            "non_heap",
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          jvmMemoryPoolName,
          jvmMemoryType,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Measure of memory used, as measured after the most recent garbage collection event on this pool.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.MemoryUsedAfterLastGc` instead.", "")
  object MemoryUsedAfterLastGc extends MetricSpec.Unsealed {

    val name: String = "jvm.memory.used_after_last_gc"
    val description: String =
      "Measure of memory used, as measured after the most recent garbage collection event on this pool."
    val unit: String = "By"
    val stability: Stability = Stability.stable
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Name of the memory pool.
        *
        * @note
        *   <p> Pool names are generally obtained via <a
        *   href="https://docs.oracle.com/en/java/javase/11/docs/api/java.management/java/lang/management/MemoryPoolMXBean.html#getName()">MemoryPoolMXBean#getName()</a>.
        */
      val jvmMemoryPoolName: AttributeSpec[String] =
        AttributeSpec(
          JvmAttributes.JvmMemoryPoolName,
          List(
            "G1 Old Gen",
            "G1 Eden space",
            "G1 Survivor Space",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The type of memory.
        */
      val jvmMemoryType: AttributeSpec[String] =
        AttributeSpec(
          JvmAttributes.JvmMemoryType,
          List(
            "heap",
            "non_heap",
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          jvmMemoryPoolName,
          jvmMemoryType,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Average CPU load of the whole system for the last minute as reported by the JVM.
    *
    * @note
    *   <p> The value range is [0,n], where n is the number of CPU cores - or a negative number if the value is not
    *   available. This utilization is not defined as being for the specific interval since last measurement (unlike
    *   `system.cpu.utilization`). <a
    *   href="https://docs.oracle.com/en/java/javase/17/docs/api/java.management/java/lang/management/OperatingSystemMXBean.html#getSystemLoadAverage()">Reference</a>.
    */
  object SystemCpuLoad1m extends MetricSpec.Unsealed {

    val name: String = "jvm.system.cpu.load_1m"
    val description: String = "Average CPU load of the whole system for the last minute as reported by the JVM."
    val unit: String = "{run_queue_item}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Recent CPU utilization for the whole system as reported by the JVM.
    *
    * @note
    *   <p> The value range is [0.0,1.0]. This utilization is not defined as being for the specific interval since last
    *   measurement (unlike `system.cpu.utilization`). <a
    *   href="https://docs.oracle.com/en/java/javase/17/docs/api/jdk.management/com/sun/management/OperatingSystemMXBean.html#getCpuLoad()">Reference</a>.
    */
  object SystemCpuUtilization extends MetricSpec.Unsealed {

    val name: String = "jvm.system.cpu.utilization"
    val description: String = "Recent CPU utilization for the whole system as reported by the JVM."
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Number of executing platform threads.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.ThreadCount` instead.", "")
  object ThreadCount extends MetricSpec.Unsealed {

    val name: String = "jvm.thread.count"
    val description: String = "Number of executing platform threads."
    val unit: String = "{thread}"
    val stability: Stability = Stability.stable
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Whether the thread is daemon or not.
        */
      val jvmThreadDaemon: AttributeSpec[Boolean] =
        AttributeSpec(
          JvmAttributes.JvmThreadDaemon,
          List(
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** State of the thread.
        */
      val jvmThreadState: AttributeSpec[String] =
        AttributeSpec(
          JvmAttributes.JvmThreadState,
          List(
            "runnable",
            "blocked",
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          jvmThreadDaemon,
          jvmThreadState,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

}
