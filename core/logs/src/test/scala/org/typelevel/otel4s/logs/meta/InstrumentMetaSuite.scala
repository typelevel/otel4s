/*
 * Copyright 2025 Typelevel
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

package org.typelevel.otel4s.logs.meta

import cats.data.Kleisli
import cats.effect.IO
import munit.CatsEffectSuite
import org.typelevel.otel4s.logs.Severity

class InstrumentMetaSuite extends CatsEffectSuite {

  test("dynamic uses the explicitly provided context") {
    val severity = Some(Severity.error)
    val eventName = Some("explicit-context")

    for {
      captured <- IO.ref(Option.empty[(String, Option[Severity], Option[String])])
      meta = InstrumentMeta.dynamic[Kleisli[IO, String, *], String] { (ctx, sev, event) =>
        Kleisli.liftF(captured.set(Some((ctx, sev, event))).as(ctx == "expected-context"))
      }
      result <- meta.isEnabled("expected-context", severity, eventName).run("ambient-context")
      seen <- captured.get
    } yield {
      assertEquals(result, true)
      assertEquals(seen, Some(("expected-context", severity, eventName)))
    }
  }

  test("dynamic resolves context from Ask for context-free isEnabled") {
    val severity = Some(Severity.warn)
    val eventName = Some("ask-context")

    for {
      captured <- IO.ref(Option.empty[(String, Option[Severity], Option[String])])
      meta = InstrumentMeta.dynamic[Kleisli[IO, String, *], String] { (ctx, sev, event) =>
        Kleisli.liftF(captured.set(Some((ctx, sev, event))).as(ctx == "context-from-ask"))
      }
      result <- meta.isEnabled(severity, eventName).run("context-from-ask")
      seen <- captured.get
    } yield {
      assertEquals(result, true)
      assertEquals(seen, Some(("context-from-ask", severity, eventName)))
    }
  }
}
