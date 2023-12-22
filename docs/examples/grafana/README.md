# Grafana - All-in-one

In this example, we are going to use [Prometheus](https://prometheus.io/) and [Jaeger](https://jaegertracing.io/), both displayed inside [Grafana](https://grafana.com/grafana/).
Just like [Jaeger example](../jaeger-docker/README.md), we will need to set up an [Open Telemetry Collector](https://opentelemetry.io/docs/collector/) to gather both metrics and traces.

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
javaOptions += "-Dotel.java.global-autoconfigure.enabled=true"       // <4>
javaOptions += "-Dotel.service.name=grafana-example"                 // <5>
javaOptions += "-Dotel.exporter.otlp.endpoint=http://localhost:4317" // <6>
```

@:choice(scala-cli)

Add directives to the `grafana.scala`:
```scala
//> using scala 3.3.0
//> using lib "org.typelevel::otel4s-java:@VERSION@" // <1>
//> using lib "io.opentelemetry:opentelemetry-exporter-otlp:@OPEN_TELEMETRY_VERSION@" // <2>
//> using lib "io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:@OPEN_TELEMETRY_VERSION@" // <3>
//> using `java-opt` "-Dotel.java.global-autoconfigure.enabled=true"       // <4>
//> using `java-opt` "-Dotel.service.name=grafana-example"                 // <5>
//> using `java-opt` "-Dotel.exporter.otlp.endpoint=http://localhost:4317" // <6>
```

@:@

1) Add the `otel4s` library  
2) Add an OpenTelemetry exporter. Without the exporter, the application will crash  
3) Add an OpenTelemetry autoconfigure extension  
4) Enable OpenTelemetry SDK autoconfigure mode  
5) Add the name of the application to use in the traces    
6) Add the OpenTelemetry Collector endpoint


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
  otel-collector: # receives application metrics and traces via gRPC or HTTP protocol
    image: otel/opentelemetry-collector-contrib
    command: [--config=/etc/otel-collector-config.yaml]
    volumes:
      - "./config/otel-collector-config.yaml:/etc/otel-collector-config.yaml"
    ports:
      - "8888:8888" # Prometheus metrics exposed by the collector
      - "8889:8889" # Prometheus exporter metrics
      - "4317:4317" # OTLP gRPC receiver
      - "4318:4318" # OTLP http receiver
    networks:
      - static-network

  jaeger: # stores traces received from the OpenTelemetry Collector 
    image: jaegertracing/all-in-one:latest
    volumes:
      - "./config/jaeger-ui.json:/etc/jaeger/jaeger-ui.json"
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

  prometheus: # scrapes metrics from the OpenTelemetry Collector
    image: prom/prometheus:latest
    volumes:
      - "./config/prometheus.yml:/etc/prometheus/prometheus.yml"
    ports:
      - "9090:9090"
    networks:
      - static-network

  grafana: # queries Jaeger and Prometheus to visualize traces and metrics
    image: grafana/grafana-oss
    restart: unless-stopped
    ports:
      - "3000:3000"
    networks:
      - static-network

networks:
  static-network:
```

### Configuration files

#### ./config/otel-collector-config.yaml

OpenTelemetry Collector configuration: receivers, exporters, and processing pipelines.

```yml
receivers:
  otlp:
    protocols: # enable OpenTelemetry Protocol receiver, both gRPC and HTTP
      grpc:
      http:

exporters:
  jaeger: # export received traces to Jaeger
    endpoint: jaeger:14250
    tls:
      insecure: true

  prometheus: # run Prometheus exporter server on port 8889, so Prometheus can scrape the metrics  
    endpoint: 0.0.0.0:8889
    send_timestamps: true

processors:
  batch:
    timeout: 10s

service:
  pipelines:
    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [jaeger]
    metrics:
      receivers: [otlp]
      processors: [batch]
      exporters: [prometheus]
```

#### ./config/prometheus.yml

Prometheus server configuration: scrape interval and targets. 

```yml
global:
  scrape_interval: 15s # Set the scrape interval to every 15 seconds. Default is every 1 minute.
  evaluation_interval: 15s # Evaluate rules every 15 seconds. The default is every 1 minute.

scrape_configs:
  # The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
  - job_name: "prometheus" # scrape prometheus itself to collect the internal metrics (e.g. scrape stats, etc)
    static_configs:
      - targets: ["localhost:9090"]

  - job_name: "otel-collector" # scrape metrics from the OpenTelemetry collector
    static_configs:
      - targets: ["otel-collector:8889"]
```

#### ./config/jaeger-ui.json

Jaeger configuration: enable [Service Performance Monitor (SPM)](https://www.jaegertracing.io/docs/1.48/spm/). 

```json
{
  "monitor": {
    "menuEnabled": true
  },
  "dependencies": {
    "menuEnabled": true
  }
}
```

### Application example

Example service mocking a call to a remote API: here the remote API returns apples or bananas.
We're using the metrics to measure the apple/banana ratio returned by the API,
and the traces to measure the latency of this API. 

```scala mdoc:silent
import cats.effect.{Async, IO, IOApp}
import cats.effect.std.Random
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.trace.Tracer

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

case class ApiData(result: String)

trait ApiService[F[_]] {
  def getDataFromSomeAPI: F[ApiData]
}

object ApiService {
  def apply[F[_]: Async: Tracer: Meter: Random](
      minLatency: Int,
      maxLatency: Int,
      bananaPercentage: Int
  ): F[ApiService[F]] = 
    Meter[F]
      .counter("RemoteApi.fruit.count")
      .withDescription("Number of fruits returned by the API.")
      .create
      .map { remoteApiFruitCount =>
        new ApiService[F] {
          override def getDataFromSomeAPI: F[ApiData] = for {
            latency <- Random[F].betweenInt(minLatency, maxLatency)
            isBanana <- Random[F].betweenInt(0, 100).map(_ <= bananaPercentage)
            duration = FiniteDuration(latency, TimeUnit.MILLISECONDS)
            fruit <- Tracer[F].span("remoteAPI.com/fruit").surround(
              Async[F].sleep(duration) *>
                Async[F].pure(if (isBanana) "banana" else "apple")
            )
            _ <- remoteApiFruitCount.inc(Attribute("fruit", fruit))
          } yield ApiData(s"Api returned a $fruit !")
        }
      }
}

object ExampleService extends IOApp.Simple {
  def run: IO[Unit] =
    OtelJava.global.flatMap { otel4s =>
      (
        otel4s.tracerProvider.get("com.service.runtime"),
        otel4s.meterProvider.get("com.service.runtime"),
        Random.scalaUtilRandom[IO]
      ).flatMapN { case components =>
        implicit val (tracer: Tracer[IO], meter: Meter[IO], random: Random[IO]) = 
          components

        for {
          service <- ApiService[IO](
            minLatency = 40, 
            maxLatency = 80, 
            bananaPercentage = 70
          )
          data <- service.getDataFromSomeAPI
          _ <- IO.println(s"Service data: $data")
        } yield ()
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
Now after making a few calls to `ApiService.getDataFromSomeAPI`, you should get some data points:

@:image(metrics-grafana.png) {
    alt = Grafana Metrics Example
}

### Adding your traces

Just like you just did with Prometheus, you can use Jaeger as data source and display traces using the appropriate data visualization in your [new dashboard](https://github.com/typelevel/otel4s/tree/main/docs/examples/grafana/dashboards/traces-dashboard.json):

@:image(traces-grafana.png) {
alt = Grafana Traces Example
}

> Note: a complete example is available [here](https://github.com/keuhdall/otel4s-grafana-example/)