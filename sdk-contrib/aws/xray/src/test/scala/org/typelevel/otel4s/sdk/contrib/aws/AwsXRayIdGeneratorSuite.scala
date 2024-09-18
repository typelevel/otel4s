package org.typelevel.otel4s.sdk.contrib.aws

import cats.syntax.all.*
import cats.effect.*
import cats.effect.std.*
import cats.effect.testkit.TestControl
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.*
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.trace.SpanContext.{SpanId, TraceId}
import scodec.bits.ByteVector

import scala.concurrent.duration.*

class AwsXRayIdGeneratorSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  /**
   * Generate an arbitrary list of delays representing the times between pairs of
   * calls to {{{AwsXRayIdGenerator[IO].generateTraceId}}}
   */
  private val genTimeline: Gen[List[FiniteDuration]] =
    Gen.posNum[Int]
      .flatMap(Gen.listOfN(_, Gen.choose(0.seconds, 1.second)))

  test("Trace IDs should be a combination of the current timestamp with random numbers") {
    PropF.forAllNoShrinkF(Gen.long, genTimeline) { (seed: Long,
                                                    timeline: List[FiniteDuration]) =>
      Random.scalaUtilRandomSeedLong[IO](seed).flatMap { controlRandom =>
        Random.scalaUtilRandomSeedLong[IO](seed).flatMap { implicit testRandom =>
          val expecteds: IO[List[ByteVector]] =
            timeline
              .scanLeft(0.millis)(_ + _) // convert delays to absolute timestamps
              .traverse { now =>
                for {
                  hiRandom <- controlRandom.nextInt.map(_ & 0xFFFFFFFFL)
                  lowRandom <- controlRandom.nextLong.iterateUntil(_ != 0L)
                } yield TraceId.fromLongs(now.toSeconds << 32 | hiRandom, lowRandom)
              }

          TestControl
            .execute {
              for {
                expecteds <- expecteds
                output <- timeline.traverse(AwsXRayIdGenerator[IO].generateTraceId.delayBy(_))
              } yield {
                assertEquals(output, expecteds)
              }
            }
            .flatMap(_.tickAll)
        }
      }
    }
  }

  test("Span IDs should be randomly generated but not equal 0") {
    PropF.forAllNoShrinkF(Gen.long, Gen.posNum[Int]) { (seed: Long, count: Int) =>
      Random.scalaUtilRandomSeedLong[IO](seed).flatMap { controlRandom =>
        Random.scalaUtilRandomSeedLong[IO](seed).flatMap { implicit testRandom =>
          for {
            expected <- controlRandom.nextLong.iterateUntil(_ != 0L).map(SpanId.fromLong).replicateA(count)
            output <- AwsXRayIdGenerator[IO].generateSpanId.replicateA(count)
          } yield {
            assertEquals(output, expected)
          }
        }
      }
    }
  }

}
