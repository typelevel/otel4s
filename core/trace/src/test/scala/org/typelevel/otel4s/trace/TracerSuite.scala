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

package org.typelevel.otel4s
package trace

import cats.effect.IO
import munit.CatsEffectSuite

import scala.concurrent.duration._

class TracerSuite extends CatsEffectSuite {

  test("do not allocate attributes when instrument is noop") {
    val tracer = Tracer.noop[IO]

    var allocated = false

    def text = {
      allocated = true
      "text"
    }

    def status = {
      allocated = true
      Status.Ok
    }

    def timestamp = {
      allocated = true
      100.millis
    }

    def attribute: List[Attribute[String]] = {
      allocated = true
      List(Attribute("key", "value"))
    }

    def exception = {
      allocated = true
      new RuntimeException("exception")
    }

    // test varargs and Iterable overloads
    for {
      _ <- tracer.span("span", attribute: _*).use { span =>
        for {
          _ <- span.addAttributes(attribute: _*)
          _ <- span.addAttributes(attribute)
          _ <- span.addEvent(text, attribute: _*)
          _ <- span.addEvent(text, attribute)
          _ <- span.addEvent(text, timestamp, attribute: _*)
          _ <- span.addEvent(text, timestamp, attribute)
          _ <- span.recordException(exception, attribute: _*)
          _ <- span.recordException(exception, attribute)
          _ <- span.setStatus(status)
          _ <- span.setStatus(status, text)
        } yield ()
      }
      _ <- tracer.span("span", attribute).use_
      _ <- tracer.rootSpan("span", attribute: _*).use { span =>
        for {
          _ <- span.addAttributes(attribute: _*)
          _ <- span.addAttributes(attribute)
          _ <- span.addEvent(text, attribute: _*)
          _ <- span.addEvent(text, attribute)
          _ <- span.addEvent(text, timestamp, attribute: _*)
          _ <- span.addEvent(text, timestamp, attribute)
          _ <- span.recordException(exception, attribute: _*)
          _ <- span.recordException(exception, attribute)
          _ <- span.setStatus(status)
          _ <- span.setStatus(status, text)
        } yield ()
      }
      _ <- tracer.rootSpan("span", attribute).use_
    } yield assert(!allocated)
  }

  test("`currentSpanOrNoop` is not valid when instrument is noop") {
    val tracer = Tracer.noop[IO]
    for (span <- tracer.currentSpanOrNoop)
      yield assert(!span.context.isValid)
  }
}
