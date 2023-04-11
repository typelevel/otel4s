import cats.effect.{IO, IOApp, Resource}
import cats.syntax.foldable._
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader
import org.typelevel.otel4s.java.OtelJava
import org.typelevel.otel4s.metrics.preset.IOMetrics

import scala.jdk.CollectionConverters._
import scala.util.Random

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
      IO.delay(jMetricReader.collectAllMetrics().asScala.toList)
        .flatMap(_.traverse_(v => IO.println(v.getName + " = " + v.getData)))

    Resource
      .eval(OtelJava.forAsync[IO](jSdk))
      .evalMap(_.meterProvider.get("cats-effect-runtime-metrics"))
      .use { implicit meter =>
        IOMetrics.fromRuntimeMetrics[IO](runtime.metrics).surround {
          compute >> printMetrics
        }
      }
  }

  private def compute: IO[Unit] =
    IO.parReplicateAN(5)(20, IO.blocking(Thread.sleep(Random.nextInt(1000))))
      .void

}
