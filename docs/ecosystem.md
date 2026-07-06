# Ecosystem

| Project                                                | Description                                                                                                            |
|--------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| [otel4s-experimental][otel4s-experimental]             | An experimental functionality: instrumented `cats.effect.std.Queue`, runtime metrics, `@span` annotation, and so on.   |
| [otel4s-opentelemetry-java][otel4s-opentelemetry-java] | An experimental distribution of the OpenTelemetry Java agent that includes instrumentation for Cats Effect and otel4s. |
| [http4s-otel4s-middleware][http4s-otel4s-middleware]   | [Http4s][http4s] middleware for seamless integration of tracing and metrics.                                           |
| [otel4s-doobie][otel4s-doobie]                         | [Doobie][doobie] integration with Otel4s providing traces of database queries (with support for HikariCP).             |
| [doobie][doobie]                                       | The [doobie-otel4s][doobie-otel4s] module provides traces of database queries .                                        |
| [sttp][sttp]                                           | sttp 4.x provides [metered][sttp-metrics] and [traced][sttp-tracing] backends.                                         |
| [tapir][tapir]                                         | Tapir supports [server traces][tapir-tracing] via otel4s.                                                              |
| [fs2-queues-otel4s][fs2-queues-otel4s]                 | [fs2-queues][fs2-queues] integration with otel4s providing traces and metrics for messaging systems.                   |
| [fs2-grpc][fs2-grpc]                                   | The [fs2-grpc-otel4s-trace][fs2-grpc-otel4s-trace] module provides server and client tracing capability.               |
| [fs2-kafka-otel4s][fs2-kafka-otel4s]                   | The [fs2-kafka-otel4s][fs2-kafka-otel4s] project provides consumer and producer traces for [fs2-kafka][fs2-kafka].     |

[otel4s-experimental]: https://github.com/typelevel/otel4s-experimental
[otel4s-opentelemetry-java]: https://github.com/iRevive/otel4s-opentelemetry-java 
[http4s-otel4s-middleware]: https://github.com/http4s/http4s-otel4s-middleware
[http4s]: https://github.com/http4s/http4s
[otel4s-doobie]: https://github.com/arturaz/otel4s-doobie
[doobie]: https://github.com/typelevel/doobie
[doobie-otel4s]: https://typelevel.org/doobie/docs/20-Otel4s-Tracing.html
[sttp]: https://github.com/softwaremill/sttp
[sttp-metrics]: https://sttp.softwaremill.com/en/latest/backends/wrappers/opentelemetry.html#metrics-cats-effect-otel4s
[sttp-tracing]: https://sttp.softwaremill.com/en/latest/backends/wrappers/opentelemetry.html#tracing-cats-effect-otel4s
[tapir]: https://github.com/softwaremill/tapir
[tapir-tracing]: https://tapir.softwaremill.com/en/latest/server/observability.html#otel4s-opentelemetry-tracing
[fs2-queues-otel4s]: https://commercetools.github.io/fs2-queues/integrations/otel4s/
[fs2-queues]: https://commercetools.github.io/fs2-queues/
[fs2-grpc]: https://github.com/typelevel/fs2-grpc
[fs2-grpc-otel4s-trace]: https://github.com/typelevel/fs2-grpc#opentelemetry-tracing-with-otel4s
[fs2-kafka]: https://github.com/typelevel/fs2-kafka
[fs2-kafka-otel4s]: https://github.com/iRevive/fs2-kafka-otel4s
