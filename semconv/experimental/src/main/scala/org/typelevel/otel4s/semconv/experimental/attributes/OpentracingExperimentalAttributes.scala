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
object OpentracingExperimentalAttributes {

  /** Parent-child Reference type <p>
    * @note
    *   <p> The causal relationship between a child Span and a parent Span.
    */
  val OpentracingRefType: AttributeKey[String] =
    AttributeKey("opentracing.ref_type")

  /** Values for [[OpentracingRefType]].
    */
  abstract class OpentracingRefTypeValue(val value: String)
  object OpentracingRefTypeValue {

    /** The parent Span depends on the child Span in some capacity
      */
    case object ChildOf extends OpentracingRefTypeValue("child_of")

    /** The parent Span doesn't depend in any way on the result of the child
      * Span
      */
    case object FollowsFrom extends OpentracingRefTypeValue("follows_from")
  }

}
