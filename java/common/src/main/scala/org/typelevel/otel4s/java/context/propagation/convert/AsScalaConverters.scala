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
 *   scala/collection/convert/AsScalaConverters.scala
 * in the Scala standard library.
 */

package org.typelevel.otel4s.java.context
package propagation.convert

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

import scala.{unchecked => uc}

/** Defines explicit conversion methods from Java to Scala for `TextMapGetter`s,
  * `TextMapPropagator`s, and `ContextPropagators`. These methods are available
  * through
  * [[org.typelevel.otel4s.java.context.propagation.convert.PropagatorConverters]].
  */
trait AsScalaConverters {
  import JavaPropagatorWrappers._

  /** Converts a Java `TextMapGetter` to a Scala `TextMapGetter`.
    *
    * The returned Scala `TextMapGetter` is backed by the provided Java
    * `TextMapGetter` unless the Java `TextMapGetter` was previously obtained
    * from an implicit or explicit call of `asJava`, in which case the original
    * Scala `TextMapGetter` will be returned.
    */
  def asScala[A](getter: JTextMapGetter[A]): TextMapGetter[A] = getter match {
    case null                                 => null
    case wrapper: TextMapGetterWrapper[A @uc] => wrapper.underlying
    case _ => new JTextMapGetterWrapper(getter)
  }

  /** Converts a Java `TextMapPropagator` to a Scala `TextMapPropagator`.
    *
    * The returned Scala `TextMapPropagator` is backed by the provided Java
    * `TextMapPropagator` unless the Java `TextMapPropagator` was previously
    * obtained from an implicit or explicit call of `asJava`, in which case the
    * original Scala `TextMapPropagator` will be returned.
    */
  def asScala(propagator: JTextMapPropagator): TextMapPropagator[Context] =
    propagator match {
      case null                              => null
      case wrapper: TextMapPropagatorWrapper => wrapper.underlying
      case _ => new JTextMapPropagatorWrapper(propagator)
    }

  /** Converts a Java `ContextPropagators` to a Scala `ContextPropagators`. */
  def asScala(propagators: JContextPropagators): ContextPropagators[Context] =
    ContextPropagators.of(asScala(propagators.getTextMapPropagator))
}
