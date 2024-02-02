package example

// format: off
import cats.effect.Async
import cats.effect.LiftIO
import cats.effect.IOLocal
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.oteljava._
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.oteljava.trace._
import org.typelevel.otel4s.oteljava.trace.Traces
import org.typelevel.otel4s.oteljava.metrics._
import org.typelevel.otel4s.oteljava.metrics.Metrics
import org.typelevel.otel4s.oteljava.context._
import org.typelevel.otel4s.oteljava.context.AskContext
import org.typelevel.otel4s.oteljava.context.LocalContext
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.instances.local._
import org.typelevel.otel4s.instances.local.localForIOLocal
import org.typelevel.otel4s.instances.local.{localForIOLocal => liftLocal}
// format: on

object Test {

  def makeLocal[F[_]: Async: LiftIO](implicit local: IOLocal[Context]): Unit = {
    val b = localForIOLocal
    val c = localForIOLocal(Async[F], LiftIO[F], local)
  }

  def program[F[_]](implicit otelJava: OtelJava[F]): Unit = ???

  def traces[F[_]](implicit traces: Traces[F]): Unit = ???

  def metrics[F[_]](implicit metrics: Metrics[F]): Unit = ???

  def askCtx[F[_]: AskContext]: Unit = ???

  def localCtx[F[_]: LocalContext]: Unit = ???

  def meterOps[F[_]: Async](implicit meter: Meter[F]): Unit = {
    meter.counter[Long]("counter").create
    meter.histogram[Double]("histogram").withUnit("unit").create
    meter.upDownCounter[Long]("upDownCounter").create
    meter.observableGauge[Double]("OG").create(Async[F].pure(Nil))
    meter.observableCounter[Long]("OC").create(Async[F].pure(Nil))
    meter.observableUpDownCounter[Long]("OUDC").create(Async[F].pure(Nil))
  }

}
