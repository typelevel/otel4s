package org.typelevel.otel4s.java

import cats.effect.IOLocal
import cats.effect.unsafe.IOLocals
import io.opentelemetry.context.Context
import io.opentelemetry.context.ContextStorage
import io.opentelemetry.context.Scope

class IOLocalContextStorage(
    ioLocal: IOLocal[Context]
) extends ContextStorage {

  override def attach(toAttach: Context): Scope = {
    val previous = current()
    IOLocals.set(ioLocal, toAttach)
    new Scope {
      def close() = IOLocals.set(ioLocal, previous)
    }
  }

  override def current(): Context =
    IOLocals.get(ioLocal)

}
