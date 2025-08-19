# Zero-code | Java Agent

Zero-code instrumentation in Java works by attaching a special Java agent JAR to any Java 8+ application.
The agent dynamically modifies application bytecode at runtime to capture telemetry data—without requiring you to change your application code.

This makes it possible to automatically collect traces, metrics, and logs from many popular libraries and frameworks. Typical examples include:
- Inbound requests (e.g., HTTP servers)
- Outbound calls (e.g., HTTP clients, gRPC)
- Database access (e.g., JDBC, R2DBC)
- Message queues and other integrations

In other words, the agent gives you observability "for free" at the edges of your service, drastically reducing the need for manual instrumentation.

____

The [otel4s-opentelemetry-java][otel4s-java-agent] distribution is a variant of the official [OpenTelemetry Java agent][otel-java-agent].
It provides all the same automatic instrumentation as the upstream agent, but with two important additions:
- [Cats Effect][cats-effect] instrumentation – so fiber-based applications gain proper tracing and context propagation
- otel4s integration – keeps agent and otel4s context is sync  

You can see a working demo here: [otel4s-showcase][otel4s-showcase].

@:callout(warning)

The [otel4s-opentelemetry-java][otel4s-java-agent] agent is **experimental**.
The additional otel4s and Cats Effect instrumentation relies on non-standard techniques and may behave unpredictably in non-trivial environments.

- Please read the [Limitations](#limitations) section before using it.
- Context propagation is handled through [fiber context tracking](tracing-context-propagation.md).

@:@

## Do I need an agent?

The agent is best suited for:
- Getting started quickly – you can enable observability without writing a single line of code
- Standard service edges – HTTP, databases, gRPC, messaging, etc. are automatically instrumented

However, manual instrumentation with otel4s may be a better fit when:
- You need fine-grained control over spans, attributes, and metrics
- You want to instrument domain-specific operations (e.g., business logic)
- You're running in complex or performance-sensitive environments where bytecode manipulation could cause issues

In practice, many teams combine both approaches:
- Use the agent for broad, zero-effort coverage
- Add otel4s manual instrumentation where business-level observability is needed

## Getting started

The agent can be configured via [sbt-javaagent](https://github.com/sbt/sbt-javaagent) plugin:
```scala
lazy val service = project
  .enablePlugins(JavaAgent)                                                         // <1>
  .in(file("service"))
  .settings(
    name := "service",
    javaAgents += "io.github.irevive" % "otel4s-opentelemetry-javaagent" % "@OTEL4S_AGENT_VERSION@", // <2>
    run / fork := true,                                                             // <3>
    javaOptions += "-Dcats.effect.trackFiberContext=true",                          // <4>
    libraryDependencies ++= Seq(                                                    // <5>
      "org.typelevel"   %% "otel4s-oteljava"                           % "@VERSION@",
      "org.typelevel"   %% "otel4s-oteljava-context-storage"           % "@VERSION@",
      "io.opentelemetry" % "opentelemetry-exporter-otlp"               % "@OPEN_TELEMETRY_VERSION@" % Runtime,
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % "@OPEN_TELEMETRY_VERSION@" % Runtime
    )
  )
```

1. Enable JavaAgent sbt plugin
2. Register `otel4s-opentelemetry-javaagent` as a Java agent
3. Make sure the VM will be forked when running
4. Enable Cats Effect fiber context tracking
5. Add all necessary dependencies

## Configuration 

The agent supports the full set of [official configuration options][otel-java-agent-configuration].

In addition, you can disable or suppress specific instrumentation if certain libraries or frameworks should not be traced.
See [Disabling Instrumentation][otel-java-agent-disable] for details.

This is especially useful when:
- A library is already manually instrumented with otel4s
- Automatic instrumentation causes performance or compatibility issues
- You want to reduce telemetry volume from noisy dependencies

Configuration is typically provided via environment variables or JVM system properties, 
making it easy to manage in containerized and cloud environments.

## Application setup

The application can be configured in the following way:

```scala mdoc:silent
import cats.effect.{IO, IOApp, Resource} 
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.context.IOLocalContextStorage
import org.typelevel.otel4s.trace.Tracer

object Server extends IOApp.Simple {
  def run: IO[Unit] = {
    implicit val localProvider: LocalProvider[IO, Context] = 
      IOLocalContextStorage.localProvider[IO]                      // <1>

    for {
      otel4s <- Resource.eval(OtelJava.global[IO])                 // <2>
      meter  <- Resource.eval(otel4s.meterProvider.get("service"))
      tracer <- Resource.eval(otel4s.tracerProvider.get("service"))
      _      <- startApp(meter, tracer)
    } yield ()
  }.useForever
  
  private def startApp(meter: Meter[IO], tracer: Tracer[IO]): Resource[IO, Unit] = {
    val _ = (meter, tracer)
    Resource.unit
  }
}
```

1. `IOLocalContextStorage.localProvider` - will automatically pick up agent's context when available
2. `OtelJava.global[IO]` - you must use the **global** instance because the agent will autoconfigure it

If everything is configured correctly, you will see the following log messages:
```
[otel.javaagent 2025-07-27 09:37:18:069 +0300] [main] INFO io.opentelemetry.javaagent.tooling.VersionLogger - opentelemetry-javaagent - version: otel4s-0.0.2-otel-2.18.1
IOLocalContextStorage: agent-provided IOLocal is detected
```

## How the instrumentation works

Cats Effect has its context propagation mechanism known as [IOLocal](https://typelevel.org/cats-effect/docs/core/io-local). 
The [3.6.0](https://github.com/typelevel/cats-effect/releases/tag/v3.6.0) release provides a way to represent IOLocal as a `ThreadLocal`,
which creates an opportunity to manipulate the context from the outside.

- Agent instruments the constructor of `IORuntime` and stores a `ThreadLocal` representation of the `IOLocal[Context]` in the **bootstrap** classloader, so the agent and application both access the same instance
- Instrumentation installs a custom `ContextStorage` wrapper (for the agent context storage). This wrapper uses `FiberLocalContextHelper` to retrieve the fiber's current context (if available)
- Agent instruments `IOFiber`'s constructor and starts the fiber with the currently available context

## Limitations

For example, if you deploy two WAR files into the same Tomcat instance, 
both applications will attempt to configure the agent's shared bootstrap context, 
leading to conflicts and unpredictable behavior.

Check the https://github.com/open-telemetry/opentelemetry-java-instrumentation/pull/13576 for more information.

[otel4s-java-agent]: https://github.com/iRevive/otel4s-opentelemetry-java
[otel4s-showcase]: https://github.com/iRevive/otel4s-showcase
[otel-java-agent]: https://github.com/open-telemetry/opentelemetry-java-instrumentation
[otel-java-agent-configuration]: https://opentelemetry.io/docs/zero-code/java/agent/configuration/
[otel-java-agent-disable]: https://opentelemetry.io/docs/zero-code/java/agent/disable/
[cats-effect]: https://github.com/typelevel/cats-effect