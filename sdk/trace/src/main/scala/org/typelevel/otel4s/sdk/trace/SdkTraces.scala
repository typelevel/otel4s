/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk.trace

import cats.Applicative
import cats.Parallel
import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Console
import cats.effect.std.Env
import cats.effect.std.Random
import cats.effect.std.SystemProperties
import cats.mtl.Local
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.CommonConfigKeys
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.TelemetryResourceAutoConfigure
import org.typelevel.otel4s.sdk.common.Diagnostic
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.LocalContext
import org.typelevel.otel4s.sdk.context.LocalContextProvider
import org.typelevel.otel4s.sdk.resource.TelemetryResourceDetector
import org.typelevel.otel4s.sdk.trace.autoconfigure.ContextPropagatorsAutoConfigure
import org.typelevel.otel4s.sdk.trace.autoconfigure.TracerProviderAutoConfigure
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter
import org.typelevel.otel4s.sdk.trace.samplers.Sampler
import org.typelevel.otel4s.trace.TracerProvider

/** The configured tracing module.
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
sealed trait SdkTraces[F[_]] {

  /** The [[org.typelevel.otel4s.trace.TracerProvider TracerProvider]].
    */
  def tracerProvider: TracerProvider[F]

  /** The propagators used by the [[org.typelevel.otel4s.trace.TracerProvider TracerProvider]].
    */
  def propagators: ContextPropagators[Context]

  /** The [[org.typelevel.otel4s.sdk.context.LocalContext LocalContext]] used by the
    * [[org.typelevel.otel4s.trace.TracerProvider TracerProvider]].
    */
  def localContext: LocalContext[F]
}

object SdkTraces {

  /** Autoconfigures [[SdkTraces]] using [[AutoConfigured.Builder]].
    *
    * @note
    *   the external components (e.g. OTLP exporter) must be registered manually. Add the `otel4s-sdk-exporter`
    *   dependency to the build file:
    *   {{{
    * libraryDependencies += "org.typelevel" %%% "otel4s-sdk-exporter" % "x.x.x"
    *   }}}
    *   and register the configurer manually:
    *   {{{
    * import org.typelevel.otel4s.sdk.trace.SdkTraces
    * import org.typelevel.otel4s.sdk.exporter.otlp.trace.autoconfigure.OtlpSpanExporterAutoConfigure
    *
    * SdkTraces.autoConfigured[IO](_.addExporterConfigurer(OtlpSpanExporterAutoConfigure[IO]))
    *   }}}
    *
    * @param customize
    *   a function for customizing the auto-configured SDK builder
    */
  def autoConfigured[F[_]: Async: Parallel: Env: SystemProperties: Console: Diagnostic: LocalContextProvider](
      customize: AutoConfigured.Builder[F] => AutoConfigured.Builder[F] = (a: AutoConfigured.Builder[F]) => a
  ): Resource[F, SdkTraces[F]] =
    customize(AutoConfigured.builder[F]).build

  def noop[F[_]: Applicative: LocalContext]: SdkTraces[F] =
    new Impl(
      TracerProvider.noop,
      ContextPropagators.noop,
      Local[F, Context]
    )

  object AutoConfigured {

    type Customizer[A] = (A, Config) => A

    /** A builder of [[SdkTraces]].
      */
    sealed trait Builder[F[_]] {

      /** Sets the given config to use when resolving properties.
        *
        * @note
        *   [[addPropertiesLoader]] and [[addPropertiesCustomizer]] will have no effect if the custom config is
        *   provided.
        *
        * @param config
        *   the config to use
        */
      def withConfig(config: Config): Builder[F]

      /** Adds the properties loader. Multiple loaders will be added. The loaded properties will be merged with the
        * default config. Loaded properties take precedence over the default ones.
        *
        * @param loader
        *   the additional loader to add
        */
      def addPropertiesLoader(loader: F[Map[String, String]]): Builder[F]

      /** Adds the properties customizer. Multiple customizers can be added, and they will be applied in the order they
        * were added.
        *
        * @param customizer
        *   the customizer to add
        */
      def addPropertiesCustomizer(customizer: Config => Map[String, String]): Builder[F]

      /** Adds the tracer provider builder customizer. Multiple customizers can be added, and they will be applied in
        * the order they were added.
        *
        * @param customizer
        *   the customizer to add
        */
      def addTracerProviderCustomizer(customizer: Customizer[SdkTracerProvider.Builder[F]]): Builder[F]

      /** Adds the telemetry resource customizer. Multiple customizers can be added, and they will be applied in the
        * order they were added.
        *
        * @param customizer
        *   the customizer to add
        */
      def addResourceCustomizer(customizer: Customizer[TelemetryResource]): Builder[F]

      /** Adds the telemetry resource detector. Multiple detectors can be added, and the detected telemetry resources
        * will be merged.
        *
        * By default, the following detectors are enabled:
        *   - host: `host.arch`, `host.name`
        *   - os: `os.type`, `os.description`
        *   - process: `process.command`, `process.command_args`, `process.command_line`, `process.executable.name`,
        *     `process.executable.path`, `process.pid`, `process.owner`
        *   - process_runtime: `process.runtime.name`, `process.runtime.version`, `process.runtime.description`
        *
        * @param detector
        *   the detector to add
        */
      def addResourceDetector(detector: TelemetryResourceDetector[F]): Builder[F]

      /** Adds the exporter configurer. Can be used to register exporters that aren't included in the SDK.
        *
        * @example
        *   Add the `otel4s-sdk-exporter` dependency to the build file:
        *   {{{
        * libraryDependencies += "org.typelevel" %%% "otel4s-sdk-exporter" % "x.x.x"
        *   }}}
        *   and register the configurer manually:
        *   {{{
        * import org.typelevel.otel4s.sdk.trace.SdkTraces
        * import org.typelevel.otel4s.sdk.exporter.otlp.trace.autoconfigure.OtlpSpanExporterAutoConfigure
        *
        * SdkTraces.autoConfigured[IO](_.addExporterConfigurer(OtlpSpanExporterAutoConfigure[IO]))
        *   }}}
        *
        * @param configurer
        *   the configurer to add
        */
      def addExporterConfigurer(configurer: AutoConfigure.Named[F, SpanExporter[F]]): Builder[F]

      /** Adds the sampler configurer. Can be used to register samplers that aren't included in the SDK.
        *
        * @param configurer
        *   the configurer to add
        */
      def addSamplerConfigurer(configurer: AutoConfigure.Named[F, Sampler[F]]): Builder[F]

      /** Adds the text map propagator configurer. Can be used to register propagators that aren't included in the SDK.
        *
        * @param configurer
        *   the configurer to add
        */
      def addTextMapPropagatorConfigurer(configurer: AutoConfigure.Named[F, TextMapPropagator[Context]]): Builder[F]

      /** Creates [[SdkTraces]] using the configuration of this builder.
        */
      def build: Resource[F, SdkTraces[F]]
    }

    /** Creates a [[Builder]].
      */
    def builder[F[_]: Async: Parallel: Env: SystemProperties: Console: Diagnostic: LocalContextProvider]: Builder[F] =
      BuilderImpl(
        customConfig = None,
        propertiesLoader = Async[F].pure(Map.empty),
        propertiesCustomizers = Nil,
        resourceCustomizer = (a, _) => a,
        tracerProviderCustomizer = (a: SdkTracerProvider.Builder[F], _) => a,
        resourceDetectors = Set.empty,
        exporterConfigurers = Set.empty,
        samplerConfigurers = Set.empty,
        textMapPropagatorConfigurers = Set.empty
      )

    private final case class BuilderImpl[
        F[_]: Async: Parallel: Env: SystemProperties: Console: Diagnostic: LocalContextProvider
    ](
        customConfig: Option[Config],
        propertiesLoader: F[Map[String, String]],
        propertiesCustomizers: List[Config => Map[String, String]],
        resourceCustomizer: Customizer[TelemetryResource],
        tracerProviderCustomizer: Customizer[SdkTracerProvider.Builder[F]],
        resourceDetectors: Set[TelemetryResourceDetector[F]],
        exporterConfigurers: Set[AutoConfigure.Named[F, SpanExporter[F]]],
        samplerConfigurers: Set[AutoConfigure.Named[F, Sampler[F]]],
        textMapPropagatorConfigurers: Set[AutoConfigure.Named[F, TextMapPropagator[Context]]]
    ) extends Builder[F] {

      def withConfig(config: Config): Builder[F] =
        copy(customConfig = Some(config))

      def addPropertiesLoader(loader: F[Map[String, String]]): Builder[F] =
        copy(propertiesLoader = (this.propertiesLoader, loader).mapN(_ ++ _))

      def addPropertiesCustomizer(customizer: Config => Map[String, String]): Builder[F] =
        copy(propertiesCustomizers = this.propertiesCustomizers :+ customizer)

      def addResourceCustomizer(customizer: Customizer[TelemetryResource]): Builder[F] =
        copy(resourceCustomizer = merge(this.resourceCustomizer, customizer))

      def addResourceDetector(detector: TelemetryResourceDetector[F]): Builder[F] =
        copy(resourceDetectors = this.resourceDetectors + detector)

      def addTracerProviderCustomizer(customizer: Customizer[SdkTracerProvider.Builder[F]]): Builder[F] =
        copy(tracerProviderCustomizer = merge(this.tracerProviderCustomizer, customizer))

      def addExporterConfigurer(configurer: AutoConfigure.Named[F, SpanExporter[F]]): Builder[F] =
        copy(exporterConfigurers = this.exporterConfigurers + configurer)

      def addSamplerConfigurer(configurer: AutoConfigure.Named[F, Sampler[F]]): Builder[F] =
        copy(samplerConfigurers = this.samplerConfigurers + configurer)

      def addTextMapPropagatorConfigurer(configurer: AutoConfigure.Named[F, TextMapPropagator[Context]]): Builder[F] =
        copy(textMapPropagatorConfigurers = this.textMapPropagatorConfigurers + configurer)

      def build: Resource[F, SdkTraces[F]] = {
        def loadConfig: F[Config] =
          for {
            props <- propertiesLoader
            config <- Config.load(props)
          } yield propertiesCustomizers.foldLeft(config)((cfg, c) => cfg.withOverrides(c(cfg)))

        def loadNoop: Resource[F, SdkTraces[F]] =
          Resource.eval(
            for {
              _ <- Diagnostic[F].info(
                s"SdkTraces: the '${CommonConfigKeys.SdkDisabled}' set to 'true'. Using no-op implementation"
              )
              local <- LocalProvider[F, Context].local
            } yield SdkTraces.noop[F](Async[F], local)
          )

        def loadTraces(
            config: Config,
            resource: TelemetryResource
        ): Resource[F, SdkTraces[F]] = {
          def makeLocalContext = LocalProvider[F, Context].local

          Resource.eval(makeLocalContext).flatMap { implicit local =>
            Resource.eval(Random.scalaUtilRandom).flatMap { implicit random =>
              val propagatorsConfigure = ContextPropagatorsAutoConfigure[F](
                textMapPropagatorConfigurers
              )

              propagatorsConfigure.configure(config).flatMap { propagators =>
                val tracerProviderConfigure =
                  TracerProviderAutoConfigure[F](
                    resource,
                    propagators,
                    tracerProviderCustomizer,
                    samplerConfigurers,
                    exporterConfigurers
                  )

                for {
                  tracerProvider <- tracerProviderConfigure.configure(config)
                } yield new Impl[F](tracerProvider, propagators, local)
              }
            }
          }
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

          traces <- if (isDisabled) loadNoop else loadTraces(config, resource)
        } yield traces
      }

      private def merge[A](
          first: Customizer[A],
          second: Customizer[A]
      ): Customizer[A] =
        (a, config) => second(first(a, config), config)

    }
  }

  private final class Impl[F[_]](
      val tracerProvider: TracerProvider[F],
      val propagators: ContextPropagators[Context],
      val localContext: LocalContext[F]
  ) extends SdkTraces[F] {
    override def toString: String =
      s"SdkTraces{tracerProvider=$tracerProvider, propagators=$propagators}"
  }

}
