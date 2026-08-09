# Tracing

Use this section when you want to create spans, propagate trace context, or work with Java libraries
that depend on OpenTelemetry context.

## Start here

- [Create spans around effectful code](create-spans-around-effectful-code.md)
- [Use unmanaged spans when a span must end outside its scope](use-unmanaged-spans-when-a-span-must-end-outside-its-scope.md)
- [Trace Resource and fs2.Stream code](trace-resource-and-fs2-stream-code.md)
- [Work with baggage](work-with-baggage.md)
- [Propagate trace context across service boundaries](propagate-trace-context-across-service-boundaries.md)
- [Use otel4s with Java-instrumented libraries](use-otel4s-with-java-instrumented-libraries.md)
- [Use otel4s with Pekko HTTP instrumentation](use-otel4s-with-pekko-http-instrumentation.md)

## Related material

- For how `span`, `childScope`, `withParent`, `joinOrRoot`, `rootScope`, `rootSpan`, and `noopScope` affect
  parent-child relationships, see
  [Choosing parent spans and tracing scopes](../explanations/choosing-parent-spans-and-tracing-scopes.md).
- When your application creates the OpenTelemetry SDK, keep otel4s context in sync with OpenTelemetry Java context:
  [Keep otel4s context in sync with OpenTelemetry Java][keep-context-in-sync].
- When the standard OpenTelemetry Java agent or a framework owns the current Java context, use
  [explicit context bridging at Java boundaries](use-otel4s-with-java-instrumented-libraries.md).
- For the mental model behind `Local`, fiber-local context, and explicit scope re-entry, see
  [How otel4s context propagation works](../explanations/how-otel4s-context-propagation-works.md).
- For more background on `Resource` and `fs2.Stream` tracing scopes, see
  [Tracing Resource and fs2.Stream scopes](../explanations/tracing-resource-and-fs2-stream-scopes.md).
- For the core tracing interfaces, see the
  [Tracing API reference](../instrumentation/tracing.md).

[keep-context-in-sync]: ../how-to-jvm-setup/keep-otel4s-context-in-sync-with-opentelemetry-java.md
