package org.typelevel.otel4s.oteljava.testkit.context

import cats.effect.LiftIO
import cats.effect.MonadCancelThrow
import cats.effect.std.Console
import cats.mtl.Local
import io.opentelemetry.sdk.testing.context.SettableContextStorageProvider
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.context.IOLocalContextStorage

object IOLocalTestContextStorage {

  def localProvider[F[_]: Console: LiftIO](implicit F: MonadCancelThrow[F]): LocalProvider[F, Context] =
    new LocalProvider[F, Context] {
      val delegate = IOLocalContextStorage.localProvider[F]

      override def local: F[Local[F, Context]] =
        SettableContextStorageProvider.getContextStorage() match {
          case storage: IOLocalContextStorage => IOLocalContextStorage.whenPropagationEnabled(storage.local)
          case _                              => delegate.local
        }
    }

}
