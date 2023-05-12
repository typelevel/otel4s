package org.typelevel.otel4s.java

import cats.effect.IO
import cats.effect.IOLocal
import cats.effect.Resource
import cats.effect.std.Dispatcher
import io.opentelemetry.context.Context
import io.opentelemetry.context.ContextKey
import io.opentelemetry.context.ContextStorage
import java.util.logging._

object Run extends cats.effect.IOApp.Simple {

  val key = ContextKey.named[String]("test")

  def run =
    Dispatcher.parallel[IO].use { dispatcher =>
      for {
        _ <- IO {
          val rootLog = Logger.getLogger("")
          rootLog.setLevel(Level.FINE)
          rootLog.getHandlers().head.setLevel(Level.FINE)
        }
        ioLocal <- IOLocal(null: Context)
        storage = new IOLocalContextStorage(dispatcher, ioLocal)
        _ <- IO(ContextStorage.addWrapper(_ => storage))
        _ <- ioLocal.set(null)
        _ <- IO.println(ContextStorage.get().getClass)
        ctx = Context.root()
        _ <- Resource
          .make(IO(ctx.`with`(key, "hello").makeCurrent()))(scope =>
            IO(scope.close())
          )
          .surround {
            for {
              key <- IO(Context.current())
              _ <- IO.println(key)
            } yield ()
          }
        _ <- IO(Option(Context.current().get(key))).flatMap(v => IO.println(v))
      } yield ()
    }
}
