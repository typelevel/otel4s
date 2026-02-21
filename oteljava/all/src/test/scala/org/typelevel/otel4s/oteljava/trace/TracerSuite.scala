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

package org.typelevel.otel4s.oteljava.trace

import cats.effect.IO
import cats.effect.Resource
import cats.effect.testkit.TestControl
import cats.syntax.functor._
import cats.syntax.parallel._
import fs2.Stream
import io.opentelemetry.api.common.{AttributeKey => JAttributeKey}
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.sdk.common.InstrumentationScopeInfo
import io.opentelemetry.sdk.common.internal.AttributesMap
import io.opentelemetry.sdk.testing.time.TestClock
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder
import io.opentelemetry.sdk.trace.SpanLimits
import io.opentelemetry.sdk.trace.data.ExceptionEventData
import io.opentelemetry.sdk.trace.data.StatusData
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.oteljava.AttributeConverters._
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.context.propagation.PropagatorConverters._
import org.typelevel.otel4s.oteljava.testkit.trace.TracesTestkit
import org.typelevel.otel4s.trace.BaseTracerSuite
import org.typelevel.otel4s.trace.BaseTracerSuite.SpanInfo
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanTree
import org.typelevel.otel4s.trace.Tracer
import org.typelevel.otel4s.trace.TracerProvider

import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.util.control.NoStackTrace

class TracerSuite extends BaseTracerSuite[Context, Context.Key] {
  import BaseTracerSuite.SpanDataWrapper

  type SpanData = io.opentelemetry.sdk.trace.data.SpanData
  type Builder = SdkTracerProviderBuilder

  sdkTest("propagate instrumentation info") { sdk =>
    val expected = InstrumentationScopeInfo
      .builder("tracer")
      .setVersion("1.0")
      .setSchemaUrl("https://localhost:8080")
      .build()

    for {
      tracer <- sdk.provider
        .tracer("tracer")
        .withVersion("1.0")
        .withSchemaUrl("https://localhost:8080")
        .get

      _ <- tracer.span("span").use_

      spans <- sdk.finishedSpans
    } yield assertEquals(
      spans.map(_.underlying.getInstrumentationScopeInfo),
      List(expected)
    )
  }

  sdkTest("set error status on abnormal termination (canceled)") { sdk =>
    for {
      tracer <- sdk.provider.get("tracer")
      fiber <- tracer.span("span").surround(IO.canceled).start
      _ <- fiber.joinWith(IO.unit)
      spans <- sdk.finishedSpans
    } yield {
      assertEquals(
        spans.map(_.underlying.getStatus),
        List(StatusData.create(StatusCode.ERROR, "canceled"))
      )
      assertEquals(spans.map(_.underlying.getEvents.isEmpty), List(true))
    }
  }

  sdkTest(
    "set error status on abnormal termination (exception)",
    _.setClock(TestClock.create(Instant.ofEpochMilli(1L)))
  ) { sdk =>
    val limits = SpanLimits.getDefault
    val exception = new RuntimeException("error") with NoStackTrace

    val stringWriter = new StringWriter()
    val printWriter = new PrintWriter(stringWriter)
    exception.printStackTrace(printWriter)

    val attributes = AttributesMap.create(
      limits.getMaxNumberOfAttributes.toLong,
      limits.getMaxAttributeValueLength
    )

    attributes.put(JAttributeKey.stringKey("exception.message"), exception.getMessage)
    attributes.put(JAttributeKey.stringKey("exception.stacktrace"), stringWriter.toString)

    def expected(epoch: Long) =
      ExceptionEventData.create(
        epoch,
        exception,
        attributes,
        attributes.size()
      )

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.milli) // otherwise returns 0
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.span("span").surround(IO.raiseError(exception)).attempt
        spans <- sdk.finishedSpans
      } yield {
        assertEquals(
          spans.map(_.underlying.getStatus),
          List(StatusData.error())
        )
        assertEquals(
          spans.map(_.underlying.getEvents.asScala.toList),
          List(List(expected(now.toNanos)))
        )
      }
    }
  }

  sdkTest("propagate trace info over stream scopes") { sdk =>
    import cats.syntax.functor.*

    def treeOf(
        spans: List[SpanDataWrapper[SpanData]]
    ): List[SpanTree[SpanInfo]] =
      SpanTree
        .of(spans)
        .map(_.map(s => SpanInfo(s.name, s.startTimestamp, s.endTimestamp)))

    def expected(now: FiniteDuration): SpanTree[SpanInfo] =
      /*
       expected:
       span
       |- span-2
       `- span
          `- span-3
       */
      SpanTree(
        SpanInfo("span", now, now),
        List(
          SpanTree(SpanInfo("span-2", now, now)),
          SpanTree(
            SpanInfo("span", now, now),
            List(SpanTree(SpanInfo("span-3", now, now)))
          )
        )
      )

    def flow(tracer: Tracer[IO]): Stream[IO, Unit] = {
      Stream.resource(tracer.span("span").resource).flatMap { res =>
        (for {
          res1 <- Stream.resource(tracer.span("span").resource)
          _ <- Stream
            .eval(
              tracer.currentSpanContext.assertEquals(Some(res1.span.context))
            )
            .translate(res1.trace)
          res2 <- Stream.resource(tracer.span("span-2").resource)
          _ <- Stream
            .eval(
              tracer.currentSpanContext.assertEquals(Some(res2.span.context))
            )
            .translate(res2.trace)
          res3 <- Stream.resource(
            tracer.spanBuilder("span-3").withParent(res1.span.context).build.resource
          )
          _ <- Stream
            .eval(
              tracer.currentSpanContext.assertEquals(Some(res3.span.context))
            )
            .translate(res3.trace)
        } yield ()).translate(res.trace)
      }

    }

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.currentSpanContext.assertEquals(None)
        _ <- flow(tracer).compile.drain
        _ <- tracer.currentSpanContext.assertEquals(None)
        spans <- sdk.finishedSpans
        tree <- IO.pure(treeOf(spans))
      } yield assertEquals(tree, List(expected(now)))
    }
  }

  sdkTest("stream propagation: fan-out worker pool") { sdk =>
    val itemCount = 8

    def flow(tracer: Tracer[IO]): IO[Unit] =
      tracer.span("fanout-root").use { root =>
        Stream
          .emits((0 until itemCount).toList)
          .covary[IO]
          .parEvalMapUnordered(4) { i =>
            tracer.span(s"fanout-child-$i").use { child =>
              tracer.currentSpanContext.assertEquals(Some(child.context))
            }
          }
          .compile
          .drain
          .as(root.context)
          .flatMap { rootCtx =>
            tracer.currentSpanContext.assertEquals(Some(rootCtx))
          }
      }

    def expected: SpanTree[String] =
      SpanTree(
        "fanout-root",
        (0 until itemCount).toList.map(i => SpanTree(s"fanout-child-$i"))
      )

    TestControl.executeEmbed {
      for {
        tracer <- sdk.provider.get("tracer")
        _ <- flow(tracer)
        _ <- tracer.currentSpanContext.assertEquals(None)
        spans <- sdk.finishedSpans
      } yield {
        assertEquals(nameForestOf(spans), sortForest(List(expected)))
      }
    }
  }

  sdkTest("stream propagation: dual branch merge keeps roots isolated") { sdk =>
    def branch(tracer: Tracer[IO], prefix: String): Stream[IO, Unit] =
      Stream.resource(tracer.rootSpan(s"$prefix-root").resource).flatMap { root =>
        Stream(
          Stream.eval(tracer.span(s"$prefix-child-1").surround(IO.sleep(2.millis))),
          Stream.eval(tracer.span(s"$prefix-child-2").surround(IO.sleep(1.millis)))
        ).parJoin(2).translate(root.trace)
      }

    def expected: List[SpanTree[String]] =
      List(
        SpanTree("a-root", List(SpanTree("a-child-1"), SpanTree("a-child-2"))),
        SpanTree("b-root", List(SpanTree("b-child-1"), SpanTree("b-child-2")))
      )

    TestControl.executeEmbed {
      for {
        tracer <- sdk.provider.get("tracer")
        _ <- Stream(branch(tracer, "a"), branch(tracer, "b")).parJoin(2).compile.drain
        spans <- sdk.finishedSpans
      } yield {
        val aRoot = spanByName(spans, "a-root")
        val bRoot = spanByName(spans, "b-root")
        assertNotEquals(aRoot.spanContext.traceIdHex, bRoot.spanContext.traceIdHex)
        assertEquals(nameForestOf(spans), expected)
      }
    }
  }

  sdkTest("stream propagation: cancellation race in parallel children") { sdk =>
    val itemCount = 6
    val canceledIndices = Set(0, 2, 4)

    def expected: SpanTree[String] =
      SpanTree(
        "cancel-root",
        (0 until itemCount).toList.map(i => SpanTree(s"cancel-child-$i"))
      )

    TestControl.executeEmbed {
      for {
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.span("cancel-root").surround {
          Stream
            .emits((0 until itemCount).toList)
            .covary[IO]
            .parEvalMapUnordered(itemCount) { i =>
              val child =
                tracer.span(s"cancel-child-$i").surround(IO.sleep(100.millis))

              if (canceledIndices.contains(i)) child.timeoutTo(10.millis, IO.unit)
              else child
            }
            .compile
            .drain
        }
        spans <- sdk.finishedSpans
      } yield {
        val canceled = canceledIndices.toList.map(i => spanByName(spans, s"cancel-child-$i"))
        val completed =
          (0 until itemCount).filterNot(canceledIndices).toList.map(i => spanByName(spans, s"cancel-child-$i"))

        assertEquals(nameForestOf(spans), sortForest(List(expected)))
        canceled.foreach { span =>
          assertEquals(span.underlying.getStatus, StatusData.create(StatusCode.ERROR, "canceled"))
        }
        completed.foreach { span =>
          assertEquals(span.underlying.getStatus, StatusData.unset())
        }
      }
    }
  }

  sdkTest("stream propagation: nested parallelism across two levels") { sdk =>
    val batchCount = 2
    val itemsPerBatch = 3
    val leavesPerItem = 2

    def expected: SpanTree[String] =
      SpanTree(
        "nested-root",
        (0 until batchCount).toList.map { batch =>
          SpanTree(
            s"nested-batch-$batch",
            (0 until itemsPerBatch).toList.map { item =>
              SpanTree(
                s"nested-item-$batch-$item",
                (0 until leavesPerItem).toList.map { leaf =>
                  SpanTree(s"nested-leaf-$batch-$item-$leaf")
                }
              )
            }
          )
        }
      )

    TestControl.executeEmbed {
      for {
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.span("nested-root").surround {
          Stream
            .emits((0 until batchCount).toList)
            .covary[IO]
            .parEvalMapUnordered(batchCount) { batch =>
              tracer.span(s"nested-batch-$batch").surround {
                Stream
                  .emits((0 until itemsPerBatch).toList)
                  .covary[IO]
                  .parEvalMapUnordered(itemsPerBatch) { item =>
                    tracer.span(s"nested-item-$batch-$item").surround {
                      (
                        tracer.span(s"nested-leaf-$batch-$item-0").surround(IO.unit),
                        tracer.span(s"nested-leaf-$batch-$item-1").surround(IO.unit)
                      ).parTupled.void
                    }
                  }
                  .compile
                  .drain
              }
            }
            .compile
            .drain
        }
        spans <- sdk.finishedSpans
      } yield {
        assertEquals(nameForestOf(spans), List(expected))
      }
    }
  }

  sdkTest("stream propagation: background stream does not leak context") { sdk =>
    val itemCount = 6

    def branch(tracer: Tracer[IO], rootName: String, childPrefix: String): Stream[IO, Unit] =
      Stream.resource(tracer.rootSpan(rootName).resource).flatMap { root =>
        Stream
          .emits((0 until itemCount).toList)
          .covary[IO]
          .parEvalMapUnordered(3) { i =>
            tracer.span(s"$childPrefix-$i").use { span =>
              tracer.currentSpanContext.assertEquals(Some(span.context))
            }
          }
          .translate(root.trace)
      }

    def expected: List[SpanTree[String]] =
      List(
        SpanTree(
          "background-root",
          (0 until itemCount).toList.map(i => SpanTree(s"background-child-$i"))
        ),
        SpanTree(
          "foreground-root",
          (0 until itemCount).toList.map(i => SpanTree(s"foreground-child-$i"))
        )
      )

    TestControl.executeEmbed {
      for {
        tracer <- sdk.provider.get("tracer")
        _ <- Stream(
          branch(tracer, "background-root", "background-child"),
          branch(tracer, "foreground-root", "foreground-child")
        ).parJoin(2).compile.drain
        spans <- sdk.finishedSpans
      } yield {
        val backgroundRoot = spanByName(spans, "background-root")
        val foregroundRoot = spanByName(spans, "foreground-root")
        assertNotEquals(backgroundRoot.spanContext.traceIdHex, foregroundRoot.spanContext.traceIdHex)
        assertEquals(nameForestOf(spans), expected)
      }
    }
  }

  sdkTest("stream propagation: translate inner stream with evalMap stages") { sdk =>
    /*
     expected:
     inner-root
     |- inner-stage-1
     |- inner-stage-1-stream-stage-1
     |- inner-stage-1-stream-stage-2
     |- inner-stage-1-stream-stage-3
     |- inner-stage-2
     |- inner-stage-2-stream-stage-1
     |- inner-stage-2-stream-stage-2
     |- inner-stage-2-stream-stage-3
     `- inner-stage-3
        + inner-stage-3-stream-stage-1
        + inner-stage-3-stream-stage-2
        `- inner-stage-3-stream-stage-3
     */
    val stages = List("inner-stage-1", "inner-stage-2", "inner-stage-3")

    def expected: SpanTree[String] =
      SpanTree(
        "inner-root",
        stages.flatMap { stage =>
          List(
            SpanTree(stage),
            SpanTree(s"$stage-stream-stage-1"),
            SpanTree(s"$stage-stream-stage-2"),
            SpanTree(s"$stage-stream-stage-3")
          )
        }
      )

    def flow(tracer: Tracer[IO]): Stream[IO, Unit] =
      Stream.resource(tracer.span("inner-root").resource).flatMap { root =>
        Stream(1, 2, 3)
          .evalMap(i => tracer.span(s"inner-stage-$i").use_.as(i))
          .flatMap { i =>
            Stream(1, 2, 3)
              .parEvalMapUnbounded(idx => tracer.span(s"inner-stage-$i-stream-stage-$idx").use_)
          }
          .translate(root.trace)
      }

    TestControl.executeEmbed {
      for {
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.currentSpanContext.assertEquals(None)
        _ <- flow(tracer).compile.drain
        _ <- tracer.currentSpanContext.assertEquals(None)
        spans <- sdk.finishedSpans
      } yield {
        assertEquals(nameForestOf(spans), sortForest(List(expected)))
      }
    }
  }

  sdkTest("stream propagation: combo queue handoff + worker pool + cancellation") { sdk =>
    val ticks = 3
    val workers = 2
    val canceledWorker = 1

    /*
     expected:
     tick-0-root
     |- tick-0-db
     `- tick-0-process
        |- tick-0-store
        |- tick-0-worker-0
        `- tick-0-worker-1 (canceled)
     ...
     tick-2-root
     |- tick-2-db
     `- tick-2-process
        |- tick-2-store
        |- tick-2-worker-0
        `- tick-2-worker-1 (canceled)
     */
    def expected: List[SpanTree[String]] =
      (0 until ticks).toList.map { tick =>
        SpanTree(
          s"tick-$tick-root",
          List(
            SpanTree(s"tick-$tick-db"),
            SpanTree(
              s"tick-$tick-process",
              List(
                SpanTree(s"tick-$tick-store"),
                SpanTree(s"tick-$tick-worker-0"),
                SpanTree(s"tick-$tick-worker-1")
              )
            )
          )
        )
      }

    def tickFlow(tracer: Tracer[IO], tick: Int): Stream[IO, Unit] =
      Stream.resource(tracer.span(s"tick-$tick-root").resource).flatMap { root =>
        Stream
          .eval(tracer.currentSpanContext.assertEquals(None))
          .flatMap(_ =>
            Stream.eval(tracer.currentSpanContext.assertEquals(Some(root.span.context))).translate(root.trace)
          )
          .flatMap { _ =>
            Stream
              .resource(tracer.span(s"tick-$tick-db").resource)
              .flatMap { db =>
                Stream
                  .eval(tracer.currentSpanContext.assertEquals(Some(db.span.context)))
                  .translate(db.trace)
                  .flatMap(_ => Stream.eval(IO.unit).translate(db.trace))
              }
              .translate(root.trace)
          }
          .flatMap { _ =>
            Stream
              .resource(tracer.span(s"tick-$tick-process").resource)
              .flatMap { process =>
                val workerFlow =
                  Stream
                    .emits((0 until workers).toList)
                    .covary[IO]
                    .parEvalMapUnordered(workers) { w =>
                      val work =
                        tracer.span(s"tick-$tick-worker-$w").surround {
                          IO.sleep(if (w == canceledWorker) 100.millis else 5.millis)
                        }

                      if (w == canceledWorker) work.timeoutTo(10.millis, IO.unit)
                      else work
                    }

                val storeFlow =
                  Stream
                    .resource(tracer.span(s"tick-$tick-store").resource)
                    .flatMap { store =>
                      Stream
                        .eval(tracer.currentSpanContext.assertEquals(Some(store.span.context)))
                        .translate(store.trace)
                    }

                Stream
                  .eval(tracer.currentSpanContext.assertEquals(Some(process.span.context)))
                  .translate(process.trace)
                  .flatMap(_ => workerFlow.translate(process.trace).drain ++ storeFlow.translate(process.trace))
              }
              .translate(root.trace)
          }
      }

    TestControl.executeEmbed {
      for {
        tracer <- sdk.provider.get("tracer")
        _ <- Stream
          .emits((0 until ticks).toList)
          .covary[IO]
          .flatMap(tick => Stream.eval(IO.sleep(1.second)).drain ++ tickFlow(tracer, tick))
          .compile
          .drain
        spans <- sdk.finishedSpans
      } yield {
        assertEquals(nameForestOf(spans), sortForest(expected))

        (0 until ticks).foreach { tick =>
          val canceled = spanByName(spans, s"tick-$tick-worker-$canceledWorker")
          assertEquals(canceled.underlying.getStatus, StatusData.create(StatusCode.ERROR, "canceled"))
        }
      }
    }
  }

  protected def makeSdk(
      configure: Builder => Builder,
      additionalPropagators: List[TextMapPropagator[Context]]
  ): Resource[IO, BaseTracerSuite.Sdk[SpanData]] =
    TracesTestkit
      .builder[IO]
      .addTracerProviderCustomizer(configure)
      .withTextMapPropagators(W3CTraceContextPropagator.getInstance() :: additionalPropagators.map(_.asJava))
      .build
      .map { traces =>
        new BaseTracerSuite.Sdk[SpanData] {
          def provider: TracerProvider[IO] =
            traces.tracerProvider

          def finishedSpans: IO[List[SpanDataWrapper[SpanData]]] =
            traces.finishedSpans[SpanData].map(_.map(toWrapper))
        }
      }

  private def toWrapper(sd: SpanData): SpanDataWrapper[SpanData] =
    new SpanDataWrapper[SpanData] {
      def underlying: SpanData = sd

      def name: String = sd.getName

      def spanContext: SpanContext =
        SpanContextConversions.toScala(sd.getSpanContext)

      def parentSpanContext: Option[SpanContext] =
        Option.when(sd.getParentSpanContext.isValid)(
          SpanContextConversions.toScala(sd.getParentSpanContext)
        )

      def attributes: Attributes =
        sd.getAttributes.toScala

      def startTimestamp: FiniteDuration =
        sd.getStartEpochNanos.nanos

      def endTimestamp: Option[FiniteDuration] =
        Option.when(sd.hasEnded)(sd.getEndEpochNanos.nanos)
    }

  private def spanByName(
      spans: List[SpanDataWrapper[SpanData]],
      name: String
  ): SpanDataWrapper[SpanData] = {
    val found = spans.filter(_.name == name)
    assertEquals(found.size, 1, s"expected exactly one span named $name")
    found.head
  }

  private def nameForestOf(
      spans: List[SpanDataWrapper[SpanData]]
  ): List[SpanTree[String]] =
    sortForest(SpanTree.of(spans).map(_.map(_.name)))

  private def sortForest(
      forest: List[SpanTree[String]]
  ): List[SpanTree[String]] =
    forest.map(sortTree).sortBy(_.current)

  private def sortTree(
      tree: SpanTree[String]
  ): SpanTree[String] =
    SpanTree(
      tree.current,
      tree.children.map(sortTree).sortBy(_.current)
    )
}
