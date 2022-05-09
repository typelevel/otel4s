package com.rossabaker.otel4s
package oteljava

import cats.effect.Sync

import io.opentelemetry.api.{OpenTelemetry => JOpenTelemetry}
import io.opentelemetry.api.common.{Attributes => JAttributes}
import io.opentelemetry.api.common.{AttributeKey => JAttributeKey}
import io.opentelemetry.api.metrics.{Meter => JMeter}
import io.opentelemetry.api.metrics.{LongCounter => JLongCounter}

import scala.jdk.CollectionConverters._

class OtelJava[F[_]](jOtel: JOpenTelemetry)(implicit F: Sync[F]) {

  private case class MeterBuilderImpl(name: String, version: Option[String] = None, schemaUrl: Option[String] = None) extends MeterBuilder[F] {
    def withVersion(version: String) = copy(version = Option(version))
    def withSchemaUrl(schemaUrl: String) = copy(schemaUrl = Option(schemaUrl))

    def get: F[Meter[F]] = F.delay {
      val b = jOtel.meterBuilder(name)
      version.foreach(b.setInstrumentationVersion)
      schemaUrl.foreach(b.setSchemaUrl)
      new MeterImpl(b.build())
    }
  }

  private class MeterImpl(jMeter: JMeter) extends Meter[F] {
    def counter(name: String): SyncInstrumentBuilder[F, Counter[F, Long]] =
      new CounterBuilderImpl(jMeter, name)
  }

  private case class CounterBuilderImpl(jMeter: JMeter, name: String, unit: Option[String] = None, description: Option[String] = None) extends SyncInstrumentBuilder[F, Counter[F, Long]] {
    type Self = CounterBuilderImpl

    def withUnit(unit: String) = copy(unit = Option(unit))
    def withDescription(description: String) = copy(description = Option(description))

    def create: F[Counter[F, Long]] = F.delay {
      val b = jMeter.counterBuilder(name)
      unit.foreach(b.setUnit)
      description.foreach(b.setDescription)
      new CounterImpl(b.build)
    }
  }

  private class CounterImpl(longCounter: JLongCounter) extends Counter[F, Long] {
    def add(long: Long, attributes: Attributes) =
      F.delay(longCounter.add(long, toJAttributes(attributes)))
  }

  private def toJAttributes(attributes: Attributes): JAttributes = new JAttributes {
    def asMap = Map.empty.asJava
    def forEach(f: java.util.function.BiConsumer[_ >: io.opentelemetry.api.common.AttributeKey[_], _ >: Object]) = ()
    def get[T](key: JAttributeKey[T]): T = null.asInstanceOf[T]
    def isEmpty = attributes.isEmpty
    def size = attributes.size
    def toBuilder = JAttributes.builder()
  }

  def meterProvider: MeterProvider[F] = new MeterProvider[F] {
    def meter(name: String): MeterBuilder[F] = MeterBuilderImpl(name)
  }
}
