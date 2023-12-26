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
 *   scala/collection/convert/AsScalaExtensions.scala
 * in the Scala standard library.
 */

package org.typelevel.otel4s.oteljava.context
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

/** Defines `asScala` extension methods for `TextMapGetter`s,
  * `TextMapPropagator`s, and `ContextPropagators`, available through
  * [[org.typelevel.otel4s.oteljava.context.propagation.PropagatorConverters]].
  */
trait AsScalaExtensions {
  import convert.{PropagatorConverters => conv}

  implicit class TextMapGetterHasAsScala[A](getter: JTextMapGetter[A]) {

    /** Converts a Java `TextMapGetter` to a Scala `TextMapGetter`.
      *
      * @see
      *   [[org.typelevel.otel4s.oteljava.context.propagation.convert.AsScalaConverters.asScala[A](getter* `convert.PropagatorConverters.asScala`]].
      */
    def asScala: TextMapGetter[A] = conv.asScala(getter)
  }

  implicit class TextMapPropagatorHasAsScala(propagator: JTextMapPropagator) {

    /** Converts a Java `TextMapPropagator` to a Scala `TextMapPropagator`.
      *
      * @see
      *   [[org.typelevel.otel4s.oteljava.context.propagation.convert.AsScalaConverters.asScala(propagator:* `convert.PropagatorConverters.asScala`]].
      */
    def asScala: TextMapPropagator[Context] = conv.asScala(propagator)
  }

  implicit class ContextPropagatorsHasAsScala(cp: JContextPropagators) {

    /** Converts a Java `TextMapPropagator` to a Scala `TextMapPropagator`.
      *
      * @see
      *   [[org.typelevel.otel4s.oteljava.context.propagation.convert.AsScalaConverters.asScala(propagators* `convert.PropagatorConverters.asScala`]].
      */
    def asScala: ContextPropagators[Context] = conv.asScala(cp)
  }
}
