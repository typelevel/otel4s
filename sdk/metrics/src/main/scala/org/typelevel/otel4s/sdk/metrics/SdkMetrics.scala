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

package org.typelevel.otel4s.sdk.metrics

import cats.Applicative
import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Console
import cats.effect.std.Random
import cats.mtl.Ask
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.CommonConfigKeys
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.TelemetryResourceAutoConfigure
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.autoconfigure.MeterProviderAutoConfigure
import org.typelevel.otel4s.sdk.metrics.exemplar.TraceContextLookup
import org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter
import org.typelevel.otel4s.sdk.resource.TelemetryResourceDetector

/** The configured metrics module.
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
sealed trait SdkMetrics[F[_]] {

  /** The [[org.typelevel.otel4s.metrics.MeterProvider MeterProvider]].
    */
  def meterProvider: MeterProvider[F]
}

object SdkMetrics {

  /** Autoconfigures [[SdkMetrics]] using [[AutoConfigured.Builder]].
    *
    * @note
    *   the external components (e.g. OTLP exporter) must be registered
    *   manually. Add the `otel4s-sdk-exporter` dependency to the build file:
    *   {{{
    * libraryDependencies += "org.typelevel" %%% "otel4s-sdk-exporter" % "x.x.x"
    *   }}}
    *   and register the configurer manually:
    *   {{{
    * import org.typelevel.otel4s.sdk.metrics.SdkMetrics
    * import org.typelevel.otel4s.sdk.exporter.otlp.metrics.autoconfigure.OtlpMetricExporterAutoConfigure
    *
    * SdkMetrics.autoConfigured[IO](_.addExporterConfigurer(OtlpMetricExporterAutoConfigure[IO]))
    *   }}}
    *
    * @param customize
    *   a function for customizing the auto-configured SDK builder
    */
  def autoConfigured[F[_]: Async: Console](
      customize: AutoConfigured.Builder[F] => AutoConfigured.Builder[F] =
        (a: AutoConfigured.Builder[F]) => a
  ): Resource[F, SdkMetrics[F]] =
    customize(AutoConfigured.builder[F]).build

  def noop[F[_]: Applicative]: SdkMetrics[F] =
    new Impl(MeterProvider.noop)

  object AutoConfigured {

    type Customizer[A] = (A, Config) => A

    /** A builder of [[SdkMetrics]].
      */
    sealed trait Builder[F[_]] {

      /** Sets the given config to use when resolving properties.
        *
        * @note
        *   [[addPropertiesLoader]] and [[addPropertiesCustomizer]] will have no
        *   effect if the custom config is provided.
        *
        * @param config
        *   the config to use
        */
      def withConfig(config: Config): Builder[F]

      /** Adds the properties loader. Multiple loaders will be added. The loaded
        * properties will be merged with the default config. Loaded properties
        * take precedence over the default ones.
        *
        * @param loader
        *   the additional loader to add
        */
      def addPropertiesLoader(loader: F[Map[String, String]]): Builder[F]

      /** Adds the properties customizer. Multiple customizers can be added, and
        * they will be applied in the order they were added.
        *
        * @param customizer
        *   the customizer to add
        */
      def addPropertiesCustomizer(
          customizer: Config => Map[String, String]
      ): Builder[F]

      /** Adds the meter provider builder customizer. Multiple customizers can
        * be added, and they will be applied in the order they were added.
        *
        * @param customizer
        *   the customizer to add
        */
      def addMeterProviderCustomizer(
          customizer: Customizer[SdkMeterProvider.Builder[F]]
      ): Builder[F]

      /** Adds the telemetry resource customizer. Multiple customizers can be
        * added, and they will be applied in the order they were added.
        *
        * @param customizer
        *   the customizer to add
        */
      def addResourceCustomizer(
          customizer: Customizer[TelemetryResource]
      ): Builder[F]

      /** Adds the telemetry resource detector. Multiple detectors can be added,
        * and the detected telemetry resources will be merged.
        *
        * By default, the following detectors are enabled:
        *   - host: `host.arch`, `host.name`
        *   - os: `os.type`, `os.description`
        *
        * @param detector
        *   the detector to add
        */
      def addResourceDetector(
          detector: TelemetryResourceDetector[F]
      ): Builder[F]

      /** Adds the exporter configurer. Can be used to register exporters that
        * aren't included in the SDK.
        *
        * @example
        *   Add the `otel4s-sdk-exporter` dependency to the build file:
        *   {{{
        * libraryDependencies += "org.typelevel" %%% "otel4s-sdk-exporter" % "x.x.x"
        *   }}}
        *   and register the configurer manually:
        *   {{{
        * import org.typelevel.otel4s.sdk.metrics.SdkMetrics
        * import org.typelevel.otel4s.sdk.exporter.otlp.metric.autoconfigure.OtlpMetricExporterAutoConfigure
        *
        * SdkMetrics.autoConfigured[IO](_.addExporterConfigurer(OtlpMetricExporterAutoConfigure[IO]))
        *   }}}
        *
        * @param configurer
        *   the configurer to add
        */
      def addExporterConfigurer(
          configurer: AutoConfigure.Named[F, MetricExporter[F]]
      ): Builder[F]

      /** Creates [[SdkMetrics]] using the configuration of this builder.
        */
      def build: Resource[F, SdkMetrics[F]]
    }

    /** Creates a [[Builder]].
      */
    def builder[F[_]: Async: Console]: Builder[F] =
      BuilderImpl(
        customConfig = None,
        propertiesLoader = Async[F].pure(Map.empty),
        propertiesCustomizers = Nil,
        resourceCustomizer = (a, _) => a,
        meterProviderCustomizer = (a: SdkMeterProvider.Builder[F], _) => a,
        resourceDetectors = Set.empty,
        exporterConfigurers = Set.empty
      )

    private final case class BuilderImpl[F[_]: Async: Console](
        customConfig: Option[Config],
        propertiesLoader: F[Map[String, String]],
        propertiesCustomizers: List[Config => Map[String, String]],
        resourceCustomizer: Customizer[TelemetryResource],
        meterProviderCustomizer: Customizer[SdkMeterProvider.Builder[F]],
        resourceDetectors: Set[TelemetryResourceDetector[F]],
        exporterConfigurers: Set[AutoConfigure.Named[F, MetricExporter[F]]]
    ) extends Builder[F] {

      def withConfig(config: Config): Builder[F] =
        copy(customConfig = Some(config))

      def addPropertiesLoader(
          loader: F[Map[String, String]]
      ): Builder[F] =
        copy(propertiesLoader = (this.propertiesLoader, loader).mapN(_ ++ _))

      def addPropertiesCustomizer(
          customizer: Config => Map[String, String]
      ): Builder[F] =
        copy(propertiesCustomizers = this.propertiesCustomizers :+ customizer)

      def addResourceCustomizer(
          customizer: Customizer[TelemetryResource]
      ): Builder[F] =
        copy(resourceCustomizer = merge(this.resourceCustomizer, customizer))

      def addMeterProviderCustomizer(
          customizer: Customizer[SdkMeterProvider.Builder[F]]
      ): Builder[F] =
        copy(meterProviderCustomizer =
          merge(this.meterProviderCustomizer, customizer)
        )

      def addResourceDetector(
          detector: TelemetryResourceDetector[F]
      ): Builder[F] =
        copy(resourceDetectors = this.resourceDetectors + detector)

      def addExporterConfigurer(
          configurer: AutoConfigure.Named[F, MetricExporter[F]]
      ): Builder[F] =
        copy(exporterConfigurers = this.exporterConfigurers + configurer)

      def build: Resource[F, SdkMetrics[F]] = {
        def loadConfig: F[Config] =
          for {
            props <- propertiesLoader
            config <- Config.load(props)
          } yield propertiesCustomizers.foldLeft(config)((cfg, c) =>
            cfg.withOverrides(c(cfg))
          )

        def loadNoop: Resource[F, SdkMetrics[F]] =
          Resource.eval(
            Console[F]
              .println(
                s"SdkMetrics: the '${CommonConfigKeys.SdkDisabled}' set to 'true'. Using no-op implementation"
              )
              .as(SdkMetrics.noop[F])
          )

        def loadMetrics(
            config: Config,
            resource: TelemetryResource
        ): Resource[F, SdkMetrics[F]] =
          Resource.eval(Random.scalaUtilRandom).flatMap { implicit random =>
            implicit val askContext: Ask[F, Context] = Ask.const(Context.root)

            val meterProviderConfigure =
              MeterProviderAutoConfigure[F](
                resource,
                TraceContextLookup.noop,
                meterProviderCustomizer,
                exporterConfigurers
              )

            for {
              meterProvider <- meterProviderConfigure.configure(config)
            } yield new Impl[F](meterProvider)
          }

        for {
          config <- Resource.eval(customConfig.fold(loadConfig)(Async[F].pure))

          resource <- TelemetryResourceAutoConfigure[F](resourceDetectors)
            .configure(config)
            .map(resourceCustomizer(_, config))

          isDisabled <- Resource.eval(
            Async[F].fromEither(
              config.getOrElse(CommonConfigKeys.SdkDisabled, false)
            )
          )

          metrics <- if (isDisabled) loadNoop else loadMetrics(config, resource)
        } yield metrics
      }

      private def merge[A](
          first: Customizer[A],
          second: Customizer[A]
      ): Customizer[A] =
        (a, config) => second(first(a, config), config)

    }
  }

  private final class Impl[F[_]](
      val meterProvider: MeterProvider[F]
  ) extends SdkMetrics[F] {
    override def toString: String =
      s"SdkMetrics{meterProvider=$meterProvider}"
  }

}
