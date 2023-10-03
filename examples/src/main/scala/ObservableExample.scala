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

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import org.typelevel.otel4s.java.OtelJava
import org.typelevel.otel4s.metrics.ObservableCounter

import java.lang.management.ManagementFactory
import javax.management.MBeanServer
import javax.management.ObjectName

object ObservableExample extends IOApp.Simple {

  val mbeanServer: MBeanServer = ManagementFactory.getPlatformMBeanServer
  val mbeanName = new ObjectName("cats.effect.metrics:type=CpuStarvation")

  def meterResource: Resource[IO, ObservableCounter] =
    Resource
      .eval(OtelJava.global)
      .evalMap(_.meterProvider.get("observable-example"))
      .flatMap(
        _.observableCounter("cats-effect-runtime-cpu-starvation-count")
          .withDescription("CE runtime starvation count")
          .createWithCallback(obs =>
            IO(
              mbeanServer
                .getAttribute(mbeanName, "CpuStarvationCount")
                .asInstanceOf[Long]
            ).flatMap(c => obs.record(c))
          )
      )

  def run: IO[Unit] = meterResource.useForever
}
