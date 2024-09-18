package org.typelevel.otel4s.sdk.contrib.aws

import cats.effect.*
import cats.effect.std.*
import cats.effect.testkit.TestControl
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Gen
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.trace.SpanContext.{SpanId, TraceId}

import scala.concurrent.duration.FiniteDuration

class AwsXRayIdGeneratorSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  // TODO generate ids over time
  test("Trace IDs should be a combination of the current timestamp with random numbers") {
    PropF.forAllNoShrinkF { (seed: Long,
                             now: FiniteDuration) =>
      Random.scalaUtilRandomSeedLong[IO](seed).flatMap { controlRandom =>
        Random.scalaUtilRandomSeedLong[IO](seed).flatMap { implicit testRandom =>
          val expected = for {
            hiRandom <- controlRandom.nextInt.map(_ & 0xFFFFFFFFL)
            lowRandom <- controlRandom.nextLong
          } yield TraceId.fromLongs(now.toSeconds << 32 | hiRandom, lowRandom)

          TestControl
            .execute {
              for {
                _ <- IO.sleep(now) // advance time until the arbitrary time given to us by ScalaCheck
                expected <- expected
                output <- AwsXRayIdGenerator[IO].generateTraceId
              } yield {
                assertEquals(output, expected)
              }
            }
            .flatMap(_.tickFor(now))
        }
      }
    }
  }

  test("Span IDs should be randomly generated") {
    PropF.forAllNoShrinkF(Gen.long, Gen.posNum[Int]) { (seed: Long, count: Int) =>
      Random.scalaUtilRandomSeedLong[IO](seed).flatMap { controlRandom =>
        Random.scalaUtilRandomSeedLong[IO](seed).flatMap { implicit testRandom =>
          for {
            expected <- controlRandom.nextLong.map(SpanId.fromLong).replicateA(count)
            output <- AwsXRayIdGenerator[IO].generateSpanId.replicateA(count)
          } yield {
            assertEquals(output, expected)
          }
        }
      }
    }
  }

}
