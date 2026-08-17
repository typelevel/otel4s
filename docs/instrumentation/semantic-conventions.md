# Semantic conventions reference

The semantic-convention modules contain generated attribute keys and metric specs based on the
[OpenTelemetry semantic conventions][opentelemetry-semconv].

For the stable and experimental module model, including binary compatibility guarantees, see
[Semantic conventions and stability](../explanations/semantic-conventions-and-stability.md).

## Artifacts

| Artifact | Stability | Package prefix | Contents |
|:--|:--|:--|:--|
| `otel4s-semconv` | Stable | `org.typelevel.otel4s.semconv.attributes` | Generated stable attribute keys and values |
| `otel4s-semconv-metrics` | Stable | `org.typelevel.otel4s.semconv.metrics` | Generated stable metric specs and constructors |
| `otel4s-semconv-experimental` | Experimental | `org.typelevel.otel4s.semconv.experimental.attributes` | Generated incubating attribute keys and values |
| `otel4s-semconv-metrics-experimental` | Experimental | `org.typelevel.otel4s.semconv.experimental.metrics` | Generated incubating metric specs and constructors |

All four artifacts are available for JVM, Scala.js, and Scala Native.

The two stable artifacts are covered by the otel4s binary compatibility policy. The two `*-experimental` artifacts
provide no binary compatibility guarantee between releases.

## Generated attributes

Attribute modules group `AttributeKey[A]` values by semantic domain. For example,
`org.typelevel.otel4s.semconv.attributes.HttpAttributes.HttpRequestMethod` is an `AttributeKey[String]` with the name
`http.request.method`.

Some modules also contain generated value types for attributes with a defined set of values.

For task guidance, see [Use semantic attributes](../how-to-semantic-conventions/use-semantic-attributes.md).

## `MetricSpec`

Each generated metric object implements `MetricSpec` and exposes:

| Member | Type | Description |
|:--|:--|:--|
| `name` | `String` | Canonical metric name |
| `description` | `String` | Canonical metric description |
| `unit` | `String` | UCUM metric unit |
| `stability` | `Stability` | Stability recorded in the source convention |
| `attributeSpecs` | `List[AttributeSpec[_]]` | Attributes defined for the metric |

Each enclosing domain object also exposes `specs`, which contains all metric specs generated for that domain.

## Generated metric constructors

Each generated metric object has a `create` method that uses the implicit `Meter[F]` to create the corresponding
instrument with the spec's name, description, and unit.

Counter, gauge, and up-down-counter constructors take the effect and measurement types. Histogram constructors also
take `BucketBoundaries`.

For task guidance, see
[Create metrics from semantic metric specs](../how-to-semantic-conventions/create-metrics-from-semantic-metric-specs.md).

## `AttributeSpec`

Each entry in `MetricSpec.attributeSpecs` exposes:

| Member | Type | Description |
|:--|:--|:--|
| `key` | `AttributeKey[A]` | Generated semantic attribute key |
| `examples` | `List[A]` | Example values from the source convention |
| `requirement` | `Requirement` | Requirement level and optional note |
| `stability` | `Stability` | Attribute stability recorded in the source convention |

## `Requirement`

`Requirement.level` is one of:

| Level | Meaning |
|:--|:--|
| `Required` | The metric implementation must record the attribute |
| `ConditionalRequired` | The implementation must record the attribute when the accompanying condition applies |
| `Recommended` | The implementation should record the attribute when applicable |
| `OptIn` | The attribute is available when users explicitly enable or request it |

`Requirement.note` contains the condition or additional guidance when the source convention provides one.

## `Stability`

The generated `Stability` values are `stable`, `development`, `releaseCandidate`, `alpha`, and `beta`.

## Generation

The source files under `semconv` are generated. Run `sbt semanticConventionsGenerate` to regenerate them from the
configured OpenTelemetry semantic-convention source.

## Related material

- [Semantic conventions and stability](../explanations/semantic-conventions-and-stability.md)
- [Use semantic attributes](../how-to-semantic-conventions/use-semantic-attributes.md)
- [Create metrics from semantic metric specs](../how-to-semantic-conventions/create-metrics-from-semantic-metric-specs.md)
- [Test metrics against semantic conventions](../how-to-testkit/test-metrics-against-semantic-conventions.md)

[opentelemetry-semconv]: https://opentelemetry.io/docs/specs/semconv/
