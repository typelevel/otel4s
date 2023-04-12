import cats.effect.std.Random
import cats.effect.{IO, IOApp, Resource}
import cats.syntax.foldable._
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader
import org.typelevel.otel4s.java.OtelJava
import org.typelevel.otel4s.metrics.preset.IOMetrics

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

object RuntimeMetricsExample extends IOApp.Simple {
  def run: IO[Unit] = {
    val jMetricReader = InMemoryMetricReader.create()
    val jSdk = {
      val meterProvider = SdkMeterProvider
        .builder()
        .registerMetricReader(jMetricReader)
        .build()

      val sdk = OpenTelemetrySdk
        .builder()
        .setMeterProvider(meterProvider)
        .build()

      sdk
    }

    def printMetrics: IO[Unit] =
      for {
        _ <- IO.println("New cycle: ")
        metrics <- IO.delay(jMetricReader.collectAllMetrics().asScala.toList)
        _ <- metrics.traverse_(v => IO.println(v.getName + " = " + v.getData))
      } yield ()

    Resource
      .eval(OtelJava.forAsync[IO](jSdk))
      .evalMap(_.meterProvider.get("cats-effect-runtime-metrics"))
      .use { implicit meter =>
        IOMetrics.fromRuntimeMetrics[IO](runtime.metrics).surround {
          printMetrics.delayBy(500.millis).foreverM.background.surround {
            compute
          }
        }
      }
  }

  private def compute: IO[Unit] =
    Random.scalaUtilRandom[IO].flatMap { random =>
      val io = random.betweenLong(10, 3000).flatMap { delay =>
        if (delay % 2 == 0) IO.blocking(Thread.sleep(delay))
        else IO.delay(Thread.sleep(delay))
      }

      IO.parReplicateAN(30)(100, io).void
    }

}
