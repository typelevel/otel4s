# Metrics

Use this section when you want to record application metrics or expose runtime metrics with otel4s.

## Start here

- Record counters, histograms, and observable measurements in application code:
  [Record application metrics](record-application-metrics.md)
- Run code that requires a meter without recording or exporting measurements:
  [Provide a no-op meter](provide-a-no-op-meter.md)
- Create instruments with metadata from OpenTelemetry semantic conventions:
  [Create metrics from semantic metric specs](../how-to-semantic-conventions/create-metrics-from-semantic-metric-specs.md)
- Customize histogram buckets for a specific metric:
  [Customize histogram buckets](customize-histogram-buckets.md)
- Export Cats Effect runtime metrics from your application:
  [Register Cats Effect runtime metrics](register-cats-effect-runtime-metrics.md)
- Export JVM runtime metrics such as memory, threads, and GC activity:
  [Register JVM runtime metrics](register-jvm-runtime-metrics.md)
