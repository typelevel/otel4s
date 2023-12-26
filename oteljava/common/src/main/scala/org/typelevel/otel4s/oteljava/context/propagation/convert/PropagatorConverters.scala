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
 *   scala/jdk/javaapi/CollectionConverters.scala
 * in the Scala standard library.
 */

package org.typelevel.otel4s.oteljava.context.propagation.convert

/** This object contains methods that convert between Scala and Java
  * `TextMapGetter`s, `TextMapPropagator`s, and `ContextPropagators` using
  * explicit `asScala` and `asJava` methods.
  *
  * In general, it is preferable to use the extension methods defined in
  * [[org.typelevel.otel4s.oteljava.context.propagation.PropagatorConverters]].
  *
  * {{{
  *   // Java Code
  *   import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
  *   import org.typelevel.otel4s.context.propagation.TextMapPropagator;
  *   import org.typelevel.otel4s.oteljava.context.Context;
  *   import org.typelevel.otel4s.oteljava.context.propagation.convert.TextMapOperatorConverters;
  *
  *   public class Example {
  *     public void foo(W3CTraceContextPropagator jp) {
  *       TextMapPropagator<Context> sp = TextMapOperatorConverters.asScala(jp);
  *     }
  *   }
  * }}}
  *
  * The conversions return wrappers for the TextMap operators, and converting
  * from a source type to a target type and back again will return the original
  * source object.
  *
  * Currently, `ContextPropagators` for both Java and Scala are simple wrappers
  * around a `TextMapPropagator` instance of the corresponding type.
  * Consequently, conversions between `ContextPropagators` convert the
  * `TextMapPropagator` and do not use a custom wrapper.
  */
object PropagatorConverters extends AsJavaConverters with AsScalaConverters
