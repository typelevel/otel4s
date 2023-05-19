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

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.IOLocal
import cats.effect.Resource
import io.opentelemetry.context.Context
import io.opentelemetry.context.ContextKey
import io.opentelemetry.context.ContextStorage
import org.typelevel.otel4s.java.IOLocalContextStorage
import java.util.logging._

object ContextStorageExample extends IOApp.Simple {

  val key = ContextKey.named[String]("test")

  val printKey =
    IO(Option(Context.current().get(key))).flatMap(v => IO.println(v))

  def run =
    for {
      _ <- IO {
        val rootLog = Logger.getLogger("")
        rootLog.setLevel(Level.FINE)
        rootLog.getHandlers().head.setLevel(Level.FINE)
      }
      ioLocal <- IOLocal(null: Context)
      storage = new IOLocalContextStorage(ioLocal)
      _ <- IO(ContextStorage.addWrapper(_ => storage))
      ctx = Context.root()
      _ <- ioLocal.set(Context.root())
      _ <- Resource
        .make(IO(ctx.`with`(key, "hello").makeCurrent()))(scope =>
          IO(scope.close())
        )
        .surround(printKey)
      _ <- printKey
    } yield ()
}
