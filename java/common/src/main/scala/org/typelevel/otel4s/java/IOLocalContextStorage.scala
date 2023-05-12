package org.typelevel.otel4s.java

import cats.effect.IOLocal
import cats.effect.LiftIO
import cats.effect.std.Dispatcher
import io.opentelemetry.context.Context
import io.opentelemetry.context.ContextStorage
import io.opentelemetry.context.Scope

class IOLocalContextStorage[F[_]: LiftIO](
    dispatcher: Dispatcher[F],
    ioLocal: IOLocal[Context]
) extends ContextStorage {

  override def attach(toAttach: Context): Scope =
    dispatcher.unsafeRunSync(
      currentOrRoot
        .flatMap { old =>
          ioLocal
            .set(toAttach)
            .as(new Scope {
              def close() = dispatcher.unsafeRunSync(ioLocal.set(old).to[F])
            })
        }
        .to[F]
    )

  override def current(): Context = {
    dispatcher.unsafeRunSync(currentOrRoot.to[F])
  }

  private def currentOrRoot = ioLocal.get.map {
    case null => Context.root()
    case ctx  => ctx
  }
}
