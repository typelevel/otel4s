package org.typelevel.otel4s.sdk.exporter.otlp

sealed trait HttpPayloadEncoding

object HttpPayloadEncoding {
  case object Json extends HttpPayloadEncoding
  case object Protobuf extends HttpPayloadEncoding
}
