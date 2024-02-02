/* rule=V0_5_0Rewrites */

package example

// format: off
import cats.effect.Async
import cats.effect.LiftIO
import cats.effect.IOLocal
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.java._
import org.typelevel.otel4s.java.OtelJava
import org.typelevel.otel4s.java.trace._
import org.typelevel.otel4s.java.trace.Traces
import org.typelevel.otel4s.java.metrics._
import org.typelevel.otel4s.java.metrics.Metrics
import org.typelevel.otel4s.java.context._
import org.typelevel.otel4s.java.context.AskContext
import org.typelevel.otel4s.java.context.LocalContext
import org.typelevel.otel4s.java.context.Context
import org.typelevel.otel4s.java.instances._
import org.typelevel.otel4s.java.instances.localForIoLocal
import org.typelevel.otel4s.java.instances.{localForIoLocal => liftLocal}
// format: on

object Test {

  def makeLocal[F[_]: Async: LiftIO](implicit local: IOLocal[Context]): Unit = {
    val b = localForIoLocal
    val c = localForIoLocal(Async[F], LiftIO[F], local)
  }

  def program[F[_]](implicit otelJava: OtelJava[F]): Unit = ???

  def traces[F[_]](implicit traces: Traces[F]): Unit = ???

  def metrics[F[_]](implicit metrics: Metrics[F]): Unit = ???

  def askCtx[F[_]: AskContext]: Unit = ???

  def localCtx[F[_]: LocalContext]: Unit = ???

  def meterOps[F[_]: Async](implicit meter: Meter[F]): Unit = {
    meter.counter("counter").create
    meter.histogram("histogram").withUnit("unit").create
    meter.upDownCounter("upDownCounter").create
    meter.observableGauge("OG").create(Async[F].pure(Nil))
    meter.observableCounter("OC").create(Async[F].pure(Nil))
    meter.observableUpDownCounter("OUDC").create(Async[F].pure(Nil))
  }

}
