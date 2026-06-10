# The JVM backend

`otel4s-oteljava` is the JVM backend for otel4s.
It implements the otel4s APIs on top of [OpenTelemetry Java][opentelemetry-java].

Use it when you want otel4s instrumentation to run against the OpenTelemetry Java SDK and ecosystem.

## When to use `otel4s-oteljava`

This backend is usually the right fit when you are building a JVM application and you want:

- OpenTelemetry Java exporters and SDK extensions
- interoperability with Java libraries that expect OpenTelemetry Java types or the global SDK
- integration with the OpenTelemetry Java agent or the otel4s Java agent distribution
- the `otel4s-oteljava-testkit` module for asserting exported telemetry in tests

`otel4s-oteljava` is JVM-only.
If you need a backend for Scala.js or Scala Native, use the separate
[`otel4s-sdk`](https://typelevel.org/otel4s-sdk/sdk/overview.html) project instead.

## Two ways to access the SDK

Most JVM applications use one of these entry points:

| API | Use it when | What it does |
|---|---|---|
| `OtelJava.autoConfigured` | your application is responsible for creating the SDK | creates an isolated, non-global SDK instance from OpenTelemetry Java autoconfiguration |
| `OtelJava.global` | the SDK is already configured elsewhere | reads the current process-wide OpenTelemetry instance |

The difference matters because these paths have different ownership and interoperability rules.

### `OtelJava.autoConfigured`

Use `OtelJava.autoConfigured` when your application owns SDK creation.

This is the common path when:

- your application starts the SDK itself
- you configure exporters and SDK settings with environment variables or JVM properties
- you do not need to reuse an SDK that another library, framework, or agent already created

`OtelJava.autoConfigured` creates an isolated instance.
If the same process already has a global SDK that other code relies on, creating a separate isolated instance can make
telemetry behavior harder to reason about.

For the setup steps, use
[Set up otel4s in a JVM application](../how-to-jvm-setup/set-up-otel4s-in-a-jvm-application.md).

### `OtelJava.global`

Use `OtelJava.global` when SDK creation happens elsewhere and otel4s needs to join that existing setup.

This is the common path when:

- an agent configures the global OpenTelemetry SDK
- a framework or bootstrap layer initializes OpenTelemetry before your otel4s code runs
- you need otel4s and Java libraries to observe the same process-wide SDK state

`OtelJava.global` does not configure an SDK.
It only reads the global instance that already exists.

For the setup steps, use
[Use the global OpenTelemetry instance](../how-to-jvm-setup/use-the-global-opentelemetry-instance.md).

## Related JVM modules

Some JVM-specific modules are useful only in particular situations:

- `otel4s-oteljava-context-storage` helps keep Cats Effect context and OpenTelemetry Java context aligned when both
  need to share the same tracing state
- `otel4s-oteljava-testkit` provides in-memory exporters and expectation APIs for tests

If you are using the otel4s Java agent distribution, also see:

- [Use the otel4s Java agent](../how-to-jvm-setup/use-the-otel4s-java-agent.md)
- [How otel4s context works with the otel4s Java agent](how-otel4s-context-works-with-the-otel4s-java-agent.md)

## Next steps

- Start a new JVM application setup:
  [Set up otel4s in a JVM application](../how-to-jvm-setup/set-up-otel4s-in-a-jvm-application.md)
- Reuse a process-wide SDK:
  [Use the global OpenTelemetry instance](../how-to-jvm-setup/use-the-global-opentelemetry-instance.md)
- Keep Cats Effect and OpenTelemetry Java context aligned:
  [Keep otel4s context in sync with OpenTelemetry Java](../how-to-jvm-setup/keep-otel4s-context-in-sync-with-opentelemetry-java.md)
- Test telemetry output:
  [Testkit](../oteljava/testkit.md)

[opentelemetry-java]: https://opentelemetry.io/docs/languages/java/intro/
