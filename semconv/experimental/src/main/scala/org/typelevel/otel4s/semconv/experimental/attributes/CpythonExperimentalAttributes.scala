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
object CpythonExperimentalAttributes {

  /** Value of the garbage collector collection generation.
    */
  val CpythonGcGeneration: AttributeKey[Long] =
    AttributeKey("cpython.gc.generation")

  /** Values for [[CpythonGcGeneration]].
    */
  abstract class CpythonGcGenerationValue(val value: Long)
  object CpythonGcGenerationValue {

    /** Generation 0
      */
    case object Generation0 extends CpythonGcGenerationValue(0)

    /** Generation 1
      */
    case object Generation1 extends CpythonGcGenerationValue(1)

    /** Generation 2
      */
    case object Generation2 extends CpythonGcGenerationValue(2)
  }

}
