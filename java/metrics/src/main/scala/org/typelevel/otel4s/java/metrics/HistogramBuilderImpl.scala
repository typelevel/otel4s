/*
 * Copyright 2022 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.otel4s
package java
package metrics

import cats.effect.kernel.Sync
import cats.effect.std.Dispatcher
import io.opentelemetry.api.metrics.{Meter => JMeter}
import org.typelevel.otel4s.metrics._

private[java] case class HistogramBuilderImpl[F[_]](
    jMeter: JMeter,
    name: String,
    unit: Option[String] = None,
    description: Option[String] = None
)(implicit F: Sync[F])
    extends SyncInstrumentBuilder[F, Histogram[F, Double]] {
  type Self = HistogramBuilderImpl[F]

  def withUnit(unit: String): Self = copy(unit = Option(unit))

  def withDescription(description: String): Self =
    copy(description = Option(description))

  def create: F[Histogram[F, Double]] = F.delay {
    val b = jMeter.histogramBuilder(name)
    unit.foreach(b.setUnit)
    description.foreach(b.setDescription)
    jMeter.counterBuilder().buildWithCallback()
    new HistogramImpl(b.build)

    jMeter.gaugeBuilder().buildWithCallback(m => m.record())
  }

  Dispatcher.sequential()

  import _root_.java.lang.management.ManagementFactory
  import javax.management.{MBeanServer, ObjectName}

  val server: MBeanServer = ManagementFactory.getPlatformMBeanServer
  val mbeanName = new ObjectName("cats.effect.metrics:type=CpuStarvation")

  server.getAttribute(mbeanName, "CpuStarvationCount").asInstanceOf[Long]

  def collect(): java.util.List[Collector.MetricFamilySamples] =
    java.util.Arrays.asList(
      createGauge("cats_effect_cpu_starvation_count", "", "CpuStarvationCount"),
      createGauge("cats_effect_max_clock_drift_ms", "", "MaxClockDriftMs"),
      createGauge("cats_effect_current_clock_drift_ms", "", "CurrentClockDriftMs"),
    )

  private def createGauge(metric: String, help: String, attributeName: String): GaugeMetricFamily = {
    val metricFamily = new GaugeMetricFamily(metric, help, Collections.emptyList[String]())
    val value = server.getAttribute(mbeanName, attributeName).asInstanceOf[Long]
    metricFamily.addMetric(Collections.emptyList[String](), value.toDouble)
  }

  cats.effect.unsafe.IORuntime.global.scheduler
}
