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

package org.typelevel.otel4s.oteljava

import cats.effect.IO
import cats.effect.SyncIO
import io.opentelemetry.context.{Context => JContext}
import io.opentelemetry.context.ContextStorage
import munit.CatsEffectSuite
import munit.Location
import org.typelevel.otel4s.context.Key
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.context.LocalContext

import scala.util.Using

class IOLocalContextStorageSuite extends CatsEffectSuite {
  import IOLocalContextStorageSuite._

  private val localProvider: IO[LocalContext[IO]] =
    IOLocalContextStorage.localProvider[IO].local

  private def sCurrent[F[_]](implicit L: LocalContext[F]): F[Context] =
    L.ask[Context]
  private def jCurrent: JContext = JContext.current()

  private def usingModifiedCtx[A](f: JContext => JContext)(body: => A): A =
    Using.resource(f(jCurrent).makeCurrent())(_ => body)

  private def localTest(
      name: String
  )(body: LocalContext[IO] => IO[Any])(implicit loc: Location): Unit =
    test(name) {
      for {
        local <- localProvider
        _ <- body(local)
      } yield ()
    }

  // if this fails, the rest will almost certainly fail,
  // and will be meaningless regardless
  localTest("correctly configured") { implicit L =>
    for {
      sCtx <- sCurrent
      jCtx <- IO(jCurrent)
    } yield {
      // correct ContextStorage is configured
      assertEquals(
        ContextStorage.get().getClass: Any,
        classOf[IOLocalContextStorage]: Any
      )

      // current is root
      assertEquals(JContext.root(), Context.root.underlying)
      assertEquals(jCtx, sCtx.underlying)
      assertEquals(sCtx, Context.root)
      assertEquals(jCtx, JContext.root())

      // root is empty
      assertEquals(sCtx.get(key1), None)
      assertEquals(sCtx.get(key2), None)
      assertEquals(Option(jCtx.get(key1)), None)
      assertEquals(Option(jCtx.get(key2)), None)
    }
  }

  // see https://discord.com/channels/632277896739946517/839263556754472990/1317163027451088926
  test("works as a Java-only ContextStorage".ignore) {
    usingModifiedCtx(_.`with`(key1, "1")) {
      assertEquals(Option(jCurrent.get(key1)), Some("1"))
      assertEquals(Option(jCurrent.get(key2)), None)

      usingModifiedCtx(_.`with`(key2, 2)) {
        assertEquals(Option(jCurrent.get(key1)), Some("1"))
        assertEquals(Option(jCurrent.get(key2)), Some(2))

        usingModifiedCtx(_ => JContext.root()) {
          assertEquals(Option(jCurrent.get(key1)), None)
          assertEquals(Option(jCurrent.get(key2)), None)
        }
      }
    }
  }

  localTest("works as a Scala-only Local") { implicit L =>
    doLocally(_.updated(key1, "1")) {
      for {
        _ <- doLocally(_.updated(key2, 2)) {
          for {
            _ <- doScoped(Context.root) {
              for (ctx <- sCurrent)
                yield {
                  assertEquals(ctx.get(key1), None)
                  assertEquals(ctx.get(key2), None)
                }
            }
            ctx <- sCurrent
          } yield {
            assertEquals(ctx.get(key1), Some("1"))
            assertEquals(ctx.get(key2), Some(2))
          }
        }
        ctx <- sCurrent
      } yield {
        assertEquals(ctx.get(key1), Some("1"))
        assertEquals(ctx.get(key2), None)
      }
    }
  }

  localTest("Scala with Java nested inside it") { implicit L =>
    doLocally(_.updated(key1, "1")) {
      for {
        _ <- IO {
          usingModifiedCtx(_.`with`(key2, 2)) {
            val sCtx = sCurrent.unsafeRunSync()
            val jCtx = jCurrent
            assertEquals(jCtx, sCtx.underlying)
            assertEquals(sCtx.get(key1), Some("1"))
            assertEquals(sCtx.get(key2), Some(2))
            assertEquals(Option(jCtx.get(key1)), Some("1"))
            assertEquals(Option(jCtx.get(key2)), Some(2))
          }
        }
        sCtx <- sCurrent
        jCtx <- IO(jCurrent)
      } yield {
        assertEquals(jCtx, sCtx.underlying)
        assertEquals(sCtx.get(key1), Some("1"))
        assertEquals(sCtx.get(key2), None)
        assertEquals(Option(jCtx.get(key1)), Some("1"))
        assertEquals(Option(jCtx.get(key2)), None)
      }
    }
  }

  localTest("Java with Scala nested inside it") { implicit L =>
    IO {
      usingModifiedCtx(_.`with`(key1, "1")) {
        val sCtx = locally {
          for {
            _ <- doLocally(_.updated(key2, 2)) {
              for {
                sCtx <- sCurrent
                jCtx <- IO(jCurrent)
              } yield {
                assertEquals(jCtx, sCtx.underlying)
                assertEquals(sCtx.get(key1), Some("1"))
                assertEquals(sCtx.get(key2), Some(2))
                assertEquals(Option(jCtx.get(key1)), Some("1"))
                assertEquals(Option(jCtx.get(key2)), Some(2))
              }
            }
            ctx <- sCurrent
          } yield ctx
        }.unsafeRunSync()
        val jCtx = jCurrent
        assertEquals(jCtx, sCtx.underlying)
        assertEquals(sCtx.get(key1), Some("1"))
        assertEquals(sCtx.get(key2), None)
        assertEquals(Option(jCtx.get(key1)), Some("1"))
        assertEquals(Option(jCtx.get(key2)), None)
      }
    }
  }

  localTest("lots of nesting") { implicit L =>
    doLocally(_.updated(key1, "1")) {
      for {
        _ <- IO {
          usingModifiedCtx(_.`with`(key2, 2)) {
            usingModifiedCtx(_.`with`(key1, "3")) {
              val sCtx = locally {
                for {
                  _ <- doLocally(_.updated(key2, 4)) {
                    for {
                      sCtx <- sCurrent
                      jCtx <- IO(jCurrent)
                    } yield {
                      assertEquals(jCtx, sCtx.underlying)
                      assertEquals(sCtx.get(key1), Some("3"))
                      assertEquals(sCtx.get(key2), Some(4))
                      assertEquals(Option(jCtx.get(key1)), Some("3"))
                      assertEquals(Option(jCtx.get(key2)), Some(4))
                    }
                  }
                  ctx <- sCurrent
                } yield ctx
              }.unsafeRunSync()
              val jCtx = jCurrent
              assertEquals(jCtx, sCtx.underlying)
              assertEquals(sCtx.get(key1), Some("3"))
              assertEquals(sCtx.get(key2), Some(2))
              assertEquals(Option(jCtx.get(key1)), Some("3"))
              assertEquals(Option(jCtx.get(key2)), Some(2))
            }
            val sCtx = locally {
              for {
                _ <- doScoped(Context.root) {
                  for {
                    sCtx <- sCurrent
                    jCtx <- IO(jCurrent)
                  } yield {
                    assertEquals(jCtx, sCtx.underlying)
                    assertEquals(sCtx.get(key1), None)
                    assertEquals(sCtx.get(key2), None)
                    assertEquals(Option(jCtx.get(key1)), None)
                    assertEquals(Option(jCtx.get(key2)), None)
                  }
                }
                ctx <- sCurrent
              } yield ctx
            }.unsafeRunSync()
            val jCtx = jCurrent
            assertEquals(jCtx, sCtx.underlying)
            assertEquals(sCtx.get(key1), Some("1"))
            assertEquals(sCtx.get(key2), Some(2))
            assertEquals(Option(jCtx.get(key1)), Some("1"))
            assertEquals(Option(jCtx.get(key2)), Some(2))
          }
        }
        sCtx <- sCurrent
        jCtx <- IO(jCurrent)
      } yield {
        assertEquals(jCtx, sCtx.underlying)
        assertEquals(sCtx.get(key1), Some("1"))
        assertEquals(sCtx.get(key2), None)
        assertEquals(Option(jCtx.get(key1)), Some("1"))
        assertEquals(Option(jCtx.get(key2)), None)
      }
    }
  }
}

object IOLocalContextStorageSuite {
  private val keyProvider = Key.Provider[SyncIO, Context.Key]
  val key1: Context.Key[String] =
    keyProvider.uniqueKey[String]("key1").unsafeRunSync()
  val key2: Context.Key[Int] =
    keyProvider.uniqueKey[Int]("key2").unsafeRunSync()

  // `Local`'s methods have their argument lists in the an annoying order
  def doLocally[F[_], A](f: Context => Context)(fa: F[A])(implicit
      L: LocalContext[F]
  ): F[A] =
    L.local(fa)(f)
  def doScoped[F[_], A](e: Context)(fa: F[A])(implicit
      L: LocalContext[F]
  ): F[A] =
    L.scope(fa)(e)
}
