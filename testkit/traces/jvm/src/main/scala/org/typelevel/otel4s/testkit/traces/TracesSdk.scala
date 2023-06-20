package org.typelevel.otel4s

package testkit
package metrics

import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import cats.effect.kernel.Sync
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import cats.effect.IOLocal
import org.typelevel.vault.Vault
import org.typelevel.otel4s.trace.TracerProvider
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.{
  ContextPropagators => JContextPropagators
}

import scala.jdk.CollectionConverters._

object TracesSdk {
  object TracerSuite {

    class Sdk[F[_]: Sync](
      val provider: TracerProvider[F],
      exporter: InMemorySpanExporter
    ) {

      def finishedSpans: F[List[SpanData]] =
        Sync[F].delay(exporter.getFinishedSpanItems.asScala.toList)
    }
  }

  def create[F[_]: Sync](
      customize: SdkTracerProviderBuilder => SdkTracerProviderBuilder = identity
  ): F[TracerSuite.Sdk[F]] = {
    val exporter = InMemorySpanExporter.create()

    val builder = SdkTracerProvider
      .builder()
      .addSpanProcessor(SimpleSpanProcessor.create(exporter))

    val tracerProvider: SdkTracerProvider =
      customize(builder).build()

    IOLocal(Vault.empty).map { implicit ioLocal: IOLocal[Vault] =>
      val propagators = new ContextPropagatorsImpl[F](
        JContextPropagators.create(W3CTraceContextPropagator.getInstance()),
        ContextConversions.toJContext,
        ContextConversions.fromJContext
      )

      val provider = TracerProviderImpl.local[F](tracerProvider, propagators)
      new TracerSuite.Sdk[F](provider, exporter)
    }
  }
}