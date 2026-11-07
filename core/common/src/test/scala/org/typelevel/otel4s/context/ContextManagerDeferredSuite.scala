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

package org.typelevel.otel4s.context

import cats.effect.Deferred
import cats.effect.IO
import cats.effect.IOLocal
import cats.effect.Resource
import cats.effect.kernel.Outcome
import cats.syntax.all._
import fs2.Stream
import munit.CatsEffectSuite
import org.typelevel.otel4s.context.vault.VaultContext

class ContextManagerDeferredSuite extends CatsEffectSuite {

  List("success", "error", "cancel").foreach { termination =>
    test(s"Resource.mapK - acquisition and release preserve context on $termination") {
      withManager { (manager, key) =>
        val captured = VaultContext.root.updated(key, "captured")
        val consumer = VaultContext.root.updated(key, "consumer")

        for {
          seen <- IO.ref(Vector.empty[(String, Option[String])])
          ready <- Deferred[IO, Unit]
          restore <- manager.scope(captured)(manager.captureScope)
          record = (stage: String) => manager.current.flatMap(ctx => seen.update(_ :+ (stage -> ctx.get(key))))
          resource = Resource.make(record("acquire"))(_ => record("release")).mapK(restore)
          _ <- run(termination, ready) {
            manager.scope(consumer) {
              resource.use(_ => record("use") *> terminate(termination, ready)).guarantee(record("after"))
            }
          }
          _ <- seen.get.assertEquals(
            Vector(
              "acquire" -> Some("captured"),
              "use" -> Some("consumer"),
              "release" -> Some("captured"),
              "after" -> Some("consumer")
            )
          )
        } yield ()
      }
    }

    test(s"Stream.translate - effects and finalizers preserve context on $termination") {
      withManager { (manager, key) =>
        val captured = VaultContext.root.updated(key, "captured")
        val consumer = VaultContext.root.updated(key, "consumer")

        for {
          seen <- IO.ref(Vector.empty[(String, Option[String])])
          ready <- Deferred[IO, Unit]
          restore <- manager.scope(captured)(manager.captureScope)
          record = (stage: String) => manager.current.flatMap(ctx => seen.update(_ :+ (stage -> ctx.get(key))))
          stream = Stream.eval(record("body") *> terminate(termination, ready)).onFinalize(record("finalize"))
          _ <- run(termination, ready) {
            manager.scope(consumer) {
              stream.translate(restore).compile.drain.guarantee(record("after"))
            }
          }
          _ <- seen.get.assertEquals(
            Vector(
              "body" -> Some("captured"),
              "finalize" -> Some("captured"),
              "after" -> Some("consumer")
            )
          )
        } yield ()
      }
    }
  }

  test("reuse a captured scope concurrently across resources and streams") {
    withManager { (manager, key) =>
      val captured = VaultContext.root.updated(key, "captured")

      for {
        restore <- manager.scope(captured)(manager.captureScope)
        entered <- IO.ref(0)
        bothEntered <- Deferred[IO, Unit]
        _ <- List("first", "second").parTraverse_ { name =>
          val consumer = VaultContext.root.updated(key, name)
          val checkCaptured = manager.current.map(_.get(key)).assertEquals(Some("captured"))
          val checkConsumer = manager.current.map(_.get(key)).assertEquals(Some(name))
          val resource = Resource.make(checkCaptured)(_ => checkCaptured).mapK(restore)
          val work =
            entered.updateAndGet(_ + 1).flatMap(n => bothEntered.complete(()).void.whenA(n == 2)) *>
              bothEntered.get *> checkCaptured

          manager.scope(consumer) {
            resource.use { _ =>
              checkConsumer *> Stream.eval(work).translate(restore).compile.drain *> checkConsumer
            } *> checkConsumer
          }
        }
        _ <- manager.current.map(_.get(key)).assertEquals(None)
      } yield ()
    }
  }

  private def terminate(termination: String, ready: Deferred[IO, Unit]): IO[Unit] =
    ready.complete(()).void *> (termination match {
      case "success" => IO.unit
      case "error"   => IO.raiseError(TestException)
      case _         => IO.never
    })

  private def run(termination: String, ready: Deferred[IO, Unit])(effect: IO[Unit]): IO[Unit] =
    for {
      fiber <- effect.attempt.start
      _ <- ready.get
      _ <- fiber.cancel.whenA(termination == "cancel")
      outcome <- fiber.join
      _ <- outcome match {
        case Outcome.Succeeded(result) if termination != "cancel" =>
          result.flatMap {
            case Right(()) if termination == "success" => IO.unit
            case Left(error) if termination == "error" => IO(assertEquals(error, TestException))
            case other                                 => IO(fail(s"Unexpected result for $termination: $other"))
          }
        case Outcome.Canceled() if termination == "cancel" => IO.unit
        case other                                         => IO(fail(s"Unexpected outcome for $termination: $other"))
      }
    } yield ()

  private def withManager(
      f: (ContextManager.Aux[IO, VaultContext], VaultContext.Key[String]) => IO[Unit]
  ): IO[Unit] =
    for {
      key <- VaultContext.Key.unique[IO, String]("custom")
      local <- IOLocal(VaultContext.root)
      _ <- f(ContextManager.fromLocal(local.asLocal), key)
    } yield ()

  private case object TestException extends RuntimeException
}
