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

object TracesSdk {
  def create[F[_]: Sync](
      customize: SdkTracerProviderBuilder => SdkTracerProviderBuilder = identity
  ): F[TracerSuite.Sdk] = {
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
      new TracerSuite.Sdk(provider, exporter)
    }
  }
}
