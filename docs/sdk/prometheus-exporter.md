# Prometheus Exporter

The exporter exports metrics in a prometheus-compatible format, so Prometheus can scrape metrics from the HTTP server.
You can either allow exporter to launch its own server or add Prometheus routes to the existing one. 

An example of output (e.g. `curl -H "Accept:text/plain" http://localhost:9464/metrics`):
```scala mdoc:passthrough
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s._
import org.http4s.syntax.literals._
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.exporter.prometheus._
import org.typelevel.otel4s.sdk.metrics.SdkMetrics

val response = PrometheusMetricExporter.builder[IO].build.flatMap { exporter =>
  SdkMetrics
    .autoConfigured[IO](
      _.withConfig(Config.ofProps(Map("otel.metrics.exporter" -> "none")))
       .addMeterProviderCustomizer((b, _) => b.registerMetricReader(exporter.metricReader))
    )
    .use { autoConfigured =>
      val sdk = autoConfigured
      val routes = PrometheusHttpRoutes.routes[IO](exporter, PrometheusWriter.Config.default)
      for {
        meter <- sdk.meterProvider.meter("meter").get
        counter <- meter.counter[Long]("counter").create
        _ <- counter.inc()
        response <- routes.orNotFound.run(Request(Method.GET, uri"/metrics"))
        body <- response.bodyText.compile.foldMonoid
      } yield body
    }
}.unsafeRunSync()

println("```yaml")
println(response)
println("```")
```

## Getting Started

@:select(build-tool)

@:choice(sbt)

Add settings to the `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %%% "otel4s-sdk" % "@VERSION@", // <1>
  "org.typelevel" %%% "otel4s-sdk-exporter-prometheus" % "@VERSION@", // <2>
)
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using dep "org.typelevel::otel4s-sdk::@VERSION@" // <1>
//> using dep "org.typelevel::otel4s-sdk-exporter-prometheus::@VERSION@" // <2>
```

@:@

1. Add the `otel4s-sdk` library
2. Add the `otel4s-sdk-exporter-prometheus` library

## Configuration

The `OpenTelemetrySdk.autoConfigured(...)` and `SdkMetrics.autoConfigured(...)` rely on the environment variables 
and system properties to configure the SDK.
Check out the [configuration details](configuration.md#prometheus-exporter).

## Autoconfigured (built-in server)

By default, Prometheus metrics exporter will launch its own HTTP server. 
To make autoconfiguration work, we must configure the `otel.metrics.exporter` property:

@:select(sdk-options-source)

@:choice(sbt)

Add settings to the `build.sbt`:

```scala
javaOptions += "-Dotel.metrics.exporter=prometheus"
javaOptions += "-Dotel.traces.exporter=none"
envVars ++= Map("OTEL_METRICS_EXPORTER" -> "prometheus", "OTEL_TRACES_EXPORTER" -> "none")
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using javaOpt -Dotel.metrics.exporter=prometheus
//> using javaOpt -Dotel.traces.exporter=none
```

@:choice(shell)

```shell
$ export OTEL_METRICS_EXPORTER=prometheus
$ export OTEL_TRACES_EXPORTER=none
```
@:@

Then autoconfigure the SDK:

@:select(sdk-entry-point)

@:choice(sdk)

`OpenTelemetrySdk.autoConfigured` configures both `MeterProvider` and `TracerProvider`:

```scala mdoc:reset:silent
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.OpenTelemetrySdk
import org.typelevel.otel4s.sdk.exporter.prometheus.autoconfigure.PrometheusMetricExporterAutoConfigure
import org.typelevel.otel4s.trace.TracerProvider

object TelemetryApp extends IOApp.Simple {

  def run: IO[Unit] =
    OpenTelemetrySdk
      .autoConfigured[IO](
        // register Prometheus exporter configurer
        _.addMetricExporterConfigurer(PrometheusMetricExporterAutoConfigure[IO])
      )
      .use { autoConfigured =>
        val sdk = autoConfigured.sdk
          
        program(sdk.meterProvider, sdk.tracerProvider) >> IO.never
      }

  def program(
      meterProvider: MeterProvider[IO],
      tracerProvider: TracerProvider[IO]
  ): IO[Unit] = {
    val _ = tracerProvider
    for {
      meter <- meterProvider.meter("meter").get
      counter <- meter.counter[Long]("counter").create
      _ <- counter.inc()
    } yield ()
  }
}
```

@:choice(metrics)

`SdkMetrics` configures only `MeterProvider`:

```scala mdoc:reset:silent
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.exporter.prometheus.autoconfigure.PrometheusMetricExporterAutoConfigure
import org.typelevel.otel4s.sdk.metrics.SdkMetrics

object TelemetryApp extends IOApp.Simple {

  def run: IO[Unit] =
    SdkMetrics
      .autoConfigured[IO](
        // register Prometheus exporters configurer
        _.addExporterConfigurer(PrometheusMetricExporterAutoConfigure[IO])
      )
      .use { autoConfigured =>
        program(autoConfigured.meterProvider)
      }

  def program(
      meterProvider: MeterProvider[IO]
  ): IO[Unit] =
    for {
      meter <- meterProvider.meter("meter").get
      counter <- meter.counter[Long]("counter").create
      _ <- counter.inc()
    } yield ()
}
```

@:@

The SDK will launch an HTTP server, and you can scrape metrics from the `http://localhost:9464/metrics` endpoint. 

## Manual (use Prometheus routes with existing server)

If you already run an HTTP server, you can attach Prometheus routes to it. 
For example, you can expose Prometheus metrics at `/prometheus/metrics` alongside your app routes. 

**Note**: since we configure the exporter manually, the exporter autoconfiguration must be disabled.

@:select(sdk-options-source)

@:choice(sbt)

Add settings to the `build.sbt`:

```scala
javaOptions += "-Dotel.metrics.exporter=none"
javaOptions += "-Dotel.traces.exporter=none"
envVars ++= Map("OTEL_METRICS_EXPORTER" -> "none", "OTEL_TRACES_EXPORTER" -> "none")
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using javaOpt -Dotel.metrics.exporter=none
//> using javaOpt -Dotel.traces.exporter=none
```

@:choice(shell)

```shell
$ export OTEL_METRICS_EXPORTER=none
$ export OTEL_TRACES_EXPORTER=none
```
@:@

Then autoconfigure the SDK and attach Prometheus routes to your HTTP server:

@:select(sdk-entry-point)

@:choice(sdk)

`OpenTelemetrySdk.autoConfigured` configures both `MeterProvider` and `TracerProvider`:

```scala mdoc:reset:silent
import cats.effect.{IO, IOApp}
import cats.syntax.semigroupk._
import org.http4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.OpenTelemetrySdk
import org.typelevel.otel4s.sdk.exporter.prometheus._
import org.typelevel.otel4s.trace.TracerProvider

object TelemetryApp extends IOApp.Simple {

  def run: IO[Unit] =
    PrometheusMetricExporter.builder[IO].build.flatMap { exporter =>
      OpenTelemetrySdk
        .autoConfigured[IO](
            // disable exporter autoconfiguration
            // can be skipped if you use system properties or env variables
          _.addPropertiesCustomizer(_ => Map("otlp.metrics.exporter" -> "none"))
            // register Prometheus exporter 
           .addMeterProviderCustomizer((b, _) => 
              b.registerMetricReader(exporter.metricReader)
            )
        )
        .use { autoConfigured =>
          val sdk = autoConfigured.sdk
          
          val appRoutes: HttpRoutes[IO] = HttpRoutes.empty // your app routes
          
          val writerConfig = PrometheusWriter.Config.default
          val prometheusRoutes = PrometheusHttpRoutes.routes[IO](exporter, writerConfig)
          
          val routes = appRoutes <+> Router("prometheus/metrics" -> prometheusRoutes)

          EmberServerBuilder.default[IO].withHttpApp(routes.orNotFound).build.use { _ =>
            program(sdk.meterProvider, sdk.tracerProvider) >> IO.never
          }
        }
    }

  def program(
      meterProvider: MeterProvider[IO],
      tracerProvider: TracerProvider[IO]
  ): IO[Unit] = {
    val _ = tracerProvider
    for {
      meter <- meterProvider.meter("meter").get
      counter <- meter.counter[Long]("counter").create
      _ <- counter.inc()
    } yield ()
  }
}
```

@:choice(metrics)

`SdkMetrics` configures only `MeterProvider`:

```scala mdoc:reset:silent
import cats.effect.{IO, IOApp}
import cats.syntax.semigroupk._
import org.http4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.exporter.prometheus._
import org.typelevel.otel4s.sdk.metrics.SdkMetrics

object TelemetryApp extends IOApp.Simple {

  def run: IO[Unit] =
    PrometheusMetricExporter.builder[IO].build.flatMap { exporter =>
      SdkMetrics
        .autoConfigured[IO](
            // disable exporter autoconfiguration
            // can be skipped if you use system properties or env variables
          _.addPropertiesCustomizer(_ => Map("otlp.metrics.exporter" -> "none"))
            // register Prometheus exporter 
            .addMeterProviderCustomizer((b, _) => 
              b.registerMetricReader(exporter.metricReader)
            )
        )
        .use { autoConfigured =>
          val appRoutes: HttpRoutes[IO] = HttpRoutes.empty // your app routes
          
          val writerConfig = PrometheusWriter.Config.default
          val prometheusRoutes = PrometheusHttpRoutes.routes[IO](exporter, writerConfig)
          
          val routes = appRoutes <+> Router("prometheus/metrics" -> prometheusRoutes)

          EmberServerBuilder.default[IO].withHttpApp(routes.orNotFound).build.use { _ =>
            program(autoConfigured.meterProvider) >> IO.never
          }
        }
    }

  def program(
      meterProvider: MeterProvider[IO]
  ): IO[Unit] =
    for {
      meter <- meterProvider.meter("meter").get
      counter <- meter.counter[Long]("counter").create
      _ <- counter.inc()
    } yield ()
}
```

@:@

That way you attach Prometheus routes to the existing HTTP server, 
and you can scrape metrics from the http://localhost:8080/prometheus/metrics endpoint.
