import cats.effect.IO
import cats.effect.IOApp
import cats.effect.MonadCancelThrow
import cats.effect.std.Console
import cats.syntax.all._
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import org.typelevel.otel4s.trace.Tracer
import org.typelevel.otel4s.java.OtelJava

trait Work[F[_]] {
  def doWork: F[Unit]
}

object Work {
  def apply[F[_]: MonadCancelThrow: Tracer: Console]: Work[F] =
    new Work[F] {
      def doWork: F[Unit] =
        implicitly[Tracer[F]].span("Work.DoWork").use { span =>
          span.addEvent("Starting the work.") *>
            doWorkInternal *>
            span.addEvent("Finished working.")
        }

      def doWorkInternal =
        Console[F].println("Doin' work")
    }
}

class TracingExample extends IOApp.Simple {
  def getTracer: IO[Tracer[IO]] =
    for {
      otel4j <- IO(
        AutoConfiguredOpenTelemetrySdk.builder.build().getOpenTelemetrySdk
      )
      otel4s <- OtelJava.forSync(otel4j)
      tracerProvider = otel4s.tracerProvider
      tracer <- tracerProvider.tracer("Example").get
    } yield tracer

  def run: IO[Unit] = {
    getTracer.flatMap { implicit tracer: Tracer[IO] =>
      Work[IO].doWork
    }
  }
}
