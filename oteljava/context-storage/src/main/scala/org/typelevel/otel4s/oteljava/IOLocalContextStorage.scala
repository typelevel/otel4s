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

import cats.effect.IOLocal
import cats.effect.LiftIO
import cats.effect.MonadCancelThrow
import cats.mtl.Local
import io.opentelemetry.context.{Context => JContext}
import io.opentelemetry.context.ContextStorage
import io.opentelemetry.context.Scope
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.instances.local._
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.context.LocalContext

/** A `ContextStorage` backed by an [[cats.effect.IOLocal `IOLocal`]] of a
  * [[org.typelevel.otel4s.oteljava.context.Context `Context`]] that also provides [[cats.mtl.Local `Local`]] instances
  * that reflect the state of the backing `IOLocal`. Usage of `Local` and `ContextStorage` methods will be consistent
  * and stay in sync as long as effects are threaded properly.
  */
class IOLocalContextStorage(_ioLocal: () => IOLocal[Context]) extends ContextStorage {
  private[this] implicit lazy val ioLocal: IOLocal[Context] = _ioLocal()
  private[this] lazy val unsafeThreadLocal = ioLocal.unsafeThreadLocal()

  @inline private[this] def unsafeCurrent: Context =
    unsafeThreadLocal.get()

  override def attach(toAttach: JContext): Scope = {
    val previous = unsafeCurrent
    unsafeThreadLocal.set(Context.wrap(toAttach))
    () => unsafeThreadLocal.set(previous)
  }

  override def current(): JContext =
    unsafeCurrent.underlying

  /** @return
    *   a [[cats.mtl.Local `Local`]] of a [[org.typelevel.otel4s.oteljava.context.Context `Context`]] that reflects the
    *   state of the backing `IOLocal`
    */
  def local[F[_]: MonadCancelThrow: LiftIO]: LocalContext[F] = implicitly
}

object IOLocalContextStorage {

  /** Returns a [[cats.mtl.Local `Local`]] of a [[org.typelevel.otel4s.oteljava.context.Context `Context`]] if an
    * [[`IOLocalContextStorage`]] is configured to be used as the `ContextStorage` for the Java otel library.
    *
    * Raises an exception if an [[`IOLocalContextStorage`]] is __not__ configured to be used as the `ContextStorage` for
    * the Java otel library, or if [[cats.effect.IOLocal `IOLocal`]] propagation is not enabled.
    *
    * @example
    *   {{{
    * implicit val localProvider: LocalProvider[IO, Context] = IOLocalContextStorage.localProvider
    * OtelJava.autoConfigured[IO].use { otel4s =>
    *   ...
    * }
    *   }}}
    */
  def localProvider[F[_]: LiftIO](implicit F: MonadCancelThrow[F]): LocalProvider[F, Context] =
    new LocalProvider[F, Context] {
      def local: F[Local[F, Context]] =
        ContextStorage.get() match {
          case storage: IOLocalContextStorage =>
            if (IOLocal.isPropagating) {
              F.pure(storage.local)
            } else {
              F.raiseError(
                new IllegalStateException(
                  "IOLocal propagation must be enabled with: -Dcats.effect.ioLocalPropagation=true"
                )
              )
            }
          case _ =>
            F.raiseError(
              new IllegalStateException(
                "IOLocalContextStorage is not configured for use as the ContextStorageProvider"
              )
            )
        }
    }

}
