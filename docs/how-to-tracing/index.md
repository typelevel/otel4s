# Tracing

Use this section when you want to create spans, propagate trace context, or work with Java libraries
that depend on OpenTelemetry context.

## Start here

- [Create spans around effectful code](create-spans-around-effectful-code.md)
- [Propagate trace context across service boundaries](propagate-trace-context-across-service-boundaries.md)
- [Use otel4s with Java-instrumented libraries](use-otel4s-with-java-instrumented-libraries.md)

## Related material

- To keep otel4s context in sync with OpenTelemetry Java context, follow
  [Keep otel4s context in sync with OpenTelemetry Java](../how-to-jvm-setup/keep-otel4s-context-in-sync-with-opentelemetry-java.md).
- For `Resource`, `fs2.Stream`, and other advanced tracing patterns, see the existing
  [Tracing](../instrumentation/tracing.md) page.
- For baggage, see the existing [Baggage](../instrumentation/baggage.md) page.
