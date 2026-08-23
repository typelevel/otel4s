# Semantic conventions and stability

OpenTelemetry semantic conventions define shared names and meanings for telemetry produced by different libraries and
services. They cover signal-specific data such as span attributes and metrics, as well as common resource attributes.

otel4s generates Scala attribute keys and metric specs from the upstream conventions. The generated APIs reduce repeated
strings and metadata, while the upstream specification remains the source of truth for when and how each convention
applies.

## Stable and experimental conventions

OpenTelemetry develops conventions through different stability levels. otel4s publishes them in separate artifact and
package families:

- stable artifacts contain conventions that have reached stable status upstream
- experimental artifacts contain incubating conventions that may still change

The split lets applications use stable conventions without bringing incubating APIs onto the classpath. It also makes
an opt-in to an incubating convention visible in dependencies and imports.

## Binary compatibility guarantees

The stable `otel4s-semconv` and `otel4s-semconv-metrics` artifacts are part of the otel4s compatibility baseline and are
covered by the repository's binary compatibility checks.

The `otel4s-semconv-experimental` and `otel4s-semconv-metrics-experimental` artifacts have no binary compatibility
guarantee between releases. Their generated classes, members, and signatures may change when otel4s updates the
upstream incubating conventions.

When a convention becomes stable upstream, its generated API may move from an experimental package to the corresponding
stable package. Adopting the stable API can require dependency and import changes.

## Choosing a module

Use stable modules whenever they contain the convention you need.

An application can use an experimental module when it needs an incubating convention and can absorb source and binary
changes during upgrades. Library authors should avoid exposing experimental semantic-convention types in public APIs,
because those types pass the same compatibility risk to library users.

## What generated APIs enforce

Generated attributes fix the key name and Scala value type. Generated metric constructors fix the instrument name,
unit, description, and instrument kind.

They do not enforce all semantic requirements. Instrumentation code remains responsible for matters such as:

- deciding when conditionally required attributes apply
- recording required and recommended attributes
- keeping attribute values within the specified cardinality
- following the behavioral requirements in the upstream convention

## Related material

- Look up the artifacts, packages, and generated types:
  [Semantic conventions reference](../instrumentation/semantic-conventions.md)
- Add generated keys to telemetry:
  [Use semantic attributes](../how-to-semantic-conventions/use-semantic-attributes.md)
- Create instruments from generated specs:
  [Create metrics from semantic metric specs](../how-to-semantic-conventions/create-metrics-from-semantic-metric-specs.md)
- Read the source specification:
  [OpenTelemetry semantic conventions][opentelemetry-semconv]

[opentelemetry-semconv]: https://opentelemetry.io/docs/specs/semconv/
