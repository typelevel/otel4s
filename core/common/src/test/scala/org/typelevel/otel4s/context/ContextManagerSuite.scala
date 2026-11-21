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

import cats.data.Kleisli
import cats.effect.IO
import cats.effect.IOLocal
import cats.effect.kernel.Outcome
import cats.mtl.Local
import munit.CatsEffectSuite
import org.typelevel.otel4s.context.vault.VaultContext

class ContextManagerSuite extends CatsEffectSuite {

  test("current - return the context associated with the current scope") {
    for {
      key <- VaultContext.Key.unique[IO, String]("key")
      _ <- withManager { manager =>
        val context = VaultContext.root.updated(key, "value")

        for {
          root <- manager.current
          scoped <- manager.scope(context)(manager.current)
          restored <- manager.current
        } yield {
          assertEquals(root.get(key), None)
          assertEquals(scoped.get(key), Some("value"))
          assertEquals(restored.get(key), None)
        }
      }
    } yield ()
  }

  test("scope - replace the complete context") {
    for {
      outerKey <- VaultContext.Key.unique[IO, String]("outer")
      innerKey <- VaultContext.Key.unique[IO, String]("inner")
      _ <- withManager { manager =>
        val outer = VaultContext.root.updated(outerKey, "outer-value")
        val inner = VaultContext.root.updated(innerKey, "inner-value")

        manager.scope(outer) {
          manager.scope(inner)(manager.current).map { current =>
            assertEquals(current.get(outerKey), None)
            assertEquals(current.get(innerKey), Some("inner-value"))
          }
        }
      }
    } yield ()
  }

  test("scope - restore the previous context after an error") {
    for {
      key <- VaultContext.Key.unique[IO, String]("key")
      _ <- withManager { manager =>
        val context = VaultContext.root.updated(key, "value")

        manager.scope(context)(IO.raiseError[Unit](TestException)).attempt *> manager.current.map { restored =>
          assertEquals(restored.get(key), None)
        }
      }
    } yield ()
  }

  test("captureScope - capture when evaluated and restore when applied") {
    for {
      key <- VaultContext.Key.unique[IO, String]("key")
      _ <- withManager { manager =>
        val captured = VaultContext.root.updated(key, "captured")
        val ambient = VaultContext.root.updated(key, "ambient")

        for {
          capturedScope <- manager.scope(captured)(manager.captureScope)
          result <- manager.scope(ambient) {
            for {
              before <- manager.current
              inside <- capturedScope(manager.current)
              after <- manager.current
            } yield (before, inside, after)
          }
        } yield {
          assertEquals(result._1.get(key), Some("ambient"))
          assertEquals(result._2.get(key), Some("captured"))
          assertEquals(result._3.get(key), Some("ambient"))
        }
      }
    } yield ()
  }

  test("captureScope - evaluate the same capture effect in different contexts") {
    for {
      key <- VaultContext.Key.unique[IO, String]("key")
      _ <- withManager { manager =>
        val capture = manager.captureScope
        val first = VaultContext.root.updated(key, "first")
        val second = VaultContext.root.updated(key, "second")

        for {
          firstScope <- manager.scope(first)(capture)
          secondScope <- manager.scope(second)(capture)
          _ <- firstScope(manager.current).map(_.get(key)).assertEquals(Some("first"))
          _ <- secondScope(manager.current).map(_.get(key)).assertEquals(Some("second"))
        } yield ()
      }
    } yield ()
  }

  test("captureScope - restore the consumer context after failure in a detached fiber") {
    for {
      key <- VaultContext.Key.unique[IO, String]("key")
      _ <- withManager { manager =>
        val captured = VaultContext.root.updated(key, "captured")
        val consumer = VaultContext.root.updated(key, "consumer")

        for {
          restore <- manager.scope(captured)(manager.captureScope)
          fiber <- manager.scope(consumer) {
            (for {
              _ <- restore(manager.current).map(_.get(key)).assertEquals(Some("captured"))
              result <- restore(IO.raiseError[Unit](TestException)).attempt
              _ <- IO(assertEquals(result, Left(TestException)))
              _ <- manager.current.map(_.get(key)).assertEquals(Some("consumer"))
            } yield ()).start
          }
          _ <- fiber.joinWithNever
          _ <- manager.current.map(_.get(key)).assertEquals(None)
        } yield ()
      }
    } yield ()
  }

  test("captureScope - restore the consumer context before cancellation finalizers") {
    for {
      key <- VaultContext.Key.unique[IO, String]("key")
      observed <- IO.ref(Option.empty[String])
      _ <- withManager { manager =>
        val captured = VaultContext.root.updated(key, "captured")
        val consumer = VaultContext.root.updated(key, "consumer")

        for {
          restore <- manager.scope(captured)(manager.captureScope)
          fiber <- manager.scope(consumer) {
            IO.uncancelable { poll =>
              poll(restore(IO.canceled)).onCancel(manager.current.flatMap(ctx => observed.set(ctx.get(key))))
            }.start
          }
          outcome <- fiber.join
          _ <- IO(assertEquals(outcome, Outcome.Canceled[IO, Throwable, Unit]()))
          _ <- observed.get.assertEquals(Some("consumer"))
        } yield ()
      }
    } yield ()
  }

  test("captureScope - return a reusable scope") {
    for {
      key <- VaultContext.Key.unique[IO, String]("key")
      _ <- withManager { manager =>
        val context = VaultContext.root.updated(key, "value")

        for {
          capturedScope <- manager.scope(context)(manager.captureScope)
          first <- capturedScope(manager.current)
          second <- capturedScope(manager.current)
        } yield {
          assertEquals(first.get(key), Some("value"))
          assertEquals(second.get(key), Some("value"))
        }
      }
    } yield ()
  }

  test("liftTo - preserve the context type and scope behavior") {
    type G[A] = Kleisli[IO, String, A]

    for {
      key <- VaultContext.Key.unique[IO, String]("key")
      _ <- withManager { manager =>
        val lifted: ContextManager.Aux[G, VaultContext] = manager.liftTo[G]
        val captured = VaultContext.root.updated(key, "captured")
        val ambient = VaultContext.root.updated(key, "ambient")

        val program =
          lifted.scope(captured) {
            lifted.captureScope.flatMap { capturedScope =>
              lifted.scope(ambient)(capturedScope(lifted.current))
            }
          }

        program.run("environment").map { result =>
          assertEquals(result.get(key), Some("captured"))
        }
      }
    } yield ()
  }

  test("managers share context only when their providers return the same local") {
    val allocating = LocalProvider.fromLiftIO[IO, VaultContext]

    for {
      key <- VaultContext.Key.unique[IO, String]("shared")
      first <- allocating.local
      second <- allocating.local
      firstManager = ContextManager.fromLocal(first)
      independentManager = ContextManager.fromLocal(second)
      context = VaultContext.root.updated(key, "value")
      _ <- firstManager.scope(context)(independentManager.current).map(_.get(key)).assertEquals(None)
      shared = LocalProvider.fromLocal(first)
      sharedLocal <- shared.local
      sharedManager = ContextManager.fromLocal(sharedLocal)
      _ <- firstManager.scope(context)(sharedManager.current).map(_.get(key)).assertEquals(Some("value"))
      _ <- sharedManager.current.map(_.get(key)).assertEquals(None)
    } yield ()
  }

  private def withManager[A](
      f: ContextManager.Aux[IO, VaultContext] => IO[A]
  ): IO[A] =
    IOLocal(VaultContext.root).flatMap { ioLocal =>
      implicit val local: Local[IO, VaultContext] = ioLocal.asLocal

      f(ContextManager.fromLocal[IO, VaultContext])
    }

  private case object TestException extends RuntimeException
}
