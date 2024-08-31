# Configuration

The `OpenTelemetrySdk.autoConfigured(...)` and `SdkTraces.autoConfigured(...)` rely on the environment variables and system properties to configure the SDK.

There are several ways to configure the options:

@:select(sdk-options-source)

@:choice(sbt)

Add settings to the `build.sbt`:

```scala
javaOptions += "-Dotel.service.name=auth-service"
envVars ++= Map("OTEL_SERVICE_NAME" -> "auth-service")
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using javaOpt -Dotel.service.name=auth-service
```

@:choice(shell)

```shell
$ export OTEL_SERVICE_NAME=auth-service
```
@:@

## Common

| System property   | Environment variable  | Description                                               |
|-------------------|-----------------------|-----------------------------------------------------------|
| otel.sdk.disabled | OTEL\\_SDK\\_DISABLED | If true returns a no-op SDK instance. Default is `false`. |


## Telemetry resource

It's highly recommended to specify the `service.name` for your application. 
For example, `auth-service` could be an application that handles authentication requests, 
and `jobs-dispatcher` could be an application that processes background jobs.

If not specified, SDK defaults the service name to `unknown_service:scala`.

| System property                          | Environment variable                             | Description                                                                                                 |
|------------------------------------------|--------------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| otel.resource.attributes                 | OTEL\\_RESOURCE\\_ATTRIBUTES                     | Specify resource attributes in the following format: `key1=val1,key2=val2,key3=val3`.                       |
| otel.service.name                        | OTEL\\_SERVICE\\_NAME                            | Specify logical service name. Takes precedence over `service.name` defined with `otel.resource.attributes`. |
| otel.experimental.resource.disabled-keys | OTEL\\_EXPERIMENTAL\\_RESOURCE\\_DISABLED\\_KEYS | Specify resource attribute keys that are filtered.                                                          |
| otel.otel4s.resource.detectors.enabled   | OTEL\\_OTEL4S\\_RESOURCE\\_DETECTORS\\_ENABLED   | Specify resource detectors to use. Defaults to `host,os,process,process_runtime`.                           | 
| otel.otel4s.resource.detectors.disabled  | OTEL\\_OTEL4S\\_RESOURCE\\_DETECTORS\\_DISABLED  | Specify resource detectors to disable.                                                                      | 

### Telemetry resource detectors

`TelemetryResourceDetector` adds environment-aware attributes to the telemetry resource.
For example, `HostDetector` will add `host.arch` and `host.name` attributes.
By default, the following resource detectors are enabled: `host`, `os`, `process`, `process_runtime`.

To disable all detectors, set `-Dotel.otel4s.resource.detectors.enabled=none`. 
To disable some detectors, use `-Dotel.otel4s.resource.detectors.disabled=host,os,process`.

## Metrics

### Exporters

| System property       | Environment variable      | Description                                                                                                              |
|-----------------------|---------------------------|--------------------------------------------------------------------------------------------------------------------------|
| otel.metrics.exporter | OTEL\\_METRICS\\_EXPORTER | List of exporters to be export metrics, separated by commas. `none` means no autoconfigured exporter. Default is `otlp`. |


Options supported out of the box:

- `otlp` - requires `otel4s-sdk-exporter` dependency.
- `console` - prints metrics to stdout. It's mainly used for testing and debugging.
- `none` - means no autoconfigured exporter.

### OTLP exporter

The exporter can be configured by two sets of settings:
- global: `otel.expoter.otlp.{x}`
- target-specific: `otel.exporter.otlp.metrics.{x}`

Global properties can be used to configure span and metric exporters simultaneously.
Global `otel.exporter.otlp.endpoint` must be a **base** URL. The configurer automatically adds path (i.e. `v1/metrics`) to the URL.

Target-specific properties are prioritized. E.g. `otel.exporter.otlp.metrics.endpoint` is prioritized over `otel.exporter.otlp.endpoint`.

| System property                        | Environment variable                           | Description                                                                                                                                                                           |
|----------------------------------------|------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| otel.exporter.otlp.endpoint            | OTEL\\_EXPORTER\\_OTLP\\_ENDPOINT              | The OTLP traces, metrics, and logs endpoint to connect to. Must be a **base** URL with a scheme of either http or https based on the use of TLS. Default is `http://localhost:4318/`. |
| otel.exporter.otlp.headers             | OTEL\\_EXPORTER\\_OTLP\\_HEADERS               | Key-value pairs separated by commas to pass as request headers on OTLP trace, metric, and log requests.                                                                               |
| otel.exporter.otlp.compression         | OTEL\\_EXPORTER\\_OTLP\\_COMPRESSION           | The compression type to use on OTLP trace, metric, and log requests. Options include gzip. By default, no compression will be used.                                                   |
| otel.exporter.otlp.timeout             | OTEL\\_EXPORTER\\_OTLP\\_TIMEOUT               | The maximum waiting time to send each OTLP trace, metric, and log batch. Default is `10 seconds`.                                                                                     |
| **Target specific:**                   |                                                |                                                                                                                                                                                       |
| otel.exporter.otlp.metrics.endpoint    | OTEL\\_EXPORTER\\_OTLP\\_METRICS\\_ENDPOINT    | The OTLP metrics endpoint to connect to. Default is `http://localhost:4318/v1/metrics`.                                                                                               |
| otel.exporter.otlp.metrics.headers     | OTEL\\_EXPORTER\\_OTLP\\_METRICS\\_HEADERS     | Key-value pairs separated by commas to pass as request headers on OTLP trace requests.                                                                                                |
| otel.exporter.otlp.metrics.compression | OTEL\\_EXPORTER\\_OTLP\\_METRICS\\_COMPRESSION | The compression type to use on OTLP trace requests. Options include gzip. By default, no compression will be used.                                                                    |
| otel.exporter.otlp.metrics.timeout     | OTEL\\_EXPORTER\\_OTLP\\_METRICS\\_TIMEOUT     | The maximum waiting time to send each OTLP trace batch. Default is `10 seconds`.                                                                                                      |

### Period metric reader

Period metric reader pushes metrics to the push-based exporters (e.g. OTLP) over a fixed interval.

| System property             | Environment variable              | Description                                                                        |
|-----------------------------|-----------------------------------|------------------------------------------------------------------------------------|
| otel.metric.export.interval | OTEL\\_METRIC\\_EXPORT\\_INTERVAL | The time interval between the start of two export attempts. Default is `1 minute`. |
| otel.metric.export.timeout  | OTEL\\_METRIC\\_EXPORT\\_TIMEOUT  | Maximum allowed time to export data. Default is `30 seconds`.                      |

### Exemplar filter

The exemplar filter decides whether the measurement will be recorded as an exemplar.

| System property              | Environment variable               | Description                                           |
|------------------------------|------------------------------------|-------------------------------------------------------|
| otel.metrics.exemplar.filter | OTEL\\_METRICS\\_EXEMPLAR\\_FILTER | The exemplar filter to use. Default is `trace_based`. |

The following options for `otel.metrics.exemplar.filter` are supported:
- `always_on` - all measurements eligible for being an exemplar.
- `always_off` - no measurements eligible for being an exemplar.
- `trace_based` - accepts measurements where there is a span in a context that is being sampled.

## Tracing

### Exporters

| System property      | Environment variable     | Description                                                                                                            |
|----------------------|--------------------------|------------------------------------------------------------------------------------------------------------------------|
| otel.traces.exporter | OTEL\\_TRACES\\_EXPORTER | List of exporters to be export spans, separated by commas. `none` means no autoconfigured exporter. Default is `otlp`. |


Options supported out of the box:

- `otlp` - requires `otel4s-sdk-exporter` dependency.
- `console` - prints the name of the span along with its attributes to stdout. It's mainly used for testing and debugging.
- `none` - means no autoconfigured exporter.

### OTLP exporter

The exporter can be configured by two sets of settings:
- global: `otel.expoter.otlp.{x}`
- target-specific: `otel.exporter.otlp.traces.{x}`

Global properties can be used to configure span and metric exporters simultaneously.
Global `otel.exporter.otlp.endpoint` must be a **base** URL. The configurer automatically adds path (i.e. `v1/traces`) to the URL.

Target-specific properties are prioritized. E.g. `otel.exporter.otlp.traces.endpoint` is prioritized over `otel.exporter.otlp.endpoint`.

| System property                       | Environment variable                          | Description                                                                                                                                                                           |
|---------------------------------------|-----------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| otel.exporter.otlp.endpoint           | OTEL\\_EXPORTER\\_OTLP\\_ENDPOINT             | The OTLP traces, metrics, and logs endpoint to connect to. Must be a **base** URL with a scheme of either http or https based on the use of TLS. Default is `http://localhost:4318/`. |
| otel.exporter.otlp.headers            | OTEL\\_EXPORTER\\_OTLP\\_HEADERS              | Key-value pairs separated by commas to pass as request headers on OTLP trace, metric, and log requests.                                                                               |
| otel.exporter.otlp.compression        | OTEL\\_EXPORTER\\_OTLP\\_COMPRESSION          | The compression type to use on OTLP trace, metric, and log requests. Options include gzip. By default, no compression will be used.                                                   |
| otel.exporter.otlp.timeout            | OTEL\\_EXPORTER\\_OTLP\\_TIMEOUT              | The maximum waiting time to send each OTLP trace, metric, and log batch. Default is `10 seconds`.                                                                                     |
| **Target specific:**                  |                                               |                                                                                                                                                                                       |
| otel.exporter.otlp.traces.endpoint    | OTEL\\_EXPORTER\\_OTLP\\_TRACES\\_ENDPOINT    | The OTLP traces endpoint to connect to. Default is `http://localhost:4318/v1/traces`.                                                                                                 |
| otel.exporter.otlp.traces.headers     | OTEL\\_EXPORTER\\_OTLP\\_TRACES\\_HEADERS     | Key-value pairs separated by commas to pass as request headers on OTLP trace requests.                                                                                                |
| otel.exporter.otlp.traces.compression | OTEL\\_EXPORTER\\_OTLP\\_TRACES\\_COMPRESSION | The compression type to use on OTLP trace requests. Options include gzip. By default, no compression will be used.                                                                    |
| otel.exporter.otlp.traces.timeout     | OTEL\\_EXPORTER\\_OTLP\\_TRACES\\_TIMEOUT     | The maximum waiting time to send each OTLP trace batch. Default is `10 seconds`.                                                                                                      |

### Propagators

The propagators determine which distributed tracing header formats are used, and which baggage propagation header formats are used.

| System property  | Environment variable | Description                                                                                                           |
|------------------|----------------------|-----------------------------------------------------------------------------------------------------------------------|
| otel.propagators | OTEL\\_PROPAGATORS   | The propagators to use. Use a comma-separated list for multiple propagators. Default is `tracecontext,baggage` (W3C). |

Options supported out of the box:
- `tracecontext` - [W3C Trace Context](https://www.w3.org/TR/trace-context/)
- `baggage` - [W3C Baggage](https://www.w3.org/TR/baggage/)
- `b3` - [B3 Single](https://github.com/openzipkin/b3-propagation#single-header)
- `b3multi` - [B3 Multi](https://github.com/openzipkin/b3-propagation#multiple-headers)
- `jaeger` - [Jaeger](https://www.jaegertracing.io/docs/1.21/client-libraries/#propagation-format)
- `ottrace` - [OpenTracing](https://opentracing.io/)

### Batch span processor

| System property                | Environment variable                     | Description                                                           |
|--------------------------------|------------------------------------------|-----------------------------------------------------------------------|
| otel.bsp.schedule.delay        | OTEL\\_BSP\\_SCHEDULE\\_DELAY            | The interval between two consecutive exports. Default is `5 seconds`. |
| otel.bsp.max.queue.size        | OTEL\\_BSP\\_MAX\\_QUEUE_SIZE            | The maximum queue size. Default is `2048`.                            |
| otel.bsp.max.export.batch.size | OTEL\\_BSP\\_MAX\\_EXPORT\\_BATCH\\_SIZE | The maximum batch size. Default is `512`.                             |
| otel.bsp.export.timeout        | OTEL\\_BSP\\_EXPORT\\_TIMEOUT            | The maximum allowed time to export data. Default is `30 seconds`.     |

### Sampler

The sampler decides whether spans will be recorded.

| System property         | Environment variable          | Description                                                              |
|-------------------------|-------------------------------|--------------------------------------------------------------------------|
| otel.traces.sampler     | OTEL\\_TRACES\\_SAMPLER       | The sampler to use for tracing. Defaults to `parentbased_always_on`.     |
| otel.traces.sampler.arg | OTEL\\_TRACES\\_SAMPLER\\_ARG | An argument to the configured tracer if supported, for example, a ratio. |


The following options for `otel.traces.sampler` are supported out of the box:
- `always_on` - always samples spans, regardless of the parent span's sampling decision.
- `always_off` - never samples spans, regardless of the parent span's sampling decision.
- `traceidratio`, where `otel.traces.sampler.arg` sets the ratio - samples probabilistically based on the configured rate.
- `parentbased_always_on` - respects its parent span's sampling decision, but otherwise always samples.
- `parentbased_always_off` - respects its parent span's sampling decision, but otherwise never samples.
- `parentbased_traceidratio`, where `otel.traces.sampler.arg` sets the ratio - respects its parent span's sampling decision, 
but otherwise samples probabilistically based on the configured rate.

### Span limits

These properties can be used to control the maximum size of spans by placing limits on attributes, events, and links.

| System property                          | Environment variable                             | Description                                                           |
|------------------------------------------|--------------------------------------------------|-----------------------------------------------------------------------|
| otel.span.attribute.count.limit          | OTEL\\_SPAN\\_ATTRIBUTE\\_COUNT\\_LIMIT          | The maximum allowed span attribute count. Default is `128`.           |
| otel.span.event.count.limit              | OTEL\\_SPAN\\_EVENT\\_COUNT\\_LIMIT              | The maximum allowed span event count. Default is `128`.               |
| otel.span.link.count.limit               | OTEL\\_SPAN\\_LINK\\_COUNT\\_LIMIT               | The maximum allowed span link count. Default is `128`.                |
| otel.event.attribute.count.limit         | OTEL\\_EVENT\\_ATTRIBUTE\\_COUNT\\_LIMIT         | The maximum allowed attribute per span event count. Default is `128`. |
| otel.link.attribute.count.limit          | OTEL\\_LINK\\_ATTRIBUTE\\_COUNT\\_LIMIT          | The maximum allowed attribute per span link count. Default is `128`.  |
| otel.span.attribute.value.length.limit   | OTEL\\_SPAN\\_ATTRIBUTE\\_VALUE\\_LENGTH\\_LIMIT | The maximum allowed attribute value size. No limit by default.        |
