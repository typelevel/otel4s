/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.instrumentation.ce

import cats.Show
import cats.effect.Resource
import cats.effect.Sync
import cats.effect.unsafe.metrics.{IORuntimeMetrics => CatsIORuntimeMetrics}
import cats.syntax.applicative._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.MeterProvider

private[ce] trait IORuntimeMetricsPlatform {
  self: IORuntimeMetrics.type =>

  sealed trait Config {

    /** The configuration of the CPU starvation metrics.
      */
    def cpuStarvation: Config.CpuStarvationConfig

    override final def toString: String =
      Show[Config].show(this)
  }

  object Config {

    sealed trait CpuStarvationConfig {

      /** Indicates whether metrics are enabled.
        */
      def enabled: Boolean

      /** The attributes to attach to the metrics.
        */
      def attributes: Attributes

      override final def toString: String =
        Show[CpuStarvationConfig].show(this)
    }

    object CpuStarvationConfig {

      /** The metrics are enabled.
        */
      def enabled: CpuStarvationConfig =
        Impl(enabled = true, Attributes.empty)

      /** The metrics are enabled and the given `attributes` will be attached.
        *
        * @param attributes
        *   the attributes to attach to the metrics
        */
      def enabled(attributes: Attributes): CpuStarvationConfig =
        Impl(enabled = true, attributes)

      /** The metrics are disabled.
        */
      def disabled: CpuStarvationConfig =
        Impl(enabled = false, Attributes.empty)

      implicit val cpuStarvationConfigShow: Show[CpuStarvationConfig] = { cfg =>
        s"CpuStarvationConfig{enabled=${cfg.enabled}, attributes=${cfg.attributes}}"
      }

      private case class Impl(enabled: Boolean, attributes: Attributes) extends CpuStarvationConfig
    }

    /** The default configuration, the following metrics are enabled:
      *   - CPU starvation
      */
    def default: Config =
      Impl(CpuStarvationConfig.enabled)

    /** A configuration with the given `cpuStarvation`.
      *
      * @param cpuStarvation
      *   the CPU starvation configuration to use
      */
    def apply(cpuStarvation: CpuStarvationConfig): Config =
      Impl(cpuStarvation)

    implicit val configShow: Show[Config] = { cfg =>
      s"IORuntimeMetrics.Config{cpuStarvation=${cfg.cpuStarvation}}"
    }

    private case class Impl(cpuStarvation: CpuStarvationConfig) extends Config
  }

  /** Registers the following collectors depending on the `config`:
    *   - CPU starvation
    *
    * @example
    *   {{{
    * object Main extends IOApp.Simple {
    *   def program(
    *     meterProvider: MeterProvider[IO],
    *     tracerProvider: TracerProvider[IO]
    *   ): IO[Unit] = ???
    *
    *   def run: IO[Unit] =
    *     OpenTelemetrySdk.autoConfigured[IO]().use { autoConfigured =>
    *       val sdk = autoConfigured.sdk
    *       implicit val mp: MeterProvider[IO] = sdk.meterProvider
    *
    *       IORuntimeMetrics
    *         .register[IO](runtime.metrics, IORuntimeMetrics.Config.default)
    *         .surround {
    *           program(sdk.meterProvider, sdk.tracerProvider)
    *         }
    *     }
    * }
    *   }}}
    *
    * =CPU starvation metrics=
    *
    * Registers the CPU starvation:
    *   - `cats.effect.runtime.cpu.starvation.count`
    *   - `cats.effect.runtime.cpu.starvation.clock.drift.current`
    *   - `cats.effect.runtime.cpu.starvation.clock.drift.max`
    *
    * To disable CPU starvation metrics, customize a config:
    * {{{
    * val config: IORuntimeMetrics.Config = {
    *   import IORuntimeMetrics.Config._
    *   IORuntimeMetrics.Config(
    *     CpuStarvationConfig.disabled // disable CPU starvation metrics
    *   )
    * }
    *
    * IORuntimeMetrics.register[IO](runtime.metrics, config)
    * }}}
    *
    * To attach attributes to CPU starvation metrics, customize a config:
    * {{{
    * val config: IORuntimeMetrics.Config = {
    *   import IORuntimeMetrics.Config._
    *   IORuntimeMetrics.Config(
    *     CpuStarvationConfig.enabled(
    *       Attributes(Attribute("key", "value")) // the attributes
    *     )
    *   )
    * }
    *
    * IORuntimeMetrics.register[IO](runtime.metrics, config)
    * }}}
    */
  def register[F[_]: Sync: MeterProvider](
      metrics: CatsIORuntimeMetrics,
      config: Config
  ): Resource[F, Unit] =
    Resource.eval(MeterProvider[F].get(Const.MeterNamespace)).flatMap { implicit meter =>
      cpuStarvationMetrics(
        metrics.cpuStarvation,
        config.cpuStarvation.attributes
      ).whenA(config.cpuStarvation.enabled)
    }

}
