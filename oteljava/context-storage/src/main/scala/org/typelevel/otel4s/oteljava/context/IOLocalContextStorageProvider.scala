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

package org.typelevel.otel4s.oteljava.context

import cats.effect.IOLocal
import cats.effect.SyncIO
import cats.effect.unsafe.IORuntime
import cats.syntax.all._
import io.opentelemetry.context.{Context => JContext}
import io.opentelemetry.context.ContextStorage
import io.opentelemetry.context.ContextStorageProvider

object IOLocalContextStorageProvider {
  private[context] lazy val localContext: IOLocal[Context] =
    IOLocal[Context](Context.root)
      .syncStep(100)
      .flatMap(
        _.leftMap(_ =>
          new Error(
            "Failed to initialize the local context of the IOLocalContextStorageProvider."
          )
        ).liftTo[SyncIO]
      )
      .unsafeRunSync()

  private[context] lazy val threadLocalContext: ThreadLocal[Context] =
    new ThreadLocal[Context] {
      private val fiberLocal = localContext.unsafeThreadLocal()

      override def initialValue(): Context =
        Context.root

      override def get(): Context =
        if (IORuntime.isUnderFiberContext()) fiberLocal.get() else super.get()

      override def set(value: Context): Unit =
        if (IORuntime.isUnderFiberContext()) fiberLocal.set(value) else super.set(value)
    }

  private[context] lazy val threadLocalJContext: ThreadLocal[JContext] =
    new ThreadLocal[JContext] {
      override def initialValue(): JContext =
        JContext.root()

      override def get(): JContext =
        threadLocalContext.get().underlying

      override def set(value: JContext): Unit =
        threadLocalContext.set(Context.wrap(value))
    }

}

/** SPI implementation for [[`IOLocalContextStorage`]]. */
class IOLocalContextStorageProvider extends ContextStorageProvider {
  def get(): ContextStorage =
    new IOLocalContextStorage(
      () => IOLocalContextStorageProvider.localContext,
      () => IOLocalContextStorageProvider.threadLocalContext
    )
}
