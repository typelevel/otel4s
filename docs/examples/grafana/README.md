# Grafana - All-in-one

In this example, we are going to use [Prometheus](https://prometheus.io/) and [Tempo](https://grafana.com/oss/tempo/), both displayed inside [Grafana](https://grafana.com/grafana/).
Just like [Jaeger example](../jaeger-docker/README.md), we will need to setup a collector to gather both metrics and traces.

### Project setup

Configure the project using your favorite tool, once again setup is similar to the Jaeger example, only difference being that we export metrics as they will be stored inside Prometheus:

@:select(build-tool)

@:choice(sbt)

Add settings to the `build.sbt`:
```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "otel4s-java" % "@VERSION@", // <1>
  "io.opentelemetry" % "opentelemetry-exporter-otlp" % "@OPEN_TELEMETRY_VERSION@" % Runtime, // <2>
  "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % "@OPEN_TELEMETRY_VERSION@" % Runtime // <3>
)
run / fork := true
javaOptions += "-Dotel.java.global-autoconfigure.enabled=true" // <4>
javaOptions += "-Dotel.service.name=grafana-example"           // <5>
```

@:choice(scala-cli)

Add directives to the `grafana.scala`:
```scala
//> using scala 3.3.0
//> using lib "org.typelevel::otel4s-java:@VERSION@" // <1>
//> using lib "io.opentelemetry:opentelemetry-exporter-otlp:@OPEN_TELEMETRY_VERSION@" // <2>
//> using lib "io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:@OPEN_TELEMETRY_VERSION@" // <3>
//> using `java-opt` "-Dotel.java.global-autoconfigure.enabled=true" // <4>
//> using `java-opt` "-Dotel.service.name=grafana-example"           // <5>
```

@:@

1) Add the `otel4s` library
2) Add an OpenTelemetry exporter. Without the exporter, the application will crash
3) Add an OpenTelemetry autoconfigure extension
4) Enable OpenTelemetry SDK autoconfigure mode
5) Add the name of the application to use in the traces


### OpenTelemetry SDK configuration

As mentioned above, we use `otel.service.name` and `otel.metrics.exporter` system properties to configure the
OpenTelemetry SDK.
The SDK can be configured via environment variables too. Check the full list
of [environment variable configurations](https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md)
for more options.

### Observability stack configuration

Use the following `docker-compose.yaml` file:

```yaml
version: '3.7'
services:
  otel4s-grafana-example:
    image: otel4s-grafana-example:0.1.0-SNAPSHOT
    environment:
      OTEL_EXPORTER_OTLP_ENDPOINT: "http://otel-collector:4317"
    restart: on-failure
    ports:
      - "8080:8080"
    networks:
      - static-network

  otel-collector:
    image: otel/opentelemetry-collector-contrib
    command: [--config=/etc/otel-collector-config.yaml]
    volumes:
      - ./dependencies/opentelemetry/otel-collector-config.yaml:/etc/otel-collector-config.yaml
    ports:
      - "8888:8888" # Prometheus metrics exposed by the collector
      - "8889:8889" # Prometheus exporter metrics
      - "4317:4317" # OTLP gRPC receiver
      - "4318:4318" # OTLP http receiver
    networks:
      - static-network

  jaeger:
    image: jaegertracing/all-in-one:latest
    volumes:
      - "./dependencies/jaeger/jaeger-ui.json:/etc/jaeger/jaeger-ui.json"
    command: --query.ui-config /etc/jaeger/jaeger-ui.json
    environment:
      - METRICS_STORAGE_TYPE=prometheus
      - PROMETHEUS_SERVER_URL=http://prometheus:9090
    ports:
      - "14250:14250"
      - "16685:16685" # GRPC
      - "16686:16686" # UI
    networks:
      - static-network

  prometheus:
    image: prom/prometheus:latest
    volumes:
      - "./dependencies/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml"
    ports:
      - "9090:9090"
    networks:
      - static-network

  grafana:
    image: grafana/grafana-oss
    restart: unless-stopped
    ports:
      - "3000:3000"
    networks:
      - static-network

networks:
  static-network:
```

### Application example

Example service mocking a call to a remote API: here the remote API returns apples or bananas.
We're using the metrics to measure the apple/banana ratio returned by the API,
and the traces to measure the latency of this API. 

```scala 3
import cats.effect.Async
import cats.effect.std.Random
import cats.implicits.{catsSyntaxApply, toFlatMapOps, toFunctorOps}
import io.circe.derivation.{Configuration, ConfiguredCodec}
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.trace.Tracer

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

given Configuration = Configuration.default
case class ApiData(result: String) derives ConfiguredCodec

trait ExampleService[F[_]] {
  def getDataFromSomeAPI: F[ApiData]
}

object ExampleService {
  def apply[F[_]: Async: Tracer: Meter: Random](
      minLatency: Int,
      maxLatency: Int,
      bananaPercentage: Int
  ): F[ExampleService[F]] = {
    val metricsProvider = summon[Meter[F]]
    metricsProvider
      .counter("RemoteApi.fruit.count")
      .withDescription("Number of fruits returned by the API.")
      .create
      .map { remoteApiFruitCount =>
        new ExampleService[F] {
          private val spanBuilder = Tracer[F].spanBuilder("remoteAPI.com/fruit").build

          override def getDataFromSomeAPI: F[ApiData] = for {
            latency <- Random[F].betweenInt(minLatency, maxLatency)
            isBanana <- Random[F].betweenInt(0, 100).map(_ <= bananaPercentage)
            duration = FiniteDuration(latency, TimeUnit.MILLISECONDS)
            fruit <- spanBuilder.surround(
              Async[F].sleep(duration) *>
                Async[F].pure(if isBanana then "banana" else "apple")
            )
            _ <- remoteApiFruitCount.inc(Attribute("fruit", fruit))
          } yield ApiData(s"Api returned a $fruit !")
        }
      }
  }
}
```

### Run the application

@:select(build-tool)

@:choice(sbt)

```shell
$ sbt run
```

@:choice(scala-cli)

```shell
$ scala-cli run grafana.scala
```

@:@

### Setup your metrics dashboard

Connect to grafana and Add your Jaeger and Grafana data sources. Once done, you can create your [first dashboard](https://github.com/typelevel/otel4s/tree/main/docs/examples/grafana/dashboards/metrics-dashboard.json).
Now after making a few calls to `ExampleService.getDataFromSomeAPI`, you should get some data points:

@:image(metrics-grafana.png) {
    alt = Grafana Metrics Example
}

### Adding your traces

Just like you just did with Prometheus, you can use Jaeger as data source and display traces using the appropriate data visualization in your [new dashboard](https://github.com/typelevel/otel4s/tree/main/docs/examples/grafana/dashboards/traces-dashboard.json):

@:image(traces-grafana.png) {
alt = Grafana Traces Example
}

> Note: a complete example is available [here](https://github.com/keuhdall/otel4s-grafana-example/)