package com.rossabaker.otel4s
package oteljava

import cats.effect.Sync

import io.opentelemetry.api.{OpenTelemetry => JOpenTelemetry}
import io.opentelemetry.api.common.{Attributes => JAttributes}
import io.opentelemetry.api.common.{AttributeKey => JAttributeKey}
import io.opentelemetry.api.common.{AttributeType => JAttributeType}
import io.opentelemetry.api.metrics.{Meter => JMeter}
import io.opentelemetry.api.metrics.{LongCounter => JLongCounter}

import java.util.{List => JList}

object OtelJava {
  def forSync[F[_]](jOtel: JOpenTelemetry)(implicit F: Sync[F]): Otel4s[F] =
    new Otel4s[F] {
      def meterProvider: MeterProvider[F] = new MeterProviderImpl[F](jOtel)

      def stringKey(name0: String): AttributeKey[String] =
        new AttributeKey[String] with JAttributeKey[String] {
          def name = name0
          def getKey = name0
          def `type` = AttributeType.String
          def getType = JAttributeType.STRING
        }
      def booleanKey(name0: String): AttributeKey[Boolean] =
        new AttributeKey[Boolean] with JAttributeKey[Boolean] {
          def name = name0
          def getKey = name0
          def `type` = AttributeType.Boolean
          def getType = JAttributeType.BOOLEAN
        }
      def longKey(name0: String): AttributeKey[Long] =
        new AttributeKey[Long] with JAttributeKey[Long] {
          def name = name0
          def getKey = name0
          def `type` = AttributeType.Long
          def getType = JAttributeType.LONG
        }
      def doubleKey(name0: String): AttributeKey[Double] =
        new AttributeKey[Double] with JAttributeKey[Double] {
          def name = name0
          def getKey = name0
          def `type` = AttributeType.Double
          def getType = JAttributeType.DOUBLE
        }
      def stringListKey(name0: String): AttributeKey[List[String]] =
        new AttributeKey[List[String]] {
          def name = name0
          def `type` = AttributeType.StringList
        }
      def longListKey(name0: String): AttributeKey[List[Long]] =
        new AttributeKey[List[Long]] {
          def name = name0
          def `type` = AttributeType.LongList
        }
    }

  private class MeterProviderImpl[F[_]: Sync](jOtel: JOpenTelemetry) extends MeterProvider[F] {
     def meter(name: String): MeterBuilder[F] = new MeterBuilderImpl(jOtel, name)
  }

  private case class MeterBuilderImpl[F[_]](jOtel: JOpenTelemetry, name: String, version: Option[String] = None, schemaUrl: Option[String] = None)(implicit F: Sync[F]) extends MeterBuilder[F] {
    def withVersion(version: String) = copy(version = Option(version))
    def withSchemaUrl(schemaUrl: String) = copy(schemaUrl = Option(schemaUrl))

    def get: F[Meter[F]] = F.delay {
      val b = jOtel.meterBuilder(name)
      version.foreach(b.setInstrumentationVersion)
      schemaUrl.foreach(b.setSchemaUrl)
      new MeterImpl(b.build())
    }
  }

  private class MeterImpl[F[_]: Sync](jMeter: JMeter) extends Meter[F] {
    def counter(name: String): SyncInstrumentBuilder[F, Counter[F, Long]] =
      new CounterBuilderImpl(jMeter, name)
  }

  private case class CounterBuilderImpl[F[_]](jMeter: JMeter, name: String, unit: Option[String] = None, description: Option[String] = None)(implicit F: Sync[F]) extends SyncInstrumentBuilder[F, Counter[F, Long]] {
    type Self = CounterBuilderImpl[F]

    def withUnit(unit: String) = copy(unit = Option(unit))
    def withDescription(description: String) = copy(description = Option(description))

    def create: F[Counter[F, Long]] = F.delay {
      val b = jMeter.counterBuilder(name)
      unit.foreach(b.setUnit)
      description.foreach(b.setDescription)
      new CounterImpl(b.build)
    }
  }

  private class CounterImpl[F[_]](longCounter: JLongCounter)(implicit F: Sync[F]) extends Counter[F, Long] {
    def add(long: Long, attributes: Attribute[_]*) =
      F.delay(longCounter.add(long, toJAttributes(attributes)))
  }

  private def toJAttributes(attributes: Seq[Attribute[_]]): JAttributes = {
    val builder = JAttributes.builder
    attributes.foreach {
      // Handle preallocated keys as an optimal case
      case Attribute(key: JAttributeKey[_], value) =>
        val rawKey = key.asInstanceOf[JAttributeKey[Any]]
        builder.put(rawKey, value)

      // The primitives require no conversion
      case Attribute(key, value: Boolean) =>
        builder.put(key.name, value)
      case Attribute(key, value: Long) =>
        builder.put(key.name, value)
      case Attribute(key, value: Double) =>
        builder.put(key.name, value)
      case Attribute(key, value: String) =>
        builder.put(key.name, value)


      // The lists need a conversion from Scala to Java
      case Attribute(key, values: Seq[_]) if key.`type` == AttributeType.BooleanList =>
        val jKey = JAttributeKey.booleanArrayKey(key.name).asInstanceOf[JAttributeKey[JList[Any]]]
        builder.put(jKey, values: _*)
      case Attribute(key, values: Seq[_]) if key.`type` == AttributeType.LongList =>
        val jKey = JAttributeKey.longArrayKey(key.name).asInstanceOf[JAttributeKey[JList[Any]]]
        builder.put(jKey, values: _*)
      case Attribute(key, values: Seq[_]) if key.`type` == AttributeType.DoubleList =>
        val jKey = JAttributeKey.doubleArrayKey(key.name).asInstanceOf[JAttributeKey[JList[Any]]]
        builder.put(jKey, values: _*)
      case Attribute(key, values: Seq[_]) if key.`type` == AttributeType.StringList =>
        val jKey = JAttributeKey.stringArrayKey(key.name).asInstanceOf[JAttributeKey[JList[Any]]]
        builder.put(jKey, values: _*)
    }
    builder.build()
  }
}
