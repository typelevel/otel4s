# Tracing

Use this section when you want to create spans, propagate trace context, or work with Java libraries
that depend on OpenTelemetry context.

## Start here

- [Create spans around effectful code](create-spans-around-effectful-code.md)
- [Trace Resource and fs2.Stream code](trace-resource-and-fs2-stream-code.md)
- [Propagate trace context across service boundaries](propagate-trace-context-across-service-boundaries.md)
- [Use otel4s with Java-instrumented libraries](use-otel4s-with-java-instrumented-libraries.md)

## Related material

- For how `span`, `childScope`, `rootScope`, `rootSpan`, and `noopScope` affect parent-child relationships, see
  [Root spans and tracing scopes](../explanations/root-spans-and-tracing-scopes.md).
- To keep otel4s context in sync with OpenTelemetry Java context, follow
  [Keep otel4s context in sync with OpenTelemetry Java](../how-to-jvm-setup/keep-otel4s-context-in-sync-with-opentelemetry-java.md).
- For more background on `Resource` and `fs2.Stream` tracing scopes, see
  [Tracing Resource and fs2.Stream scopes](../explanations/tracing-resource-and-fs2-stream-scopes.md).
- For unmanaged spans and other lower-level tracing APIs, see the existing
  [Tracing](../instrumentation/tracing.md) page.
- For baggage, see the existing [Baggage](../instrumentation/baggage.md) page.
