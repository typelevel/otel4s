package org.typelevel.otel4s.sdk.trace

import cats.effect.IO
import cats.effect.IOLocal
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Arbitrary
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.trace.SpanContext

class SdkTraceScopeSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  private implicit val spanContextArbitrary: Arbitrary[SpanContext] =
    Arbitrary(Gens.spanContext)

  test("current - return None when there is no SpanContext") {
    for {
      scope <- createTraceScope
      current <- scope.current
    } yield assertEquals(current, None)
  }

  test("rootScope - return None when there is no SpanContext") {
    for {
      scope <- createTraceScope
      rootScope <- scope.rootScope
      current <- rootScope(scope.current)
    } yield assertEquals(current, None)
  }

  test("rootScope - keep the current context when there is no SpanContext") {
    val value = "value"

    for {
      scope <- createTraceScope
      _ <- assertIO(scope.current, None)

      key <- Context.Key.unique[IO, String]("some-key")
      ctxWithKey <- scope.reader(_.updated(key, value))

      // the context wasn't modified yet, so it should be empty
      _ <- assertIO(scope.reader(_.get(key)), None)

      // here we use an explicit context with a custom key
      _ <- scope.withExplicitContext(ctxWithKey) {
        for {
          _ <- assertIO(scope.reader(_.get(key)), Some(value))
          rootScope <- scope.rootScope
          _ <- assertIO(rootScope(scope.current), None)
          _ <- assertIO(rootScope(scope.reader(_.get(key))), Some(value))
        } yield ()
      }

      rootScope <- scope.rootScope
      _ <- assertIO(rootScope(scope.current), None)
    } yield ()
  }

  test(
    "rootScope - keep the current context when there is an invalid SpanContext"
  ) {
    val invalid = SpanContext.invalid
    val value = "value"

    for {
      scope <- createTraceScope
      _ <- assertIO(scope.current, None)

      key <- Context.Key.unique[IO, String]("some-key")
      ctxWithKey <- scope.reader(_.updated(key, value))

      // the context wasn't modified yet, so it should be empty
      _ <- assertIO(scope.reader(_.get(key)), None)

      // here we use an explicit context with a custom key
      _ <- scope.withExplicitContext(ctxWithKey) {
        scope.noopScope {
          for {
            _ <- assertIO(scope.reader(_.get(key)), Some(value))
            _ <- assertIO(scope.current, Some(invalid))
            rootScope <- scope.rootScope
            _ <- assertIO(rootScope(scope.current), Some(invalid))
            _ <- assertIO(rootScope(scope.reader(_.get(key))), Some(value))
          } yield ()
        }
      }

      // the context must be reset
      _ <- assertIO(scope.current, None)
    } yield ()
  }

  test(
    "rootScope - use Context.root when there is a valid SpanContext"
  ) {
    PropF.forAllF { (ctx: SpanContext) =>
      val value = "value"

      for {
        scope <- createTraceScope
        _ <- assertIO(scope.current, None)

        key <- Context.Key.unique[IO, String]("some-key")
        ctxWithKey <- scope.reader(_.updated(key, value))

        // the context wasn't modified yet, so it should be empty
        _ <- assertIO(scope.reader(_.get(key)), None)

        _ <- scope.withExplicitContext(ctxWithKey) {
          for {
            _ <- assertIO(scope.reader(_.get(key)), Some(value))
            scope1 <- scope.makeScope(ctx)
            _ <- scope1 {
              for {
                // the key must be present within the custom scope
                _ <- assertIO(scope.reader(_.get(key)), Some(value))
                _ <- assertIO(scope.current, Some(ctx))

                // the key must be present within the root scope, because we use empty span context
                rootScope <- scope.rootScope
                _ <- assertIO(rootScope(scope.current), None)
                _ <- assertIO(rootScope(scope.reader(_.get(key))), None)
              } yield ()
            }
          } yield ()
        }

        // the context must be reset
        _ <- assertIO(scope.current, None)
      } yield ()
    }
  }

  test("makeScope - use the given context when there is no SpanContext") {
    PropF.forAllF { (ctx: SpanContext) =>
      for {
        scope <- createTraceScope

        _ <- assertIO(scope.current, None)

        lift <- scope.makeScope(ctx)
        _ <- lift(assertIO(scope.current, Some(ctx)))

        // the context must be reset
        _ <- assertIO(scope.current, None)
      } yield ()
    }
  }

  test("makeScope - use the given context when there is a valid SpanContext") {
    PropF.forAllF { (ctx1: SpanContext, ctx2: SpanContext) =>
      for {
        scope <- createTraceScope

        _ <- assertIO(scope.current, None)

        scope1 <- scope.makeScope(ctx1)
        _ <- scope1 {
          for {
            _ <- assertIO(scope.current, Some(ctx1))
            scope2 <- scope.makeScope(ctx2)
            _ <- scope2(assertIO(scope.current, Some(ctx2)))
            _ <- assertIO(scope.current, Some(ctx1))
          } yield ()
        }

        // the context must be reset
        _ <- assertIO(scope.current, None)
      } yield ()
    }
  }

  test("makeScope - use invalid context when there is an valid SpanContext") {
    val invalid = SpanContext.invalid

    PropF.forAllF { (valid: SpanContext) =>
      for {
        scope <- createTraceScope

        _ <- assertIO(scope.current, None)

        scope1 <- scope.makeScope(invalid)
        _ <- scope1 {
          for {
            _ <- assertIO(scope.current, Some(invalid))
            scope2 <- scope.makeScope(valid)
            _ <- scope2(assertIO(scope.current, Some(invalid)))
            _ <- assertIO(scope.current, Some(invalid))
          } yield ()
        }

        // the context must be reset
        _ <- assertIO(scope.current, None)
      } yield ()
    }
  }

  test("noopScope - always use invalid context") {
    val invalid = SpanContext.invalid

    PropF.forAllF { (ctx: SpanContext) =>
      for {
        scope <- createTraceScope

        _ <- assertIO(scope.current, None)

        // the context is empty
        _ <- assertIO(scope.noopScope(scope.current), Some(invalid))

        scope1 <- scope.makeScope(ctx)

        // the context has a valid span
        _ <- scope1 {
          for {
            _ <- assertIO(scope.current, Some(ctx))
            _ <- assertIO(scope.noopScope(scope.current), Some(invalid))
            _ <- assertIO(scope.current, Some(ctx))
          } yield ()
        }

        // the context must be reset
        _ <- assertIO(scope.current, None)
      } yield ()
    }
  }

  private def createTraceScope: IO[SdkTraceScope[IO]] =
    IOLocal(Context.root).map { implicit ioLocal =>
      import org.typelevel.otel4s.sdk.instances._
      SdkTraceScope.fromLocal[IO]
    }

}
