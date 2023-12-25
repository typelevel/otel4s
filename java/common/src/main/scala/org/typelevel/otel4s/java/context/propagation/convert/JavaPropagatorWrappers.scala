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

package org.typelevel.otel4s.java.context
package propagation.convert

import io.opentelemetry.context.{Context => JContext}
import io.opentelemetry.context.propagation.{TextMapGetter => JTextMapGetter}
import io.opentelemetry.context.propagation.{
  TextMapPropagator => JTextMapPropagator
}
import io.opentelemetry.context.propagation.{TextMapSetter => JTextMapSetter}
import org.typelevel.otel4s.context.propagation.TextMapGetter
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.context.propagation.TextMapUpdater

import java.{lang => jl}
import java.{util => ju}
import scala.jdk.CollectionConverters._

private[convert] object JavaPropagatorWrappers {
  class TextMapGetterWrapper[C](val underlying: TextMapGetter[C])
      extends JTextMapGetter[C] {
    def keys(carrier: C): jl.Iterable[String] =
      underlying.keys(carrier).asJava
    def get(carrier: C /* may be `null` */, key: String): String =
      Option(carrier)
        .flatMap(underlying.get(_, key))
        .orNull
  }

  class JTextMapGetterWrapper[C](val underlying: JTextMapGetter[C])
      extends TextMapGetter[C] {
    def get(carrier: C, key: String): Option[String] =
      Option(underlying.get(carrier, key) /* may return `null` */ )
    def keys(carrier: C): Iterable[String] =
      underlying.keys(carrier).asScala
  }

  class TextMapPropagatorWrapper(val underlying: TextMapPropagator[Context])
      extends JTextMapPropagator {
    def fields(): ju.Collection[String] =
      underlying.fields.asJavaCollection
    def inject[C](
        context: JContext,
        carrier: C, // may be `null`
        setter: JTextMapSetter[C]
    ): Unit = {
      // sadly, this must essentially inject twice
      val immutableRes =
        underlying.inject(Context.wrap(context), List.empty[(String, String)])
      for ((key, value) <- immutableRes) setter.set(carrier, key, value)
    }
    def extract[C](
        context: JContext,
        carrier: C, // may be `null`
        getter: JTextMapGetter[C]
    ): JContext = {
      implicit val tmg: TextMapGetter[C] =
        PropagatorConverters.asScala(getter) // don't double-wrap
      Option(carrier)
        .fold(context)(underlying.extract(Context.wrap(context), _).underlying)
    }
  }

  class JTextMapPropagatorWrapper(val underlying: JTextMapPropagator)
      extends TextMapPropagator[Context] {
    def fields: Iterable[String] =
      underlying.fields().asScala
    def extract[A](ctx: Context, carrier: A)(implicit
        getter: TextMapGetter[A]
    ): Context =
      ctx.map(
        underlying.extract(
          _,
          carrier,
          PropagatorConverters.asJava(getter) // don't double-wrap
        )
      )
    def inject[A](ctx: Context, carrier: A)(implicit
        updater: TextMapUpdater[A]
    ): A = {
      var res = carrier
      underlying.inject[Null](
        ctx.underlying,
        null, // explicitly allowed per opentelemetry-java, so our setter can be a lambda!
        (_, key, value) => res = updater.updated(res, key, value)
      )
      res
    }
  }
}
