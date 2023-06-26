package org.typelevel.otel4s

package testkit
package traces

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

import io.opentelemetry.sdk.OpenTelemetrySdk

import scala.jdk.CollectionConverters._

trait TracesSdk[F[_]] {
  def sdk: OpenTelemetrySdk
  def traces: F[List[Trace]]
}

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
  ): TracesSdk[F] = {
    new TracesSdk[F] {

      val exporter = InMemorySpanExporter.create()

      val tracerProviderBuilder = SdkTracerProvider
        .builder()
        .addSpanProcessor(SimpleSpanProcessor.create(exporter))

        val traceProvider = customize(tracerProviderBuilder).build()

        val openTelemetrySdk = OpenTelemetrySdk
          .builder()
          .setTracerProvider(traceProvider)
          .build()

      val sdk: OpenTelemetrySdk = openTelemetrySdk

      val traces: F[List[Trace]] = ???
    }
  }

  // def create[F[_]: Sync](
  //     customize: SdkTracerProviderBuilder => SdkTracerProviderBuilder = identity
  // ): F[TracerSuite.Sdk[F]] = {
  //   val exporter = InMemorySpanExporter.create()

  //   val builder = SdkTracerProvider
  //     .builder()
  //     .addSpanProcessor(SimpleSpanProcessor.create(exporter))

  //   val tracerProvider: SdkTracerProvider =
  //     customize(builder).build()

  //   IOLocal(Vault.empty).map { implicit ioLocal: IOLocal[Vault] =>
  //     val propagators = new ContextPropagatorsImpl[F](
  //       JContextPropagators.create(W3CTraceContextPropagator.getInstance()),
  //       ContextConversions.toJContext,
  //       ContextConversions.fromJContext
  //     )

  //     val provider = TracerProviderImpl.local[F](tracerProvider, propagators)
  //     new TracerSuite.Sdk[F](provider, exporter)
  //   }
  // }
}
