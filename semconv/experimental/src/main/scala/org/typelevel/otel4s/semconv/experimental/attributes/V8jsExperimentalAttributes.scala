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
object V8jsExperimentalAttributes {

  /** The type of garbage collection.
    */
  val V8jsGcType: AttributeKey[String] =
    AttributeKey("v8js.gc.type")

  /** The name of the space type of heap memory.
    *
    * @note
    *   <p> Value can be retrieved from value `space_name` of <a
    *   href="https://nodejs.org/api/v8.html#v8getheapspacestatistics">`v8.getHeapSpaceStatistics()`</a>
    */
  val V8jsHeapSpaceName: AttributeKey[String] =
    AttributeKey("v8js.heap.space.name")

  /** Values for [[V8jsGcType]].
    */
  abstract class V8jsGcTypeValue(val value: String)
  object V8jsGcTypeValue {

    /** Major (Mark Sweep Compact).
      */
    case object Major extends V8jsGcTypeValue("major")

    /** Minor (Scavenge).
      */
    case object Minor extends V8jsGcTypeValue("minor")

    /** Incremental (Incremental Marking).
      */
    case object Incremental extends V8jsGcTypeValue("incremental")

    /** Weak Callbacks (Process Weak Callbacks).
      */
    case object Weakcb extends V8jsGcTypeValue("weakcb")
  }

  /** Values for [[V8jsHeapSpaceName]].
    */
  abstract class V8jsHeapSpaceNameValue(val value: String)
  object V8jsHeapSpaceNameValue {

    /** New memory space.
      */
    case object NewSpace extends V8jsHeapSpaceNameValue("new_space")

    /** Old memory space.
      */
    case object OldSpace extends V8jsHeapSpaceNameValue("old_space")

    /** Code memory space.
      */
    case object CodeSpace extends V8jsHeapSpaceNameValue("code_space")

    /** Map memory space.
      */
    case object MapSpace extends V8jsHeapSpaceNameValue("map_space")

    /** Large object memory space.
      */
    case object LargeObjectSpace extends V8jsHeapSpaceNameValue("large_object_space")
  }

}
