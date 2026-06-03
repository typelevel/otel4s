# How otel4s context works with the otel4s Java agent

Use [Use the otel4s Java agent](../how-to-jvm-setup/use-the-otel4s-java-agent.md) for the setup steps.
Use [Keep otel4s context in sync with OpenTelemetry Java](../how-to-jvm-setup/keep-otel4s-context-in-sync-with-opentelemetry-java.md) for the non-agent setup path.

This page explains how `otel4s-opentelemetry-javaagent` works with otel4s context propagation and why some deployment shapes are risky.
`otel4s-opentelemetry-javaagent` is a custom distribution of the upstream OpenTelemetry Java agent: it keeps the same automatic instrumentation behavior and also adds Cats Effect and otel4s integrations.

## The standard Java agent and otel4s solve different context problems

OpenTelemetry Java and otel4s both track the current trace context, but they do not use the same mechanism by default.

- OpenTelemetry Java expects context to be available through `ThreadLocal`-based storage
- otel4s uses effect-local state, exposed through `Local` and backed by Cats Effect context propagation on JVM

That difference matters whenever Java code and otel4s code both need to observe the same current span.

Examples:

- a Java framework starts the current trace and otel4s code should continue it
- otel4s creates a span and a Java library expects `Context.current()`
- automatic agent instrumentation and manual otel4s instrumentation should appear in one trace

Without extra integration, those paths do not automatically share context.

## Why `otel4s-opentelemetry-javaagent` exists

The standard OpenTelemetry Java agent already configures the global SDK and instruments many service boundaries.
That solves SDK bootstrap and automatic tracing, but it does not by itself make Cats Effect fiber-local context and Java `ThreadLocal` context behave as one system.

This custom distribution exists to bridge that gap for JVM applications that use Cats Effect and otel4s.

At a high level, it combines:

- the usual Java agent startup and automatic instrumentation
- global SDK access through `OtelJava.global`
- Cats Effect context tracking enabled with `-Dcats.effect.trackFiberContext=true`
- `otel4s-oteljava-context-storage` so otel4s can participate in the same context story

That is why the how-to for this agent uses `OtelJava.global[IO]` rather than `OtelJava.autoConfigured[IO]()`: the agent already owns SDK creation.

## Where `IOLocal` and `ThreadLocal` meet

Cats Effect propagates request-scoped state across fibers.
OpenTelemetry Java expects request-scoped state to be readable from the current thread.

The agent-specific integration works by making those two views line up closely enough for mixed Java and otel4s code to see the same current context during normal application execution.
In practice, that means otel4s can keep using Cats Effect-local context while Java libraries and agent instrumentation can still read the current context through the usual OpenTelemetry Java APIs.
The important result is not that the two mechanisms become identical.
It is that otel4s code and Java code can agree on the current tracing context in the supported setup.

## Why the agent how-to requires `OtelJava.global`

With the agent path, there is already one process-wide SDK instance.

If application code also calls `OtelJava.autoConfigured[IO]()`, it creates a second SDK instance that is isolated from the one managed by the agent.
That leads to split telemetry pipelines and broken assumptions about shared state.

So the agent path uses:

- `OtelJava.global[IO]` to read the SDK the agent already configured
- `IOLocalContextStorage.localProvider[IO]` to align otel4s context with the agent-specific integration path

This is why the Java agent how-to and the plain JVM setup how-to are separate tasks.

## Why multi-application containers are risky

The agent integration relies on shared JVM-level state to connect Java context handling and Cats Effect context tracking.

That is workable for the common case of one application in one JVM.
It becomes much harder to reason about when multiple applications share the same JVM process, such as:

- two WAR files in one Tomcat instance
- other container setups where unrelated applications share bootstrap-level state

In those environments, applications can interfere with one another's assumptions about context setup.
That is why the agent how-to keeps the limitation explicit instead of treating it as an edge case.

## When to use this path

Use `otel4s-opentelemetry-javaagent` when:

- you want automatic instrumentation at common service boundaries
- you still want to write manual otel4s spans or metrics in application code
- the application runs in a deployment model close to one app per JVM

Prefer the regular JVM setup pages when:

- your application owns SDK bootstrap directly
- you do not need this agent-specific context-sharing path
- you want the simpler `OtelJava.autoConfigured[IO]().use { otel4s => ... }` setup

## Related material

- Setup steps for the agent path:
  [Use the otel4s Java agent](../how-to-jvm-setup/use-the-otel4s-java-agent.md)
- Context sync without the agent-specific setup:
  [Keep otel4s context in sync with OpenTelemetry Java](../how-to-jvm-setup/keep-otel4s-context-in-sync-with-opentelemetry-java.md)
- Mixed Java and otel4s tracing boundaries:
  [Use otel4s with Java-instrumented libraries](../how-to-tracing/use-otel4s-with-java-instrumented-libraries.md)
