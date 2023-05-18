import cats.effect.IO
import cats.effect.IOApp
import cats.effect.IOLocal
import cats.effect.Resource
import cats.effect.unsafe.IOLocals
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
      _ = IOLocals.set(ioLocal, Context.root())
      _ <- Resource
        .make(IO(ctx.`with`(key, "hello").makeCurrent()))(scope =>
          IO(scope.close())
        )
        .surround(printKey)
      _ <- printKey
    } yield ()
}
