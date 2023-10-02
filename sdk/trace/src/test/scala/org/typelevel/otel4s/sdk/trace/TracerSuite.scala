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

package org.typelevel.otel4s.sdk.trace

import cats.effect.IO
import cats.effect.IOLocal
import cats.effect.MonadCancelThrow
import cats.effect.Resource
import cats.effect.std.Random
import cats.effect.testkit.TestControl
import cats.syntax.apply._
import cats.syntax.functor._
import cats.~>
import munit.CatsEffectSuite
import munit.Location
import munit.TestOptions
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.TextMapPropagator
import org.typelevel.otel4s.sdk.Attributes
import org.typelevel.otel4s.sdk.common.InstrumentationScopeInfo
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.propagation.PassThroughPropagator
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.data.StatusData
import org.typelevel.otel4s.sdk.trace.exporter.BatchSpanProcessor
import org.typelevel.otel4s.sdk.trace.exporter.InMemorySpanExporter
import org.typelevel.otel4s.sdk.trace.propagation.W3CTraceContextPropagator
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.Status
import org.typelevel.otel4s.trace.Tracer
import org.typelevel.otel4s.trace.TracerProvider

import scala.concurrent.duration._
import scala.util.control.NoStackTrace

class TracerSuite extends CatsEffectSuite {

  sdkTest("propagate instrumentation info") { sdk =>
    val expected = InstrumentationScopeInfo.create(
      name = "tracer",
      version = Some("1.0"),
      schemaUrl = Some("https://localhost:8080"),
      attributes = Attributes.Empty
    )

    for {
      tracer <- sdk.provider
        .tracer("tracer")
        .withVersion("1.0")
        .withSchemaUrl("https://localhost:8080")
        .get

      _ <- tracer.span("span").use_

      spans <- sdk.finishedSpans
    } yield assertEquals(
      spans.map(_.instrumentationScopeInfo),
      List(expected)
    )
  }

  sdkTest("update span name") { sdk =>
    for {
      tracer <- sdk.provider.get("tracer")
      _ <- tracer.span("span").use(span => span.updateName("span-use"))
      spans <- sdk.finishedSpans
    } yield assertEquals(spans.map(_.name), List("span-use"))
  }

  sdkTest("set attributes only once") { sdk =>
    val key = "string-attribute"
    val value = "value"
    val attribute = Attribute(key, value)

    for {
      tracer <- sdk.provider.get("tracer")
      _ <- tracer.span("span", attribute).use_
      spans <- sdk.finishedSpans
    } yield {
      assertEquals(spans.flatMap(_.attributes.toList), List(attribute))
    }
  }

  sdkTest("propagate traceId and spanId") { sdk =>
    for {
      tracer <- sdk.provider.get("tracer")
      _ <- tracer.currentSpanContext.assertEquals(None)
      result <- tracer.span("span").use { span =>
        for {
          _ <- tracer.currentSpanContext.assertEquals(Some(span.context))
          span2 <- tracer.span("span-2").use { span2 =>
            for {
              _ <- tracer.currentSpanContext.assertEquals(Some(span2.context))
            } yield span2
          }
        } yield (span, span2)
      }
      spans <- sdk.finishedSpans
    } yield {
      val (span, span2) = result
      assertEquals(span.context.traceIdHex, span2.context.traceIdHex)
      assertEquals(
        spans.map(_.spanContext),
        List(span2.context, span.context)
      )
      assertEquals(
        spans.map(_.spanContext),
        List(span2.context, span.context)
      )
    }
  }

  sdkTest("propagate attributes") { sdk =>
    val attribute = Attribute("string-attribute", "value")

    for {
      tracer <- sdk.provider.get("tracer")
      span <- tracer.span("span", attribute).use(IO.pure)
      spans <- sdk.finishedSpans
    } yield {
      assertEquals(
        spans.map(_.spanContext.traceIdHex),
        List(span.context.traceIdHex)
      )
      assertEquals(
        spans.map(_.spanContext.spanIdHex),
        List(span.context.spanIdHex)
      )
      assertEquals(spans.flatMap(_.attributes.toList), List(attribute))
    }
  }

  sdkTest("propagate to an arbitrary carrier") { sdk =>
    TestControl.executeEmbed {
      for {
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.span("span").use { span =>
          for {
            carrier <- tracer.propagate(Map("key" -> "value"))
          } yield {
            assertEquals(carrier.get("key"), Some("value"))
            val expected =
              s"00-${span.context.traceIdHex}-${span.context.spanIdHex}-01"
            assertEquals(carrier.get("traceparent"), Some(expected))
          }
        }
      } yield ()
    }
  }

  sdkTest("automatically start and stop span") { sdk =>
    val sleepDuration = 500.millis

    TestControl.executeEmbed {
      for {
        tracer <- sdk.provider.get("tracer")
        now <- IO.monotonic.delayBy(1.millis) // otherwise returns 0
        _ <- tracer.span("span").surround(IO.sleep(sleepDuration))
        spans <- sdk.finishedSpans
      } yield {
        assertEquals(spans.map(_.startEpochNanos), List(now.toNanos))
        assertEquals(
          spans.map(_.endEpochNanos),
          List(now.plus(sleepDuration).toNanos)
        )
      }
    }
  }

  sdkTest("set error status on abnormal termination (canceled)") { sdk =>
    for {
      tracer <- sdk.provider.get("tracer")
      fiber <- tracer.span("span").surround(IO.canceled).start
      _ <- fiber.joinWith(IO.unit)
      spans <- sdk.finishedSpans
    } yield {
      assertEquals(
        spans.map(_.status),
        List(StatusData.create(Status.Error, "canceled"))
      )
      assertEquals(spans.map(_.events.isEmpty), List(true))
    }
  }

  sdkTest("set error status on abnormal termination (exception)") { sdk =>
    final case class Err(reason: String)
        extends RuntimeException(reason)
        with NoStackTrace

    val exception = Err("error")

    def expected(epoch: Long) =
      EventData.exception(
        SpanLimits.Default,
        epoch,
        exception,
        Attributes.Empty
      )

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.span("span").surround(IO.raiseError(exception)).attempt
        spans <- sdk.finishedSpans
      } yield {
        assertEquals(spans.map(_.status), List(StatusData.Error))
        assertEquals(
          spans.map(_.events),
          List(List(expected(now.toNanos)))
        )
      }

    }
  }

  sdkTest("create root span explicitly") { sdk =>
    def expected(now: FiniteDuration) =
      List(
        SpanNode("span-2", now, now, Nil),
        SpanNode("span-1", now, now, Nil)
      )

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.span("span-1").use { span1 =>
          tracer.rootSpan("span-2").use { span2 =>
            for {
              _ <- tracer.currentSpanContext.assertEquals(Some(span2.context))
              _ <- IO(assertIdsNotEqual(span1, span2))
            } yield ()
          }
        }
        spans <- sdk.finishedSpans
        tree <- IO.pure(SpanNode.fromSpans(spans))
        //  _ <- IO.println(tree.map(SpanNode.render).mkString("\n"))
      } yield assertEquals(tree, expected(now))
    }
  }

  sdkTest("create a new root scope") { sdk =>
    def expected(now: FiniteDuration) =
      List(
        SpanNode("span-2", now, now, Nil),
        SpanNode("span-1", now, now, Nil)
      )

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.currentSpanContext.assertEquals(None)
        _ <- tracer.span("span-1").use { span1 =>
          for {
            _ <- tracer.currentSpanContext.assertEquals(Some(span1.context))
            _ <- tracer.rootScope {
              for {
                _ <- tracer.currentSpanContext.assertEquals(None)
                // a new root span should be created
                _ <- tracer.span("span-2").use { span2 =>
                  for {
                    _ <- IO(assertIdsNotEqual(span1, span2))
                    _ <- tracer.currentSpanContext
                      .assertEquals(Some(span2.context))
                  } yield ()
                }
              } yield ()
            }
          } yield ()
        }
        spans <- sdk.finishedSpans
        tree <- IO.pure(SpanNode.fromSpans(spans))
        //  _ <- IO.println(tree.map(SpanNode.render).mkString("\n"))
      } yield assertEquals(tree, expected(now))
    }
  }

  // todo: should pass
  sdkTest("create a no-op scope".ignore) { sdk =>
    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.currentSpanContext.assertEquals(None)
        _ <- tracer.span("span-1").use { span =>
          for {
            _ <- tracer.currentSpanContext.assertEquals(Some(span.context))
            _ <- tracer.noopScope {
              for {
                _ <- tracer.currentSpanContext.assertEquals(None)
                // a new root span should not be created
                _ <- tracer
                  .span("span-2")
                  .use(_ => tracer.currentSpanContext.assertEquals(None))
                _ <- tracer
                  .rootSpan("span-3")
                  .use(_ => tracer.currentSpanContext.assertEquals(None))
                _ <- tracer.rootScope {
                  tracer
                    .span("span-4")
                    .use(_ => tracer.currentSpanContext.assertEquals(None))
                }
              } yield ()
            }
          } yield ()
        }
        spans <- sdk.finishedSpans
        tree <- IO.pure(SpanNode.fromSpans(spans))
        // _ <- IO.println(tree.map(SpanNode.render).mkString("\n"))
      } yield assertEquals(tree, List(SpanNode("span-1", now, now, Nil)))
    }
  }

  sdkTest("create a new scope with a custom parent") { sdk =>
    def expected(now: FiniteDuration) =
      SpanNode(
        "span",
        now,
        now,
        List(
          SpanNode("span-3", now, now, Nil),
          SpanNode("span-2", now, now, Nil)
        )
      )

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.currentSpanContext.assertEquals(None)
        _ <- tracer.span("span").use { span =>
          for {
            _ <- tracer.currentSpanContext.assertEquals(Some(span.context))
            _ <- tracer.span("span-2").use { span2 =>
              for {
                _ <- tracer.currentSpanContext
                  .assertEquals(Some(span2.context))
                _ <- tracer.childScope(span.context) {
                  for {
                    _ <- tracer.currentSpanContext
                      .assertEquals(Some(span.context))
                    _ <- tracer.span("span-3").use { span3 =>
                      tracer.currentSpanContext
                        .assertEquals(Some(span3.context))
                    }
                  } yield ()

                }
              } yield ()
            }
          } yield ()
        }
        spans <- sdk.finishedSpans
        tree <- IO.pure(SpanNode.fromSpans(spans))
        // _ <- IO.println(tree.map(SpanNode.render).mkString("\n"))
      } yield assertEquals(tree, List(expected(now)))
    }
  }

  sdkTest("create a span with a custom parent (via builder)") { sdk =>
    def expected(now: FiniteDuration) =
      SpanNode(
        "span",
        now,
        now,
        List(
          SpanNode("span-3", now, now, Nil),
          SpanNode("span-2", now, now, Nil)
        )
      )

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.currentSpanContext.assertEquals(None)
        _ <- tracer.span("span").use { span =>
          for {
            _ <- tracer.currentSpanContext.assertEquals(Some(span.context))
            _ <- tracer.span("span-2").use { span2 =>
              for {
                _ <- tracer.currentSpanContext
                  .assertEquals(Some(span2.context))
                _ <- tracer
                  .spanBuilder("span-3")
                  .withParent(span.context)
                  .build
                  .use { span3 =>
                    tracer.currentSpanContext
                      .assertEquals(Some(span3.context))
                  }
              } yield ()
            }
          } yield ()
        }
        spans <- sdk.finishedSpans
        tree <- IO.pure(SpanNode.fromSpans(spans))
        // _ <- IO.println(tree.map(SpanNode.render).mkString("\n"))
      } yield assertEquals(tree, List(expected(now)))
    }
  }

  sdkTest("trace resource with use") { sdk =>
    val attribute = Attribute("string-attribute", "value")

    val acquireInnerDuration = 25.millis
    val body1Duration = 100.millis
    val body2Duration = 200.millis
    val body3Duration = 50.millis
    val releaseDuration = 300.millis

    def mkRes(tracer: Tracer[IO]): Resource[IO, Unit] =
      Resource
        .make(
          tracer.span("acquire-inner").surround(IO.sleep(acquireInnerDuration))
        )(_ => IO.sleep(releaseDuration))

    def expected(start: FiniteDuration) = {
      val acquireEnd = start + acquireInnerDuration

      val (body1Start, body1End) = (acquireEnd, acquireEnd + body1Duration)
      val (body2Start, body2End) = (body1End, body1End + body2Duration)
      val (body3Start, body3End) = (body2End, body2End + body3Duration)

      val end = body3End + releaseDuration

      SpanNode(
        "resource-span",
        start,
        end,
        List(
          SpanNode(
            "acquire",
            start,
            acquireEnd,
            List(
              SpanNode(
                "acquire-inner",
                start,
                acquireEnd,
                Nil
              )
            )
          ),
          SpanNode(
            "use",
            body1Start,
            body3End,
            List(
              SpanNode("body-1", body1Start, body1End, Nil),
              SpanNode("body-2", body2Start, body2End, Nil),
              SpanNode("body-3", body3Start, body3End, Nil)
            )
          ),
          SpanNode(
            "release",
            body3End,
            end,
            Nil
          )
        )
      )
    }

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracer <- sdk.provider.get("tracer")
        _ <- wrapResource(tracer, mkRes(tracer), attribute)
          .use {
            _ {
              tracer.span("use").surround {
                for {
                  _ <- tracer.span("body-1").surround(IO.sleep(100.millis))
                  _ <- tracer.span("body-2").surround(IO.sleep(200.millis))
                  _ <- tracer.span("body-3").surround(IO.sleep(50.millis))
                } yield ()
              }
            }
          }
        spans <- sdk.finishedSpans
        tree <- IO.pure(SpanNode.fromSpans(spans))
      } yield assertEquals(tree, List(expected(now)))
    }
  }

  sdkTest("trace resource with surround") { sdk =>
    val acquireDuration = 25.millis
    val bodyDuration = 100.millis
    val releaseDuration = 300.millis

    def mkRes: Resource[IO, Unit] =
      Resource.make(IO.sleep(acquireDuration))(_ => IO.sleep(releaseDuration))

    def expected(start: FiniteDuration) = {
      val acquireEnd = start + acquireDuration
      val (bodyStart, bodyEnd) = (acquireEnd, acquireEnd + bodyDuration)
      val end = bodyEnd + releaseDuration

      SpanNode(
        "resource-span",
        start,
        end,
        List(
          SpanNode(
            "acquire",
            start,
            acquireEnd,
            Nil
          ),
          SpanNode(
            "use",
            bodyStart,
            bodyEnd,
            List(
              SpanNode("body", bodyStart, bodyEnd, Nil)
            )
          ),
          SpanNode(
            "release",
            bodyEnd,
            end,
            Nil
          )
        )
      )
    }

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracer <- sdk.provider.tracer("tracer").get
        _ <- wrapResource(tracer, mkRes)
          .use { trace =>
            trace {
              tracer.span("use").surround {
                tracer.span("body").surround(IO.sleep(100.millis))
              }
            }
          }
        spans <- sdk.finishedSpans
        tree <- IO.pure(SpanNode.fromSpans(spans))
      } yield assertEquals(tree, List(expected(now)))
    }
  }

  sdkTest("trace resource with use_") { sdk =>
    val acquireDuration = 25.millis
    val releaseDuration = 300.millis

    def mkRes: Resource[IO, Unit] =
      Resource.make(IO.sleep(acquireDuration))(_ => IO.sleep(releaseDuration))

    def expected(start: FiniteDuration) = {
      val acquireEnd = start + acquireDuration
      val end = acquireEnd + releaseDuration

      SpanNode(
        "resource-span",
        start,
        end,
        List(
          SpanNode(
            "acquire",
            start,
            acquireEnd,
            Nil
          ),
          SpanNode(
            "use",
            acquireEnd,
            acquireEnd,
            Nil
          ),
          SpanNode(
            "release",
            acquireEnd,
            end,
            Nil
          )
        )
      )
    }

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracer <- sdk.provider.tracer("tracer").get
        _ <- wrapResource(tracer, mkRes)
          .use {
            _(tracer.span("use").use_)
          }
        spans <- sdk.finishedSpans
        tree <- IO.pure(SpanNode.fromSpans(spans))
      } yield assertEquals(tree, List(expected(now)))
    }
  }

  sdkTest("startUnmanaged: respect builder start time") { sdk =>
    val expected = SpanNode(
      name = "span",
      start = 100.millis,
      end = 200.millis,
      children = Nil
    )

    TestControl.executeEmbed {
      for {
        tracer <- sdk.provider.get("tracer")
        span <- tracer
          .spanBuilder("span")
          .withStartTimestamp(100.millis)
          .build
          .startUnmanaged

        // the sleep time should be ignored since the end timestamp is specified explicitly
        _ <- IO.sleep(100.millis)

        _ <- span.end(200.millis)
        spans <- sdk.finishedSpans
        tree <- IO.pure(SpanNode.fromSpans(spans))
        // _ <- IO.println(tree.map(SpanNode.render).mkString("\n"))
      } yield assertEquals(tree, List(expected))
    }
  }

  sdkTest(
    "startUnmanaged: use Clock[F].realTime to set start and end if builder's time is undefined"
  ) { sdk =>
    def expected(now: FiniteDuration) =
      SpanNode(
        name = "span",
        start = now,
        end = now.plus(100.millis),
        children = Nil
      )

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracer <- sdk.provider.get("tracer")
        span <- tracer.spanBuilder("span").build.startUnmanaged

        _ <- IO.sleep(100.millis)

        _ <- span.end
        spans <- sdk.finishedSpans
        tree <- IO.pure(SpanNode.fromSpans(spans))
        // _ <- IO.println(tree.map(SpanNode.render).mkString("\n"))
      } yield assertEquals(tree, List(expected(now)))
    }
  }

  // external span does not appear in the recorded spans of the in-memory sdk
  sdkTest(
    "joinOrRoot: join an external span when can be extracted",
    additionalPropagators = List(PassThroughPropagator.create("foo", "bar"))
  ) { sdk =>
    val traceId = "84b54e9330faae5350f0dd8673c98146"
    val spanId = "279fa73bc935cc05"

    val headers = Map(
      "traceparent" -> s"00-$traceId-$spanId-01",
      "foo" -> "1",
      "baz" -> "2"
    )

    TestControl.executeEmbed {
      for {
        tracer <- sdk.provider.get("tracer")
        tuple <- tracer.span("local").surround {
          tracer.joinOrRoot(headers) {
            (
              tracer.currentSpanContext,
              tracer.span("inner").use(r => IO.pure(r.context)),
              tracer.propagate(Map.empty[String, String]),
            ).tupled
          }
        }
        (external, inner, resultHeaders) = tuple
      } yield {
        assertEquals(external.map(_.traceIdHex), Some(traceId))
        assertEquals(external.map(_.spanIdHex), Some(spanId))
        assertEquals(inner.traceIdHex, traceId)
        assertEquals(resultHeaders.size, 2)
        assertEquals(
          resultHeaders.get("traceparent"),
          Some(s"00-$traceId-$spanId-01")
        )
        assertEquals(resultHeaders.get("foo"), Some("1"))
      }
    }
  }

  sdkTest(
    "joinOrRoot: ignore an external span when cannot be extracted and start a root span"
  ) { sdk =>
    val traceId = "84b54e9330faae5350f0dd8673c98146"
    val spanId = "279fa73bc935cc05"

    val headers = Map(
      "some_random_header" -> s"00-$traceId-$spanId-01"
    )

    // we must always start a root span
    def expected(now: FiniteDuration) = List(
      SpanNode(
        name = "inner",
        start = now.plus(500.millis),
        end = now.plus(500.millis).plus(200.millis),
        children = Nil
      ),
      SpanNode(
        name = "local",
        start = now,
        end = now.plus(500.millis).plus(200.millis),
        children = Nil
      )
    )

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.span("local").surround {
          tracer
            .joinOrRoot(headers) {
              tracer.span("inner").surround(IO.sleep(200.millis))
            }
            .delayBy(500.millis)
        }
        spans <- sdk.finishedSpans
        tree <- IO.pure(SpanNode.fromSpans(spans))
        // _ <- IO.println(tree.map(SpanNode.render).mkString("\n"))
      } yield assertEquals(tree, expected(now))
    }
  }

  /*
  test("propagate trace info over stream scopes") {
    def expected(now: FiniteDuration) =
      SpanNode(
        "span",
        now,
        now,
        List(
          SpanNode("span-3", now, now, Nil),
          SpanNode("span-2", now, now, Nil)
        )
      )

    def flow(tracer: Tracer[IO]): Stream[IO, Unit] =
      for {
        span <- Stream.resource(tracer.span("span"))
        _ <- Stream.eval(
          tracer.currentSpanContext.assertEquals(Some(span.context))
        )
        span2 <- Stream.resource(tracer.span("span-2"))
        _ <- Stream.eval(
          tracer.currentSpanContext.assertEquals(Some(span2.context))
        )
        span3 <- Stream.resource(
          tracer.spanBuilder("span-3").withParent(span.context).start
        )
        _ <- Stream.eval(
          tracer.currentSpanContext.assertEquals(Some(span3.context))
        )
      } yield ()

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        sdk <- makeSdk()
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.currentSpanContext.assertEquals(None)
        _ <- flow(tracer).compile.drain
        _ <- tracer.currentSpanContext.assertEquals(None)
        spans <- sdk.finishedSpans
        tree <- IO.pure(SpanNode.fromSpans(spans))
        // _ <- IO.println(tree.map(SpanNode.render).mkString("\n"))
      } yield assertEquals(tree, List(expected(now)))
    }
   }
   */

  sdkTest(
    "setting attributes using builder does not remove previous ones"
  ) { sdk =>
    val previousAttribute = Attribute("previous-attribute", "previous-value")
    val newAttribute = Attribute("new-attribute", "new-value")

    for {
      tracer <- sdk.provider.get("tracer")
      _ <- tracer
        .spanBuilder("span")
        .addAttribute(previousAttribute)
        .addAttributes(newAttribute)
        .build
        .use_
      spans <- sdk.finishedSpans
    } yield assertEquals(
      spans.flatMap(_.attributes.toList),
      List(previousAttribute, newAttribute)
    )
  }

  // typelevel/otel4s#277
  sdkTest(
    "retain all of a provided context through propagation",
    additionalPropagators = List(PassThroughPropagator.create("foo", "bar"))
  ) { sdk =>
    TestControl.executeEmbed {
      for {
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.joinOrRoot(Map("foo" -> "1", "baz" -> "2")) {
          for {
            carrier <- tracer.propagate(Map.empty[String, String])
          } yield {
            assertEquals(carrier.size, 1)
            assertEquals(carrier.get("foo"), Some("1"))
          }
        }
      } yield ()
    }
  }

  private def wrapResource[F[_]: MonadCancelThrow, A](
      tracer: Tracer[F],
      resource: Resource[F, A],
      attributes: Attribute[_]*
  ): Resource[F, F ~> F] = {
    tracer
      .span("resource-span", attributes: _*)
      .resource
      .flatMap { res =>
        Resource[F, A] {
          res.trace {
            tracer
              .span("acquire")
              .surround {
                resource.allocated.map { case (acquired, release) =>
                  acquired -> res.trace(
                    tracer.span("release").surround(release)
                  )
                }
              }
          }
        }.as(res.trace)
      }
  }

  private def assertIdsNotEqual(s1: Span[IO], s2: Span[IO]): Unit = {
    assertNotEquals(s1.context.traceIdHex, s2.context.traceIdHex)
    assertNotEquals(s1.context.spanIdHex, s2.context.spanIdHex)
  }

  private def sdkTest[A](
      options: TestOptions,
      additionalPropagators: List[TextMapPropagator[Context]] = Nil
  )(body: TracerSuite.Sdk => IO[A])(implicit loc: Location): Unit =
    test(options)(makeSdk(additionalPropagators).use(body))

  private def makeSdk(
      additionalPropagators: List[TextMapPropagator[Context]]
  ): Resource[IO, TracerSuite.Sdk] = {
    import org.typelevel.otel4s.sdk.instances._

    val textMapPropagators =
      List(W3CTraceContextPropagator) ++ additionalPropagators

    def createTracerProvider(
        processor: SpanProcessor[IO]
    )(implicit ioLocal: IOLocal[Context]): IO[SdkTracerProvider[IO]] =
      Random.scalaUtilRandom[IO].map { implicit random =>
        SdkTracerProvider
          .builder[IO]
          .addTextMapPropagators(textMapPropagators: _*)
          .addSpanProcessor(processor)
          .build
      }

    for {
      ioLocal <- Resource.eval(IOLocal(Context.root))
      exporter <- Resource.eval(InMemorySpanExporter.create[IO])
      processor <- BatchSpanProcessor.builder(exporter).build
      tracerProvider <- Resource.eval(createTracerProvider(processor)(ioLocal))
    } yield new TracerSuite.Sdk(tracerProvider, processor, exporter)
  }

}

object TracerSuite {

  class Sdk(
      val provider: TracerProvider[IO],
      processor: BatchSpanProcessor[IO],
      exporter: InMemorySpanExporter[IO]
  ) {

    def finishedSpans: IO[List[SpanData]] =
      processor.exportAll >> exporter.finishedSpans.map(_.toList)

  }

}
