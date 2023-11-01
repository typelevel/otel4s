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

/*
 * The scaladocs in this file are adapted from those in the file
 *   scala/collection/convert/AsJavaExtensions.scala
 * in the Scala standard library.
 */

package org.typelevel.otel4s.java.context
package propagation
package convert

import io.opentelemetry.context.propagation.{
  ContextPropagators => JContextPropagators
}
import io.opentelemetry.context.propagation.{TextMapGetter => JTextMapGetter}
import io.opentelemetry.context.propagation.{
  TextMapPropagator => JTextMapPropagator
}
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.context.propagation.TextMapGetter
import org.typelevel.otel4s.context.propagation.TextMapPropagator

/** Defines `asJava` extension methods for `TextMapGetter`s and
  * `TextMapPropagator`s, and `ContextPropagators`, available through
  * [[org.typelevel.otel4s.java.context.propagation.PropagatorConverters]].
  */
trait AsJavaExtensions {
  import convert.{PropagatorConverters => conv}

  implicit class TextMapGetterHasAsJava[A](getter: TextMapGetter[A]) {

    /** Converts a Scala `TextMapGetter` to a Java `TextMapGetter`.
      *
      * @see
      *   [[org.typelevel.otel4s.java.context.propagation.convert.AsJavaConverters.asJava[A](getter* `convert.PropagatorConverters.asJava`]].
      */
    def asJava: JTextMapGetter[A] = conv.asJava(getter)
  }

  implicit class TextMapPropagatorHasAsJava(prop: TextMapPropagator[Context]) {

    /** Converts a Scala `TextMapPropagator` to a Java `TextMapPropagator`.
      *
      * @see
      *   [[org.typelevel.otel4s.java.context.propagation.convert.AsJavaConverters.asJava(propagator:* `convert.PropagatorConverters.asJava`]].
      */
    def asJava: JTextMapPropagator = conv.asJava(prop)
  }

  implicit class ContextPropagatorsHasAsJava(cp: ContextPropagators[Context]) {

    /** Converts a Scala `ContextPropagators` to a Java `ContextPropagators`.
      *
      * @see
      *   [[org.typelevel.otel4s.java.context.propagation.convert.AsJavaConverters.asJava(propagators* `convert.PropagatorConverters.asJava`]].
      */
    def asJava: JContextPropagators = conv.asJava(cp)
  }
}
