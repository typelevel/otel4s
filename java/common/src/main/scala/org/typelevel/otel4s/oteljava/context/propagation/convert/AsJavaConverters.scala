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
 *   scala/collection/convert/AsJavaConverters.scala
 * in the Scala standard library.
 */

package org.typelevel.otel4s.oteljava.context
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

/** Defines explicit conversion methods from Scala to Java for `TextMapGetter`s,
  * `TextMapPropagator`s, and `ContextPropagators`. These methods are available
  * through
  * [[org.typelevel.otel4s.oteljava.context.propagation.convert.PropagatorConverters]].
  */
trait AsJavaConverters {
  import JavaPropagatorWrappers._

  /** Converts a Scala `TextMapGetter` to a Java `TextMapGetter`.
    *
    * The returned Java `TextMapGetter` is backed by the provided Scala
    * `TextMapGetter` unless the Scala `TextMapGetter` was previously obtained
    * from an implicit or explicit call of `asScala`, in which case the original
    * Java `TextMapGetter` will be returned.
    */
  def asJava[A](getter: TextMapGetter[A]): JTextMapGetter[A] = getter match {
    case null                                  => null
    case wrapper: JTextMapGetterWrapper[A @uc] => wrapper.underlying
    case _ => new TextMapGetterWrapper(getter)
  }

  /** Converts a Scala `TextMapPropagator` to a Java `TextMapPropagator`.
    *
    * The returned Java `TextMapPropagator` is backed by the provided Scala
    * `TextMapPropagator`unless the Scala `TextMapPropagator` was previously
    * obtained from an implicit or explicit call of `asScala`, in which case the
    * original Java `TextMapPropagator` will be returned.
    */
  def asJava(propagator: TextMapPropagator[Context]): JTextMapPropagator =
    propagator match {
      case null                               => null
      case wrapper: JTextMapPropagatorWrapper => wrapper.underlying
      case _ => new TextMapPropagatorWrapper(propagator)
    }

  /** Converts a Scala `ContextPropagators` to a Java `ContextPropagators`. */
  def asJava(propagators: ContextPropagators[Context]): JContextPropagators =
    JContextPropagators.create(asJava(propagators.textMapPropagator))
}
