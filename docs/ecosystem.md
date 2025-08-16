# Ecosystem

| Project                                                | Description                                                                                                            |
|--------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| [otel4s-experimental][otel4s-experimental]             | An experimental functionality: instrumented `cats.effect.std.Queue`, runtime metrics, `@span` annotation, and so on.   |
| [otel4s-opentelemetry-java][otel4s-opentelemetry-java] | An experimental distribution of the OpenTelemetry Java agent that includes instrumentation for Cats Effect and otel4s. |
| [http4s-otel4s-middleware][http4s-otel4s-middleware]   | [Http4s][http4s] middleware for seamless integration of tracing and metrics.                                           |
| [otel4s-doobie][otel4s-doobie]                         | [Doobie][doobie] integration with Otel4s providing traces of database queries.                                         |
| [sttp][sttp]                                           | sttp 4.x provides [metered][sttp-metrics] and [traced][sttp-tracing] backends.                                         |
| [tapir][tapir]                                         | Tapir supports [server traces][tapir-tracing] via otel4s.                                                              |

[otel4s-experimental]: https://github.com/typelevel/otel4s-experimental
[otel4s-opentelemetry-java]: https://github.com/iRevive/otel4s-opentelemetry-java 
[http4s-otel4s-middleware]: https://github.com/http4s/http4s-otel4s-middleware
[http4s]: https://github.com/http4s/http4s
[otel4s-doobie]: https://github.com/arturaz/otel4s-doobie
[doobie]: https://github.com/tpolecat/doobie
[sttp]: https://github.com/softwaremill/sttp
[sttp-metrics]: https://sttp.softwaremill.com/en/latest/backends/wrappers/opentelemetry.html#metrics-cats-effect-otel4s
[sttp-tracing]: https://sttp.softwaremill.com/en/latest/backends/wrappers/opentelemetry.html#tracing-cats-effect-otel4s
[tapir]: https://github.com/softwaremill/tapir
[tapir-tracing]: https://tapir.softwaremill.com/en/latest/server/observability.html#otel4s-opentelemetry-tracing
