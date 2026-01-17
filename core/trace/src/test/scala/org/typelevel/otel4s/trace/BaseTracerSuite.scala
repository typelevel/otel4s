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

package org.typelevel.otel4s.trace

import cats.data.EitherT
import cats.data.IorT
import cats.data.Kleisli
import cats.data.OptionT
import cats.data.WriterT
import cats.effect.IO
import cats.effect.MonadCancelThrow
import cats.effect.Resource
import cats.effect.SyncIO
import cats.effect.testkit.TestControl
import cats.syntax.apply._
import cats.syntax.functor._
import cats.~>
import munit.CatsEffectSuite
import munit.Location
import munit.TestOptions
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.context.Contextual
import org.typelevel.otel4s.context.Key
import org.typelevel.otel4s.context.propagation.PassThroughPropagator
import org.typelevel.otel4s.context.propagation.TextMapPropagator

import scala.concurrent.duration._

abstract class BaseTracerSuite[Ctx, K[X] <: Key[X]](implicit
    c: Contextual.Keyed[Ctx, K],
    kp: Key.Provider[SyncIO, K]
) extends CatsEffectSuite {
  import BaseTracerSuite.Sdk
  import BaseTracerSuite.SpanInfo
  import BaseTracerSuite.SpanDataWrapper
  import BaseTracerSuite.NestedSurround

  type SpanData
  type Builder

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
        assertEquals(spans.map(_.startTimestamp), List(now))
        assertEquals(
          spans.map(_.endTimestamp),
          List(Some(now.plus(sleepDuration)))
        )
      }
    }
  }

  sdkTest("create root span explicitly") { sdk =>
    def expected(now: FiniteDuration) =
      List(
        SpanTree(SpanInfo("span-2", now, now)),
        SpanTree(SpanInfo("span-1", now, now))
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
        tree <- IO.pure(treeOf(spans))
        //  _ <- IO.println(tree.map(renderTree).mkString("\n"))
      } yield assertEquals(tree, expected(now))
    }
  }

  sdkTest("create a new root scope") { sdk =>
    def expected(now: FiniteDuration) =
      List(
        SpanTree(SpanInfo("span-2", now, now)),
        SpanTree(SpanInfo("span-1", now, now))
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
        tree <- IO.pure(treeOf(spans))
        //  _ <- IO.println(tree.map(renderTree).mkString("\n"))
      } yield assertEquals(tree, expected(now))
    }
  }

  sdkTest("create a no-op scope") { sdk =>
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
        tree <- IO.pure(treeOf(spans))
        // _ <- IO.println(tree.map(renderTree).mkString("\n"))
      } yield assertEquals(tree, List(SpanTree(SpanInfo("span-1", now, now))))
    }
  }

  sdkTest("`currentSpanOrNoop` outside of a span (root scope)") { sdk =>
    for {
      tracer <- sdk.provider.get("tracer")
      currentSpan <- tracer.currentSpanOrNoop
      _ <- currentSpan.addAttribute(Attribute("string-attribute", "value"))
      spans <- sdk.finishedSpans
    } yield {
      assert(!currentSpan.context.isValid)
      assertEquals(spans.length, 0)
    }
  }

  sdkTest("`currentSpanOrNoop` in noop scope") { sdk =>
    for {
      tracer <- sdk.provider.get("tracer")
      _ <- tracer.noopScope {
        for {
          currentSpan <- tracer.currentSpanOrNoop
          _ <- currentSpan.addAttribute(Attribute("string-attribute", "value"))
        } yield assert(!currentSpan.context.isValid)
      }
      spans <- sdk.finishedSpans
    } yield assertEquals(spans.length, 0)
  }

  sdkTest("`currentSpanOrNoop` inside a span") { sdk =>
    def expected(now: FiniteDuration) =
      List(SpanTree(SpanInfo("span", now, now)))

    val attribute =
      Attribute("string-attribute", "value")

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.span("span").surround {
          for {
            currentSpan <- tracer.currentSpanOrNoop
            _ <- currentSpan.addAttribute(attribute)
          } yield assert(currentSpan.context.isValid)
        }
        spans <- sdk.finishedSpans
        tree <- IO.pure(treeOf(spans))
        // _ <- IO.println(tree.map(renderTree).mkString("\n"))
      } yield {
        assertEquals(tree, expected(now))
        assertEquals(spans.map(_.attributes), List(Attributes(attribute)))
      }
    }
  }

  sdkTest("`withCurrentSpan` outside of a span (root scope)") { sdk =>
    for {
      tracer <- sdk.provider.get("tracer")
      isValid <- tracer.withCurrentSpan(span => IO.pure(span.context.isValid))
      spans <- sdk.finishedSpans
    } yield {
      assert(!isValid)
      assertEquals(spans.length, 0)
    }
  }

  sdkTest("`withCurrentSpan` in noop scope") { sdk =>
    for {
      tracer <- sdk.provider.get("tracer")
      isValid <- tracer.noopScope {
        tracer.withCurrentSpan(span => IO.pure(span.context.isValid))
      }
      spans <- sdk.finishedSpans
    } yield {
      assert(!isValid)
      assertEquals(spans.length, 0)
    }
  }

  sdkTest("`withCurrentSpan` inside a span") { sdk =>
    def expected(now: FiniteDuration) =
      List(SpanTree(SpanInfo("span", now, now)))

    val attribute =
      Attribute("string-attribute", "value")

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.span("span").surround {
          tracer.withCurrentSpan { span =>
            span.addAttribute(attribute) >> IO(assert(span.context.isValid))
          }
        }
        spans <- sdk.finishedSpans
        tree <- IO.pure(treeOf(spans))
      } yield {
        assertEquals(tree, expected(now))
        assertEquals(spans.map(_.attributes), List(Attributes(attribute)))
      }
    }
  }

  sdkTest("`currentSpanOrThrow` outside of a span (root scope)") { sdk =>
    {
      for {
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.currentSpanOrThrow
        _ <- IO.raiseError[Unit](
          new AssertionError("did not throw for `currentSpanOrThrow`")
        )
      } yield ()
    }.recover { case _: IllegalStateException => () }
  }

  sdkTest("`currentSpanOrThrow` in noop scope") { sdk =>
    for {
      tracer <- sdk.provider.get("tracer")
      _ <- tracer.noopScope {
        for {
          currentSpan <- tracer.currentSpanOrThrow
          _ <- currentSpan.addAttribute(Attribute("string-attribute", "value"))
        } yield assert(!currentSpan.context.isValid)
      }
      spans <- sdk.finishedSpans
    } yield assertEquals(spans.length, 0)
  }

  sdkTest("`currentSpanOrThrow` inside a span") { sdk =>
    def expected(now: FiniteDuration) =
      List(SpanTree(SpanInfo("span", now, now)))

    val attribute =
      Attribute("string-attribute", "value")

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.span("span").surround {
          for {
            currentSpan <- tracer.currentSpanOrThrow
            _ <- currentSpan.addAttribute(attribute)
          } yield assert(currentSpan.context.isValid)
        }
        spans <- sdk.finishedSpans
        tree <- IO.pure(treeOf(spans))
        // _ <- IO.println(tree.map(renderTree).mkString("\n"))
      } yield {
        assertEquals(tree, expected(now))
        assertEquals(spans.map(_.attributes), List(Attributes(attribute)))
      }
    }
  }

  sdkTest("create a new scope with a custom parent") { sdk =>
    def expected(now: FiniteDuration) =
      SpanTree(
        SpanInfo("span", now, now),
        List(
          SpanTree(SpanInfo("span-3", now, now)),
          SpanTree(SpanInfo("span-2", now, now))
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
        tree <- IO.pure(treeOf(spans))
        // _ <- IO.println(tree.map(renderTree).mkString("\n"))
      } yield assertEquals(tree, List(expected(now)))
    }
  }

  sdkTest("create a span with a custom parent (via builder)") { sdk =>
    def expected(now: FiniteDuration) =
      SpanTree(
        SpanInfo("span", now, now),
        List(
          SpanTree(SpanInfo("span-3", now, now)),
          SpanTree(SpanInfo("span-2", now, now))
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
        tree <- IO.pure(treeOf(spans))
        // _ <- IO.println(tree.map(renderTree).mkString("\n"))
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

      SpanTree(
        SpanInfo("resource-span", start, end),
        List(
          SpanTree(
            SpanInfo("acquire", start, acquireEnd),
            List(SpanTree(SpanInfo("acquire-inner", start, acquireEnd)))
          ),
          SpanTree(
            SpanInfo("use", body1Start, body3End),
            List(
              SpanTree(SpanInfo("body-1", body1Start, body1End)),
              SpanTree(SpanInfo("body-2", body2Start, body2End)),
              SpanTree(SpanInfo("body-3", body3Start, body3End))
            )
          ),
          SpanTree(SpanInfo("release", body3End, end))
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
        tree <- IO.pure(treeOf(spans))
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

      SpanTree(
        SpanInfo("resource-span", start, end),
        List(
          SpanTree(SpanInfo("acquire", start, acquireEnd)),
          SpanTree(
            SpanInfo("use", bodyStart, bodyEnd),
            List(SpanTree(SpanInfo("body", bodyStart, bodyEnd)))
          ),
          SpanTree(SpanInfo("release", bodyEnd, end))
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
        tree <- IO.pure(treeOf(spans))
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

      SpanTree(
        SpanInfo("resource-span", start, end),
        List(
          SpanTree(SpanInfo("acquire", start, acquireEnd)),
          SpanTree(SpanInfo("use", acquireEnd, acquireEnd)),
          SpanTree(SpanInfo("release", acquireEnd, end))
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
        tree <- IO.pure(treeOf(spans))
      } yield assertEquals(tree, List(expected(now)))
    }
  }

  sdkTest("startUnmanaged: respect builder start time") { sdk =>
    val expected = SpanTree(
      SpanInfo(
        name = "span",
        start = 100.millis,
        end = 200.millis
      )
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
        tree <- IO.pure(treeOf(spans))
        // _ <- IO.println(tree.map(renderTree).mkString("\n"))
      } yield assertEquals(tree, List(expected))
    }
  }

  sdkTest(
    "startUnmanaged: use Clock[F].realTime to set start and end if builder's time is undefined"
  ) { sdk =>
    def expected(now: FiniteDuration) =
      SpanTree(
        SpanInfo(
          name = "span",
          start = now,
          end = now.plus(100.millis)
        )
      )

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracer <- sdk.provider.get("tracer")
        span <- tracer.spanBuilder("span").build.startUnmanaged

        _ <- IO.sleep(100.millis)

        _ <- span.end
        spans <- sdk.finishedSpans
        tree <- IO.pure(treeOf(spans))
        // _ <- IO.println(tree.map(renderTree).mkString("\n"))
      } yield assertEquals(tree, List(expected(now)))
    }
  }

  // external span does not appear in the recorded spans of the in-memory sdk
  sdkTest(
    "joinOrRoot: join an external span when can be extracted",
    additionalPropagators = List(PassThroughPropagator("foo", "bar"))
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
      SpanTree(
        SpanInfo(
          name = "inner",
          start = now.plus(500.millis),
          end = now.plus(500.millis).plus(200.millis)
        )
      ),
      SpanTree(
        SpanInfo(
          name = "local",
          start = now,
          end = now.plus(500.millis).plus(200.millis)
        )
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
        tree <- IO.pure(treeOf(spans))
        // _ <- IO.println(tree.map(renderTree).mkString("\n"))
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
        tree <- IO.pure(treeOf(spans))
        // _ <- IO.println(tree.map(renderTree).mkString("\n"))
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
      spans.flatMap(_.attributes).toSet,
      Set[Attribute[_]](previousAttribute, newAttribute)
    )
  }

  // typelevel/otel4s#277
  sdkTest(
    "retain all of a provided context through propagation",
    additionalPropagators = List(PassThroughPropagator("foo", "bar"))
  ) { sdk =>
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

  sdkTest("nested SpanOps#surround for Tracer[IO]") { sdk =>
    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracer <- sdk.provider.get("tracer")
        _ <- tracer
          .span("outer")
          .surround {
            for {
              _ <- IO.sleep(NestedSurround.preBodyDuration)
              _ <- tracer
                .span("body-1")
                .surround(IO.sleep(NestedSurround.body1Duration))
              _ <- tracer
                .span("body-2")
                .surround(IO.sleep(NestedSurround.body2Duration))
            } yield ()
          }
        spans <- sdk.finishedSpans
        tree <- IO.pure(treeOf(spans))
      } yield assertEquals(tree, List(NestedSurround.expected(now)))
    }
  }

  sdkTest("nested SpanOps#surround for liftTo[IO]") { sdk =>
    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracerIO <- sdk.provider.get("tracer")
        tracer = tracerIO.liftTo[IO]
        _ <- tracer
          .span("outer")
          .surround {
            for {
              _ <- IO.sleep(NestedSurround.preBodyDuration)
              _ <- tracer
                .span("body-1")
                .surround(IO.sleep(NestedSurround.body1Duration))
              _ <- tracer
                .span("body-2")
                .surround(IO.sleep(NestedSurround.body2Duration))
            } yield ()
          }
        spans <- sdk.finishedSpans
        tree <- IO.pure(treeOf(spans))
      } yield assertEquals(tree, List(NestedSurround.expected(now)))
    }
  }

  sdkTest("nested SpanOps#surround for liftTo[OptionT[IO, *]]") { sdk =>
    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracerIO <- sdk.provider.get("tracer")
        tracer = tracerIO.liftTo[OptionT[IO, *]]
        _ <- tracer
          .span("outer")
          .surround {
            for {
              _ <- OptionT.liftF(IO.sleep(NestedSurround.preBodyDuration))
              _ <- tracer
                .span("body-1")
                .surround(OptionT.liftF(IO.sleep(NestedSurround.body1Duration)))
              _ <- tracer
                .span("body-2")
                .surround(OptionT.liftF(IO.sleep(NestedSurround.body2Duration)))
            } yield ()
          }
          .value
        spans <- sdk.finishedSpans
        tree <- IO.pure(treeOf(spans))
      } yield assertEquals(tree, List(NestedSurround.expected(now)))
    }
  }

  sdkTest("nested SpanOps#surround for liftTo[EitherT[IO, String, *]]") { sdk =>
    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracerIO <- sdk.provider.get("tracer")
        tracer = tracerIO.liftTo[EitherT[IO, String, *]]
        _ <- tracer
          .span("outer")
          .surround {
            for {
              _ <- EitherT.liftF(IO.sleep(NestedSurround.preBodyDuration))
              _ <- tracer
                .span("body-1")
                .surround(EitherT.liftF(IO.sleep(NestedSurround.body1Duration)))
              _ <- tracer
                .span("body-2")
                .surround(EitherT.liftF(IO.sleep(NestedSurround.body2Duration)))
            } yield ()
          }
          .value
        spans <- sdk.finishedSpans
        tree <- IO.pure(treeOf(spans))
      } yield assertEquals(tree, List(NestedSurround.expected(now)))
    }
  }

  sdkTest("nested SpanOps#surround for liftTo[IorT[IO, String, *]]") { sdk =>
    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracerIO <- sdk.provider.get("tracer")
        tracer = tracerIO.liftTo[IorT[IO, String, *]]
        _ <- tracer
          .span("outer")
          .surround {
            for {
              _ <- IorT.liftF(IO.sleep(NestedSurround.preBodyDuration))
              _ <- tracer
                .span("body-1")
                .surround(IorT.liftF(IO.sleep(NestedSurround.body1Duration)))
              _ <- tracer
                .span("body-2")
                .surround(IorT.liftF(IO.sleep(NestedSurround.body2Duration)))
            } yield ()
          }
          .value
        spans <- sdk.finishedSpans
        tree <- IO.pure(treeOf(spans))
      } yield assertEquals(tree, List(NestedSurround.expected(now)))
    }
  }

  sdkTest("nested SpanOps#surround for liftTo[Kleisli[IO, String, *]]") { sdk =>
    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracerIO <- sdk.provider.get("tracer")
        tracer = tracerIO.liftTo[Kleisli[IO, String, *]]
        _ <- tracer
          .span("outer")
          .surround {
            for {
              _ <- Kleisli.liftF(IO.sleep(NestedSurround.preBodyDuration))
              _ <- tracer
                .span("body-1")
                .surround(Kleisli.liftF(IO.sleep(NestedSurround.body1Duration)))
              _ <- tracer
                .span("body-2")
                .surround(Kleisli.liftF(IO.sleep(NestedSurround.body2Duration)))
            } yield ()
          }
          .run("unused")
        spans <- sdk.finishedSpans
        tree <- IO.pure(treeOf(spans))
      } yield assertEquals(tree, List(NestedSurround.expected(now)))
    }
  }

  sdkTest("nested SpanOps#surround for liftTo[WriterT[IO, Int, *]]") { sdk =>
    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracerIO <- sdk.provider.get("tracer")
        tracer = tracerIO.liftTo[WriterT[IO, Int, *]]
        _ <- tracer
          .span("outer")
          .surround {
            for {
              _ <- WriterT.liftF[IO, Int, Unit](IO.sleep(NestedSurround.preBodyDuration))
              _ <- tracer
                .span("body-1")
                .surround(WriterT.liftF(IO.sleep(NestedSurround.body1Duration)))
              _ <- tracer
                .span("body-2")
                .surround(WriterT.liftF(IO.sleep(NestedSurround.body2Duration)))
            } yield ()
          }
          .run
        spans <- sdk.finishedSpans
        tree <- IO.pure(treeOf(spans))
      } yield assertEquals(tree, List(NestedSurround.expected(now)))
    }
  }

  sdkTest("nested SpanOps#surround for liftTo[Resource[IO, *]]") { sdk =>
    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracerIO <- sdk.provider.get("tracer")
        tracer = tracerIO.liftTo[Resource[IO, *]]
        _ <- tracer
          .span("outer")
          .surround {
            for {
              _ <- Resource.eval(IO.sleep(NestedSurround.preBodyDuration))
              _ <- tracer
                .span("body-1")
                .surround(Resource.eval(IO.sleep(NestedSurround.body1Duration)))
              _ <- tracer
                .span("body-2")
                .surround(Resource.eval(IO.sleep(NestedSurround.body2Duration)))
            } yield ()
          }
          .use_
        spans <- sdk.finishedSpans
        tree <- IO.pure(treeOf(spans))
      } yield assertEquals(tree, List(NestedSurround.expected(now)))
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
      .flatMap { case SpanOps.Res(_, trace) =>
        Resource[F, A] {
          trace {
            tracer
              .span("acquire")
              .surround {
                resource.allocated.map { case (acquired, release) =>
                  acquired -> trace(
                    tracer.span("release").surround(release)
                  )
                }
              }
          }
        }.as(trace)
      }
  }

  private def assertIdsNotEqual(s1: Span[IO], s2: Span[IO]): Unit = {
    assertNotEquals(s1.context.traceIdHex, s2.context.traceIdHex)
    assertNotEquals(s1.context.spanIdHex, s2.context.spanIdHex)
  }

  private def treeOf(
      spans: List[SpanDataWrapper[SpanData]]
  ): List[SpanTree[SpanInfo]] =
    SpanTree
      .of(spans)
      .map(_.map(s => SpanInfo(s.name, s.startTimestamp, s.endTimestamp)))

  def renderTree(tree: SpanTree[SpanInfo]): String = {
    def loop(input: SpanTree[SpanInfo], depth: Int): String = {
      val prefix = " " * depth
      val next =
        if (input.children.isEmpty) ""
        else " =>\n" + input.children.map(loop(_, depth + 2)).mkString("\n")

      val end = input.current.end.map(_.toNanos).getOrElse(0L)
      s"$prefix${input.current.name} ${input.current.start.toNanos} -> $end$next"
    }

    loop(tree, 0)
  }

  protected def sdkTest[A](
      options: TestOptions,
      configure: Builder => Builder = identity,
      additionalPropagators: List[TextMapPropagator[Ctx]] = Nil
  )(body: Sdk[SpanData] => IO[A])(implicit loc: Location): Unit =
    test(options)(makeSdk(configure, additionalPropagators).use(body))

  protected def makeSdk(
      configure: Builder => Builder,
      additionalPropagators: List[TextMapPropagator[Ctx]]
  ): Resource[IO, Sdk[SpanData]]

}

object BaseTracerSuite {

  trait SpanDataWrapper[A] {
    def underlying: A

    def name: String
    def spanContext: SpanContext
    def parentSpanContext: Option[SpanContext]
    def attributes: Attributes
    def startTimestamp: FiniteDuration
    def endTimestamp: Option[FiniteDuration]
  }

  implicit def spanDataWrapperLike[A]: SpanTree.SpanLike[SpanDataWrapper[A]] =
    SpanTree.SpanLike.make(
      _.spanContext.spanIdHex,
      _.parentSpanContext.filter(_.isValid).map(_.spanIdHex)
    )

  trait Sdk[SpanData] {
    def provider: TracerProvider[IO]
    def finishedSpans: IO[List[SpanDataWrapper[SpanData]]]
  }

  final case class SpanInfo(
      name: String,
      start: FiniteDuration,
      end: Option[FiniteDuration]
  )

  object SpanInfo {
    def apply(
        name: String,
        start: FiniteDuration,
        end: FiniteDuration
    ): SpanInfo =
      SpanInfo(name, start, Some(end))
  }

  private object NestedSurround {
    val preBodyDuration: FiniteDuration = 25.millis
    val body1Duration: FiniteDuration = 100.millis
    val body2Duration: FiniteDuration = 50.millis

    def expected(start: FiniteDuration): SpanTree[SpanInfo] = {
      val body1Start = start + preBodyDuration
      val body1End = body1Start + body1Duration
      val end = body1End + body2Duration

      SpanTree(
        SpanInfo("outer", start, end),
        List(
          SpanTree(SpanInfo("body-1", body1Start, body1End)),
          SpanTree(SpanInfo("body-2", body1End, end))
        )
      )
    }
  }

}
