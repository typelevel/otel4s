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

import org.typelevel.otel4s.metrics._
import org.typelevel.otel4s.semconv.attributes._
import org.typelevel.otel4s.semconv.experimental.attributes._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object JvmExperimentalMetrics {

  /** Number of buffers in the pool.
    */
  object BufferCount {

    val Name = "jvm.buffer.count"
    val Description = "Number of buffers in the pool."
    val Unit = "{buffer}"

    object AttributeSpecs {

      /** Name of the buffer pool. <p>
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
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          jvmBufferPoolName,
        )
    }

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Measure of total memory capacity of buffers.
    */
  object BufferMemoryLimit {

    val Name = "jvm.buffer.memory.limit"
    val Description = "Measure of total memory capacity of buffers."
    val Unit = "By"

    object AttributeSpecs {

      /** Name of the buffer pool. <p>
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
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          jvmBufferPoolName,
        )
    }

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Deprecated, use `jvm.buffer.memory.used` instead.
    */
  @deprecated("Replaced by `jvm.buffer.memory.used`.", "")
  object BufferMemoryUsage {

    val Name = "jvm.buffer.memory.usage"
    val Description = "Deprecated, use `jvm.buffer.memory.used` instead."
    val Unit = "By"

    object AttributeSpecs {

      /** Name of the buffer pool. <p>
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
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          jvmBufferPoolName,
        )
    }

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Measure of memory used by buffers.
    */
  object BufferMemoryUsed {

    val Name = "jvm.buffer.memory.used"
    val Description = "Measure of memory used by buffers."
    val Unit = "By"

    object AttributeSpecs {

      /** Name of the buffer pool. <p>
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
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          jvmBufferPoolName,
        )
    }

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Number of classes currently loaded.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.ClassCount` instead.", "")
  object ClassCount {

    val Name = "jvm.class.count"
    val Description = "Number of classes currently loaded."
    val Unit = "{class}"

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Number of classes loaded since JVM start.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.ClassLoaded` instead.", "")
  object ClassLoaded {

    val Name = "jvm.class.loaded"
    val Description = "Number of classes loaded since JVM start."
    val Unit = "{class}"

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Number of classes unloaded since JVM start.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.ClassUnloaded` instead.", "")
  object ClassUnloaded {

    val Name = "jvm.class.unloaded"
    val Description = "Number of classes unloaded since JVM start."
    val Unit = "{class}"

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Number of processors available to the Java virtual machine.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.CpuCount` instead.", "")
  object CpuCount {

    val Name = "jvm.cpu.count"
    val Description = "Number of processors available to the Java virtual machine."
    val Unit = "{cpu}"

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Recent CPU utilization for the process as reported by the JVM. <p>
    * @note
    *   <p> The value range is [0.0,1.0]. This utilization is not defined as being for the specific interval since last
    *   measurement (unlike `system.cpu.utilization`). <a
    *   href="https://docs.oracle.com/en/java/javase/17/docs/api/jdk.management/com/sun/management/OperatingSystemMXBean.html#getProcessCpuLoad()">Reference</a>.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.CpuRecentUtilization` instead.", "")
  object CpuRecentUtilization {

    val Name = "jvm.cpu.recent_utilization"
    val Description = "Recent CPU utilization for the process as reported by the JVM."
    val Unit = "1"

    def create[F[_]: Meter]: F[Gauge[F, Long]] =
      Meter[F]
        .gauge[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** CPU time used by the process as reported by the JVM.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.CpuTime` instead.", "")
  object CpuTime {

    val Name = "jvm.cpu.time"
    val Description = "CPU time used by the process as reported by the JVM."
    val Unit = "s"

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Duration of JVM garbage collection actions.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.GcDuration` instead.", "")
  object GcDuration {

    val Name = "jvm.gc.duration"
    val Description = "Duration of JVM garbage collection actions."
    val Unit = "s"

    object AttributeSpecs {

      /** Name of the garbage collector action. <p>
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

      /** Name of the garbage collector. <p>
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
          jvmGcName,
        )
    }

    def create[F[_]: Meter](boundaries: BucketBoundaries): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measure of memory committed.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.MemoryCommitted` instead.", "")
  object MemoryCommitted {

    val Name = "jvm.memory.committed"
    val Description = "Measure of memory committed."
    val Unit = "By"

    object AttributeSpecs {

      /** Name of the memory pool. <p>
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

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Measure of initial memory requested.
    */
  object MemoryInit {

    val Name = "jvm.memory.init"
    val Description = "Measure of initial memory requested."
    val Unit = "By"

    object AttributeSpecs {

      /** Name of the memory pool. <p>
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

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Measure of max obtainable memory.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.MemoryLimit` instead.", "")
  object MemoryLimit {

    val Name = "jvm.memory.limit"
    val Description = "Measure of max obtainable memory."
    val Unit = "By"

    object AttributeSpecs {

      /** Name of the memory pool. <p>
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

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Measure of memory used.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.MemoryUsed` instead.", "")
  object MemoryUsed {

    val Name = "jvm.memory.used"
    val Description = "Measure of memory used."
    val Unit = "By"

    object AttributeSpecs {

      /** Name of the memory pool. <p>
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

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Measure of memory used, as measured after the most recent garbage collection event on this pool.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.MemoryUsedAfterLastGc` instead.", "")
  object MemoryUsedAfterLastGc {

    val Name = "jvm.memory.used_after_last_gc"
    val Description = "Measure of memory used, as measured after the most recent garbage collection event on this pool."
    val Unit = "By"

    object AttributeSpecs {

      /** Name of the memory pool. <p>
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

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Average CPU load of the whole system for the last minute as reported by the JVM. <p>
    * @note
    *   <p> The value range is [0,n], where n is the number of CPU cores - or a negative number if the value is not
    *   available. This utilization is not defined as being for the specific interval since last measurement (unlike
    *   `system.cpu.utilization`). <a
    *   href="https://docs.oracle.com/en/java/javase/17/docs/api/java.management/java/lang/management/OperatingSystemMXBean.html#getSystemLoadAverage()">Reference</a>.
    */
  object SystemCpuLoad1m {

    val Name = "jvm.system.cpu.load_1m"
    val Description = "Average CPU load of the whole system for the last minute as reported by the JVM."
    val Unit = "{run_queue_item}"

    def create[F[_]: Meter]: F[Gauge[F, Long]] =
      Meter[F]
        .gauge[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Recent CPU utilization for the whole system as reported by the JVM. <p>
    * @note
    *   <p> The value range is [0.0,1.0]. This utilization is not defined as being for the specific interval since last
    *   measurement (unlike `system.cpu.utilization`). <a
    *   href="https://docs.oracle.com/en/java/javase/17/docs/api/jdk.management/com/sun/management/OperatingSystemMXBean.html#getCpuLoad()">Reference</a>.
    */
  object SystemCpuUtilization {

    val Name = "jvm.system.cpu.utilization"
    val Description = "Recent CPU utilization for the whole system as reported by the JVM."
    val Unit = "1"

    def create[F[_]: Meter]: F[Gauge[F, Long]] =
      Meter[F]
        .gauge[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Number of executing platform threads.
    */
  @deprecated("Use stable `org.typelevel.otel4s.semconv.metrics.JvmMetrics.ThreadCount` instead.", "")
  object ThreadCount {

    val Name = "jvm.thread.count"
    val Description = "Number of executing platform threads."
    val Unit = "{thread}"

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

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

}
