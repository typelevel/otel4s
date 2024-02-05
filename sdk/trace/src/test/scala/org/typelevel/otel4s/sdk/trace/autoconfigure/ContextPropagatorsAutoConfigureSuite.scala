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
package sdk
package trace
package autoconfigure

import cats.effect.IO
import cats.syntax.either._
import munit.CatsEffectSuite
import org.typelevel.otel4s.context.propagation.TextMapGetter
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.context.propagation.TextMapUpdater
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.context.Context

class ContextPropagatorsAutoConfigureSuite extends CatsEffectSuite {

  private val default =
    "ContextPropagators.Default{textMapPropagator=TextMapPropagator.Multi(W3CTraceContextPropagator, W3CBaggagePropagator)}"

  test("load from an empty config - load default") {
    val config = Config(Map.empty, Map.empty, Map.empty)

    ContextPropagatorsAutoConfigure[IO](Set.empty)
      .configure(config)
      .use { propagators =>
        IO(assertEquals(propagators.toString, default))
      }
  }

  test("load from the config (empty string) - load default") {
    val props = Map("otel.propagators" -> "")
    val config = Config.ofProps(props)

    ContextPropagatorsAutoConfigure[IO](Set.empty)
      .configure(config)
      .use { propagators =>
        IO(assertEquals(propagators.toString, default))
      }
  }

  test("load from the config (none) - load noop") {
    val props = Map("otel.propagators" -> "none")
    val config = Config.ofProps(props)

    ContextPropagatorsAutoConfigure[IO](Set.empty)
      .configure(config)
      .use { propagators =>
        IO(assertEquals(propagators.toString, "ContextPropagators.Noop"))
      }
  }

  test("load from the config - load all known") {
    val props =
      Map("otel.propagators" -> "tracecontext,baggage,b3,b3multi,jaeger")

    val config = Config.ofProps(props)
    val expected =
      "ContextPropagators.Default{textMapPropagator=TextMapPropagator.Multi(" +
        "B3Propagator{b3PropagatorInjector=B3Propagator.Injector.SingleHeader}, " +
        "JaegerPropagator, " +
        "B3Propagator{b3PropagatorInjector=B3Propagator.Injector.MultipleHeaders}, " +
        "W3CTraceContextPropagator, " +
        "W3CBaggagePropagator)}"

    ContextPropagatorsAutoConfigure[IO](Set.empty)
      .configure(config)
      .use { propagators =>
        IO(assertEquals(propagators.toString, expected))
      }
  }

  test("support custom configurers") {
    val props = Map("otel.propagators" -> "tracecontext,custom")

    val propagator: TextMapPropagator[Context] =
      new TextMapPropagator[Context] {
        def fields: Iterable[String] = Nil
        def extract[A: TextMapGetter](ctx: Context, carrier: A): Context = ???
        def inject[A: TextMapUpdater](ctx: Context, carrier: A): A = ???
        override def toString: String = "CustomPropagator"
      }

    val custom: AutoConfigure.Named[IO, TextMapPropagator[Context]] =
      AutoConfigure.Named.const("custom", propagator)

    val config = Config.ofProps(props)

    val expected =
      "ContextPropagators.Default{textMapPropagator=TextMapPropagator.Multi(" +
        "W3CTraceContextPropagator, " +
        "CustomPropagator)}"

    ContextPropagatorsAutoConfigure[IO](Set(custom))
      .configure(config)
      .use { propagators =>
        IO(assertEquals(propagators.toString, expected))
      }
  }

  test("load from the config - unknown propagator - fail") {
    val props = Map("otel.propagators" -> "aws-xray")
    val config = Config.ofProps(props)

    ContextPropagatorsAutoConfigure[IO](Set.empty)
      .configure(config)
      .use_
      .attempt
      .map(_.leftMap(_.getMessage))
      .assertEquals(
        Left("""Cannot autoconfigure [ContextPropagators].
            |Cause: Unrecognized value for [otel.propagators]: aws-xray. Supported options [b3, jaeger, b3multi, none, tracecontext, baggage].
            |Config:
            |1) `otel.propagators` - aws-xray""".stripMargin)
      )
  }
}
