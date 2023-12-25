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
 *   scala/jdk/CollectionConverters.scala
 * in the Scala standard library.
 */

package org.typelevel.otel4s.java.context.propagation

/** This object provides extension methods that convert between Scala and Java
  * `TextMapGetter`s, `TextMapPropagator`s, and `ContextPropagators` using
  * `asScala` and `asJava` extension methods.
  *
  * For the rare instances where they are needed, explicit conversion methods
  * are defined in
  * [[org.typelevel.otel4s.java.context.propagation.convert.PropagatorConverters]].
  *
  * {{{
  *   import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
  *   import org.typelevel.otel4s.context.propagation.TextMapPropagator
  *   import org.typelevel.otel4s.java.context.Context
  *   import org.typelevel.otel4s.java.context.propagation.TextMapOperatorConverters._
  *
  *   val propagator: TextMapPropagator[Context] = W3CTraceContextPropagator.getInstance().asScala
  * }}}
  *
  * The conversions return wrappers for the TextMap operators, and converting
  * from a source type to a target type and back again will return the original
  * source object. For example:
  *
  * {{{
  *   import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
  *   import org.typelevel.otel4s.context.propagation.TextMapPropagator
  *   import org.typelevel.otel4s.java.context.Context
  *   import org.typelevel.otel4s.java.context.propagation.TextMapOperatorConverters._
  *
  *   val source: W3CTraceContextPropagator = W3CTraceContextPropagator.getInstance()
  *   val target: TextMapPropagator[Context] = source.asScala
  *   val other: io.opentelemetry.context.propagation.TextMapPropagator = target.asJava
  *   assert(source eq other)
  * }}}
  *
  * Currently, `ContextPropagators` for both Java and Scala are simple wrappers
  * around a `TextMapPropagator` instance of the corresponding type.
  * Consequently, conversions between `ContextPropagators` convert the
  * `TextMapPropagator` and do not use a custom wrapper.
  */
object PropagatorConverters
    extends convert.AsJavaExtensions
    with convert.AsScalaExtensions
