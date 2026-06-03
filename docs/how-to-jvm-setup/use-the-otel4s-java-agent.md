# Use the otel4s Java agent

Use this page when you want automatic OpenTelemetry instrumentation and also want otel4s to use the same global SDK in the same JVM application.

This page covers `otel4s-opentelemetry-javaagent`, a custom distribution of the upstream OpenTelemetry Java agent. The shared-context setup below applies to this distribution, not to the standard OpenTelemetry Java agent.

For background on why this setup differs from the standard agent path, see
[How otel4s context works with the otel4s Java agent](../explanations/how-otel4s-context-works-with-the-otel4s-java-agent.md).

@:callout(warning)

`otel4s-opentelemetry-javaagent` is experimental.

@:@

## 1. Add the agent and runtime dependencies

The example below uses the [sbt-javaagent](https://github.com/sbt/sbt-javaagent) plugin to attach the agent when you run the application from sbt.

```scala
lazy val service = project
  .enablePlugins(JavaAgent) // <1>
  .in(file("service"))
  .settings(
    name := "service",
    javaAgents += "io.github.irevive" % "otel4s-opentelemetry-javaagent" % "@OTEL4S_AGENT_VERSION@", // <2>
    run / fork := true, // <3>
    javaOptions += "-Dcats.effect.trackFiberContext=true", // <4>
    libraryDependencies ++= Seq( // <5>
      "org.typelevel"   %% "otel4s-oteljava"                           % "@VERSION@",
      "org.typelevel"   %% "otel4s-oteljava-context-storage"           % "@VERSION@",
      "io.opentelemetry" % "opentelemetry-exporter-otlp"               % "@OPEN_TELEMETRY_VERSION@" % Runtime,
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % "@OPEN_TELEMETRY_VERSION@" % Runtime
    )
  )
```

1. Enable the Java agent plugin
2. Attach `otel4s-opentelemetry-javaagent`
3. Run the application in a forked JVM
4. Enable Cats Effect fiber context tracking
5. Add otel4s and OpenTelemetry runtime dependencies

`otel4s-oteljava-context-storage` and `-Dcats.effect.trackFiberContext=true` are required for the shared-context path shown on this page.

## 2. Configure the agent

The agent configures the global OpenTelemetry SDK from environment variables or system properties.

For a minimal OTLP setup, configure at least:

@:select(config-source)

@:choice(env-vars)

```bash
export OTEL_SERVICE_NAME=auth-service
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
```

@:choice(jvm-properties)

```bash
-Dotel.service.name=auth-service
-Dotel.exporter.otlp.endpoint=http://localhost:4317
```

@:@

Add more agent configuration only when needed.
For example, you can disable certain instrumentation:

@:select(config-source)

@:choice(env-vars)

```bash
export OTEL_INSTRUMENTATION_JDBC_ENABLED=false
```

@:choice(jvm-properties)

```bash
-Dotel.instrumentation.jdbc.enabled=false
```

@:@

For the full configuration surface, see the [OpenTelemetry Java agent configuration docs][otel-java-agent-configuration] and [disabling instrumentation docs][otel-java-agent-disable].

## 3. Read the global SDK from otel4s

The agent autoconfigures the global OpenTelemetry SDK.
Use `OtelJava.global[IO]`, not `OtelJava.autoConfigured[IO]()`.

```scala mdoc:silent
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.context.IOLocalContextStorage
import org.typelevel.otel4s.trace.TracerProvider

object Main extends IOApp.Simple {
  implicit val localProvider: LocalProvider[IO, Context] =
    IOLocalContextStorage.localProvider[IO]

  def run: IO[Unit] =
    OtelJava.global[IO].flatMap { otel4s =>
      program(otel4s.meterProvider, otel4s.tracerProvider)
    }

  def program(
      meterProvider: MeterProvider[IO],
      tracerProvider: TracerProvider[IO]
  ): IO[Unit] =
    for {
      meter  <- meterProvider.get("auth-service")
      tracer <- tracerProvider.get("auth-service")

      counter <- meter.counter[Long]("service.requests").create
      _       <- counter.inc()

      _ <- tracer.span("startup").surround(IO.unit)
    } yield ()
}
```

With that setup:

- the agent owns SDK configuration
- `otel4s` reads the global SDK instance
- `IOLocalContextStorage` keeps Cats Effect and agent context aligned for this agent distribution

## 4. Verify that the setup works

Run the application and confirm all of the following:

- the agent starts successfully
- your OpenTelemetry backend receives spans or metrics from the service
- manual otel4s instrumentation uses the same global SDK instance

Typical startup logs include lines like:

```text
[otel.javaagent ...] INFO io.opentelemetry.javaagent.tooling.VersionLogger - opentelemetry-javaagent - version: otel4s-...
IOLocalContextStorage: agent-provided IOLocal is detected
```

If you add your own spans with otel4s, they should appear in the same telemetry pipeline as the agent-provided instrumentation.

## 5. Check whether this setup fits your deployment

- This agent distribution is experimental.
- It relies on shared JVM bootstrap state, so it is best suited to one application per JVM.
- Multi-application containers, such as two WAR files in one Tomcat instance, can conflict with that setup.

For background on that tradeoff, see
[How otel4s context works with the otel4s Java agent](../explanations/how-otel4s-context-works-with-the-otel4s-java-agent.md)
and the related upstream discussion in
[open-telemetry/opentelemetry-java-instrumentation#13576][otel-java-agent-pr].

## What's next

- Reuse a global SDK that was configured elsewhere:
  [Use the global OpenTelemetry instance](use-the-global-opentelemetry-instance.md)
- Background on how this agent aligns otel4s and Java context:
  [How otel4s context works with the otel4s Java agent](../explanations/how-otel4s-context-works-with-the-otel4s-java-agent.md)
- Work with Java libraries that expect OpenTelemetry Java context:
  [Use otel4s with Java-instrumented libraries](../how-to-tracing/use-otel4s-with-java-instrumented-libraries.md)
- Create spans in your own code on top of agent-provided instrumentation:
  [Create spans around effectful code](../how-to-tracing/create-spans-around-effectful-code.md)
- For a working example, see:
  [otel4s-showcase](https://github.com/iRevive/otel4s-showcase)

[otel-java-agent-configuration]: https://opentelemetry.io/docs/zero-code/java/agent/configuration/
[otel-java-agent-disable]: https://opentelemetry.io/docs/zero-code/java/agent/disable/
[otel-java-agent-pr]: https://github.com/open-telemetry/opentelemetry-java-instrumentation/pull/13576
