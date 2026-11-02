/*
 * Copyright 2023 Typelevel
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
package experimental.attributes

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/attributes/SemanticAttributes.scala.j2
object JvmExperimentalAttributes {

  /** Name of the buffer pool.
    *
    * @note
    *   <p> Pool names are generally obtained via <a
    *   href="https://docs.oracle.com/en/java/javase/11/docs/api/java.management/java/lang/management/BufferPoolMXBean.html#getName()">BufferPoolMXBean#getName()</a>.
    */
  val JvmBufferPoolName: AttributeKey[String] =
    AttributeKey("jvm.buffer.pool.name")

  /** Name of the executor.
    *
    * @note
    *   <p> The executor name SHOULD be low-cardinality. If `jvm.executor.owner.name` is present, the combination of
    *   `jvm.executor.owner.name` and `jvm.executor.name` SHOULD be unique within the instrumented application;
    *   otherwise, `jvm.executor.name` SHOULD be unique when an executor-specific name is available. If the executor
    *   implementation does not provide an explicit name, instrumentation SHOULD derive a name from available
    *   implementation-specific metadata, such as a normalized worker thread name. If no executor-specific name or
    *   metadata is available, instrumentation MAY use `unknown`. The `unknown` value does not identify an executor, and
    *   metrics from multiple executors using this value MAY be aggregated. Users requiring per-executor metric series
    *   SHOULD configure a distinct name through the executor's `ThreadFactory`. Instrumentations SHOULD document how
    *   the name is derived and how users can configure it. Once metric series registration for an executor has started,
    *   `jvm.executor.name` and `jvm.executor.owner.name` MUST remain stable for the lifetime of that registration.
    *   Instrumentations SHOULD determine these attributes before the first worker is registered. Instrumentations MUST
    *   NOT re-register cumulative metrics under a new attribute set or replay prior cumulative values under new
    *   attributes.
    */
  val JvmExecutorName: AttributeKey[String] =
    AttributeKey("jvm.executor.name")

  /** Name of the logical component that owns the executor.
    *
    * @note
    *   <p> The executor owner name SHOULD be a low-cardinality identifier for the component that creates and configures
    *   the executor. It is neither the executor implementation type nor an OpenTelemetry instrumentation scope.
    */
  val JvmExecutorOwnerName: AttributeKey[String] =
    AttributeKey("jvm.executor.owner.name")

  /** State of an executor thread.
    */
  val JvmExecutorThreadState: AttributeKey[String] =
    AttributeKey("jvm.executor.thread.state")

  /** The fully qualified class name of the executor implementation.
    */
  val JvmExecutorType: AttributeKey[String] =
    AttributeKey("jvm.executor.type")

  /** Name of the garbage collector action.
    *
    * @note
    *   <p> Garbage collector action is generally obtained via <a
    *   href="https://docs.oracle.com/en/java/javase/11/docs/api/jdk.management/com/sun/management/GarbageCollectionNotificationInfo.html#getGcAction()">GarbageCollectionNotificationInfo#getGcAction()</a>.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.JvmAttributes.JvmGcAction` instead.",
    ""
  )
  val JvmGcAction: AttributeKey[String] =
    AttributeKey("jvm.gc.action")

  /** Name of the garbage collector cause.
    *
    * @note
    *   <p> Garbage collector cause is generally obtained via <a
    *   href="https://docs.oracle.com/en/java/javase/11/docs/api/jdk.management/com/sun/management/GarbageCollectionNotificationInfo.html#getGcCause()">GarbageCollectionNotificationInfo#getGcCause()</a>.
    */
  val JvmGcCause: AttributeKey[String] =
    AttributeKey("jvm.gc.cause")

  /** Name of the garbage collector.
    *
    * @note
    *   <p> Garbage collector name is generally obtained via <a
    *   href="https://docs.oracle.com/en/java/javase/11/docs/api/jdk.management/com/sun/management/GarbageCollectionNotificationInfo.html#getGcName()">GarbageCollectionNotificationInfo#getGcName()</a>.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.JvmAttributes.JvmGcName` instead.",
    ""
  )
  val JvmGcName: AttributeKey[String] =
    AttributeKey("jvm.gc.name")

  /** Name of the memory pool.
    *
    * @note
    *   <p> Pool names are generally obtained via <a
    *   href="https://docs.oracle.com/en/java/javase/11/docs/api/java.management/java/lang/management/MemoryPoolMXBean.html#getName()">MemoryPoolMXBean#getName()</a>.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.JvmAttributes.JvmMemoryPoolName` instead.",
    ""
  )
  val JvmMemoryPoolName: AttributeKey[String] =
    AttributeKey("jvm.memory.pool.name")

  /** The type of memory.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.JvmAttributes.JvmMemoryType` instead.",
    ""
  )
  val JvmMemoryType: AttributeKey[String] =
    AttributeKey("jvm.memory.type")

  /** Whether the thread is daemon or not.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.JvmAttributes.JvmThreadDaemon` instead.",
    ""
  )
  val JvmThreadDaemon: AttributeKey[Boolean] =
    AttributeKey("jvm.thread.daemon")

  /** State of the thread.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.JvmAttributes.JvmThreadState` instead.",
    ""
  )
  val JvmThreadState: AttributeKey[String] =
    AttributeKey("jvm.thread.state")

  /** Values for [[JvmExecutorThreadState]].
    */
  abstract class JvmExecutorThreadStateValue(val value: String)
  object JvmExecutorThreadStateValue {
    implicit val attributeFromJvmExecutorThreadStateValue: Attribute.From[JvmExecutorThreadStateValue, String] = _.value

    /** The thread is currently executing a task.
      */
    case object Active extends JvmExecutorThreadStateValue("active")

    /** The thread currently exists but is not executing a task.
      */
    case object Idle extends JvmExecutorThreadStateValue("idle")
  }

  /** Values for [[JvmMemoryType]].
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.JvmAttributes.JvmMemoryType` instead.",
    ""
  )
  abstract class JvmMemoryTypeValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object JvmMemoryTypeValue {
    implicit val attributeFromJvmMemoryTypeValue: Attribute.From[JvmMemoryTypeValue, String] = _.value

    /** Heap memory.
      */
    case object Heap extends JvmMemoryTypeValue("heap")

    /** Non-heap memory
      */
    case object NonHeap extends JvmMemoryTypeValue("non_heap")
  }

  /** Values for [[JvmThreadState]].
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.JvmAttributes.JvmThreadState` instead.",
    ""
  )
  abstract class JvmThreadStateValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object JvmThreadStateValue {
    implicit val attributeFromJvmThreadStateValue: Attribute.From[JvmThreadStateValue, String] = _.value

    /** A thread that has not yet started is in this state.
      */
    case object New extends JvmThreadStateValue("new")

    /** A thread executing in the Java virtual machine is in this state.
      */
    case object Runnable extends JvmThreadStateValue("runnable")

    /** A thread that is blocked waiting for a monitor lock is in this state.
      */
    case object Blocked extends JvmThreadStateValue("blocked")

    /** A thread that is waiting indefinitely for another thread to perform a particular action is in this state.
      */
    case object Waiting extends JvmThreadStateValue("waiting")

    /** A thread that is waiting for another thread to perform an action for up to a specified waiting time is in this
      * state.
      */
    case object TimedWaiting extends JvmThreadStateValue("timed_waiting")

    /** A thread that has exited is in this state.
      */
    case object Terminated extends JvmThreadStateValue("terminated")
  }

}
