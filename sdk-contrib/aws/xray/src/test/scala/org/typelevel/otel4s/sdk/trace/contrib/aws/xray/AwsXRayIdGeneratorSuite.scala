/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.sdk.trace.contrib.aws.xray

import cats.MonadThrow
import cats.effect._
import cats.effect.std._
import cats.effect.testkit.TestControl
import cats.syntax.all._
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck._
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.trace.SpanContext._
import scodec.bits.ByteVector

import scala.concurrent.duration._

class AwsXRayIdGeneratorSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  /** Generate an arbitrary list of delays representing the times between pairs of calls to
    * {{{AwsXRayIdGenerator[IO].generateTraceId}}}
    */
  private val genTimeline: Gen[List[FiniteDuration]] =
    Gen
      .posNum[Int]
      .flatMap(Gen.listOfN(_, Gen.choose(0.seconds, 1.second)))

  test("Trace IDs should be a combination of the current timestamp with random numbers") {
    PropF.forAllNoShrinkF(Gen.long, genTimeline) { (seed: Long, timeline: List[FiniteDuration]) =>
      IntermittentlyZeroRandom[IO](seed).flatMap { controlRandom =>
        IntermittentlyZeroRandom[IO](seed).flatMap { implicit testRandom =>
          val expecteds: IO[List[ByteVector]] =
            timeline
              .scanLeft(0.millis)(_ + _) // convert delays to absolute timestamps
              .traverse { now =>
                for {
                  hiRandom <- controlRandom.nextInt.map(_ & 0xffffffffL)
                  lowRandom <- controlRandom.nextLong
                } yield TraceId.fromLongs(now.toSeconds << 32 | hiRandom, lowRandom)
              }

          TestControl
            .execute {
              for {
                expecteds <- expecteds
                output <- timeline.traverse(AwsXRayIdGenerator[IO].generateTraceId.delayBy(_))
              } yield {
                assertEquals(output, expecteds)
                assert(output.forall(TraceId.isValid))
              }
            }
            .flatMap(_.tickAll)
        }
      }
    }
  }

  test("Span IDs should be randomly generated but not equal 0") {
    PropF.forAllNoShrinkF(Gen.long, Gen.posNum[Int]) { (seed: Long, count: Int) =>
      IntermittentlyZeroRandom[IO](seed).flatMap { controlRandom =>
        IntermittentlyZeroRandom[IO](seed).flatMap { implicit testRandom =>
          for {
            expected <- controlRandom.nextLong.iterateUntil(_ != 0L).map(SpanId.fromLong).replicateA(count)
            output <- AwsXRayIdGenerator[IO].generateSpanId.replicateA(count)
          } yield {
            assertEquals(output, expected)
            assert(output.forall(SpanId.isValid))
          }
        }
      }
    }
  }

  test("canSkipIdValidation is true") {
    PropF.forAllNoShrinkF(Gen.long) {
      Random.scalaUtilRandomSeedLong[IO](_).flatMap { implicit testRandom =>
        IO {
          assert(AwsXRayIdGenerator[IO].canSkipIdValidation)
        }
      }
    }
  }

}

object IntermittentlyZeroRandom {
  def apply[F[_]: Sync](seed: Long): F[Random[F]] =
    Random.scalaUtilRandomSeedLong[F](seed).map(new IntermittentlyZeroRandom[F](_))
}

class IntermittentlyZeroRandom[F[_]: MonadThrow](actualRandom: Random[F]) extends UnimplementedRandom[F] {
  override def nextDouble: F[Double] = actualRandom.nextBoolean.ifM(0d.pure[F], actualRandom.nextDouble)
  override def nextFloat: F[Float] = actualRandom.nextBoolean.ifM(0f.pure[F], actualRandom.nextFloat)
  override def nextGaussian: F[Double] = actualRandom.nextBoolean.ifM(0d.pure[F], actualRandom.nextGaussian)
  override def nextInt: F[Int] = actualRandom.nextBoolean.ifM(0.pure[F], actualRandom.nextInt)
  override def nextIntBounded(n: Int): F[Int] = actualRandom.nextBoolean.ifM(0.pure[F], actualRandom.nextIntBounded(n))
  override def nextLong: F[Long] = actualRandom.nextBoolean.ifM(0L.pure[F], actualRandom.nextLong)
  override def nextLongBounded(n: Long): F[Long] =
    actualRandom.nextBoolean.ifM(0L.pure[F], actualRandom.nextLongBounded(n))
}

class UnimplementedRandom[F[_]: MonadThrow] extends Random[F] {
  private def notImplemented[A]: F[A] = (new NotImplementedError).raiseError[F, A]

  override def betweenDouble(minInclusive: Double, maxExclusive: Double): F[Double] = notImplemented
  override def betweenFloat(minInclusive: Float, maxExclusive: Float): F[Float] = notImplemented
  override def betweenInt(minInclusive: Int, maxExclusive: Int): F[Int] = notImplemented
  override def betweenLong(minInclusive: Long, maxExclusive: Long): F[Long] = notImplemented
  override def nextAlphaNumeric: F[Char] = notImplemented
  override def nextBoolean: F[Boolean] = notImplemented
  override def nextBytes(n: Int): F[Array[Byte]] = notImplemented
  override def nextDouble: F[Double] = notImplemented
  override def nextFloat: F[Float] = notImplemented
  override def nextGaussian: F[Double] = notImplemented
  override def nextInt: F[Int] = notImplemented
  override def nextIntBounded(n: Int): F[Int] = notImplemented
  override def nextLong: F[Long] = notImplemented
  override def nextLongBounded(n: Long): F[Long] = notImplemented
  override def nextPrintableChar: F[Char] = notImplemented
  override def nextString(length: Int): F[String] = notImplemented
  override def shuffleList[A](l: List[A]): F[List[A]] = notImplemented
  override def shuffleVector[A](v: Vector[A]): F[Vector[A]] = notImplemented
}
