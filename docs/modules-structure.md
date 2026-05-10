# Modules structure

The `otel4s` repository is designed with modularity in mind. Its published
artifacts are organized into distinct module families that serve different
roles, with `otel4s-core` and `otel4s-oteljava` as the primary entry points in
this repo.

The primary motivation behind this modular architecture is to keep the classpath small. 

## High-level modules

```mermaid
graph BT
  otel4s-oteljava --> otel4s-core
```

### 1) `otel4s-core`

Defines the interfaces: [Tracer][tracer-github],
[Meter][meter-github], and others. 
It also offers no-op implementations.

### 2) `otel4s-oteljava` 

The implementation of `otel4s-core` interfaces. Uses [OpenTelemetry Java][otel-java] under the hood.

## Related project

If you need a pure Scala backend, use the separate
[`otel4s-sdk`](https://typelevel.org/otel4s-sdk/sdk/overview.html) project.
It depends on `otel4s-core` but is released from a different repository.

## High-level module structure

Each module family has several submodules:  
1. `{x}-common` - the shared code, used by `{x}-trace` and `{x}-metrics`  
2. `{x}-trace` - the tracing-specific code  
3. `{x}-metrics` - the metrics-specific code  
4. `{x}` - the high-level module itself - aggregates all of the above  

The current structure of the modules:
```mermaid
graph BT
  otel4s-core --> otel4s-core-metrics
  otel4s-core --> otel4s-core-trace
  otel4s-core-metrics --> otel4s-core-common
  otel4s-core-trace --> otel4s-core-common

  otel4s-oteljava --> otel4s-oteljava-metrics
  otel4s-oteljava --> otel4s-oteljava-trace
  otel4s-oteljava-metrics --> otel4s-oteljava-common
  otel4s-oteljava-trace --> otel4s-oteljava-common
```

The repo also publishes supporting modules such as semantic conventions,
instrumentation helpers, and testkit artifacts.

## Which module do I need?

Let's take a look into common scenarios:

1. You develop a library, and you will use only trace-specific interfaces - use `otel4s-core-trace`  
2. You develop a library, and you will use both tracing and metrics interfaces - use `otel4s-core`  
3. You develop an app and want to export your telemetry - use `otel4s-oteljava` module  

[tracer-github]: https://github.com/typelevel/otel4s/blob/main/core/trace/src/main/scala/org/typelevel/otel4s/trace/Tracer.scala
[meter-github]: https://github.com/typelevel/otel4s/blob/main/core/metrics/src/main/scala/org/typelevel/otel4s/metrics/Meter.scala
[otel-java]: https://github.com/open-telemetry/opentelemetry-java
