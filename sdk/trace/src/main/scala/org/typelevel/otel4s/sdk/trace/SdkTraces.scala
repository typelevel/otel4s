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
import cats.effect.std.Random
import cats.mtl.Local
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.CommonConfigKeys
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.TelemetryResourceAutoConfigure
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.LocalContext
import org.typelevel.otel4s.sdk.context.LocalContextProvider
import org.typelevel.otel4s.sdk.trace.autoconfigure.BatchSpanProcessorAutoConfigure
import org.typelevel.otel4s.sdk.trace.autoconfigure.ContextPropagatorsAutoConfigure
import org.typelevel.otel4s.sdk.trace.autoconfigure.SamplerAutoConfigure
import org.typelevel.otel4s.sdk.trace.autoconfigure.SpanExportersAutoConfigure
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter
import org.typelevel.otel4s.sdk.trace.processor.SimpleSpanProcessor
import org.typelevel.otel4s.sdk.trace.processor.SpanProcessor
import org.typelevel.otel4s.sdk.trace.samplers.Sampler
import org.typelevel.otel4s.trace.TracerProvider

trait SdkTraces[F[_]] {

  /** The [[org.typelevel.otel4s.trace.TracerProvider TracerProvider]].
    */
  def tracerProvider: TracerProvider[F]

  /** The propagators used by the
    * [[org.typelevel.otel4s.trace.TracerProvider TracerProvider]].
    */
  def propagators: ContextPropagators[Context]

  /** The [[TelemetryResource]] the
    * [[org.typelevel.otel4s.trace.TracerProvider TracerProvider]] links spans
    * to.
    */
  def resource: TelemetryResource

  /** The [[org.typelevel.otel4s.sdk.context.LocalContext LocalContext]] used by
    * the [[org.typelevel.otel4s.trace.TracerProvider TracerProvider]].
    */
  def localContext: LocalContext[F]
}

object SdkTraces {

  /** Autoconfigures [[SdkTraces]] using [[AutoConfigured.Builder]].
    *
    * @note
    *   the external components (e.g. OTLP exporter) must be registered
    *   manually. Add the `otel4s-sdk-exporter` dependency to the sbt file:
    *   {{{
    * libraryDependencies += "org.typelevel" %%% "otel4s-sdk-exporter" % "x.x.x"
    *   }}}
    *   and register the configurer manually:
    *   {{{
    * import org.typelevel.otel4s.sdk.trace.Traces
    * import org.typelevel.otel4s.sdk.exporter.otlp.trace.autoconfigure.OtlpSpanExporterAutoConfigure
    *
    * SdkTraces.autoConfigured[IO](_.addExporterConfigurer(OtlpSpanExporterAutoConfigure[IO]))
    *   }}}
    * @param customize
    *   a function for customizing the auto-configured SDK builder
    */
  def autoConfigured[F[_]: Async: Parallel: Console: LocalContextProvider](
      customize: AutoConfigured.Builder[F] => AutoConfigured.Builder[F] =
        (a: AutoConfigured.Builder[F]) => a
  ): Resource[F, SdkTraces[F]] =
    customize(AutoConfigured.builder[F]).build

  def noop[F[_]: Applicative: LocalContext]: SdkTraces[F] =
    new Impl(
      TracerProvider.noop,
      ContextPropagators.noop,
      TelemetryResource.empty,
      Local[F, Context]
    )

  object AutoConfigured {

    type Customizer[A] = (A, Config) => A

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

      def addPropertiesLoader(loader: F[Map[String, String]]): Builder[F]

      def addPropertiesCustomizer(
          customizer: Config => Map[String, String]
      ): Builder[F]

      def addTracerProviderCustomizer(
          customizer: Customizer[SdkTracerProvider.Builder[F]]
      ): Builder[F]

      def addResourceCustomizer(
          customizer: Customizer[TelemetryResource]
      ): Builder[F]

      def addExporterConfigurer(
          configurer: AutoConfigure.Named[F, SpanExporter[F]]
      ): Builder[F]

      def addSamplerConfigurer(
          configurer: AutoConfigure.Named[F, Sampler]
      ): Builder[F]

      def addTextMapPropagatorConfigurer(
          configurer: AutoConfigure.Named[F, TextMapPropagator[Context]]
      ): Builder[F]

      def build: Resource[F, SdkTraces[F]]
    }

    def builder[
        F[_]: Async: Parallel: Console: LocalContextProvider
    ]: Builder[F] =
      BuilderImpl(
        customConfig = None,
        propertiesLoader = Async[F].pure(Map.empty),
        propertiesCustomizers = Nil,
        resourceCustomizer = (a, _) => a,
        tracerProviderCustomizer = (a: SdkTracerProvider.Builder[F], _) => a,
        exporterConfigurers = Set.empty,
        samplerConfigurers = Set.empty,
        textMapPropagatorConfigurers = Set.empty
      )

    private[sdk] def configure[
        F[_]: Async: Parallel: Console: LocalContextProvider
    ](
        config: Config,
        resource: TelemetryResource,
        tracerProviderCustomizer: Customizer[SdkTracerProvider.Builder[F]],
        samplerConfigurers: Set[AutoConfigure.Named[F, Sampler]],
        exporterConfigurers: Set[AutoConfigure.Named[F, SpanExporter[F]]],
        textMapPropagatorConfigurers: Set[
          AutoConfigure.Named[F, TextMapPropagator[Context]]
        ]
    ): Resource[F, SdkTraces[F]] = {

      def configureProcessors(
          exporters: Map[String, SpanExporter[F]]
      ): Resource[F, List[SpanProcessor[F]]] = {
        val logging = exporters.get("logging") match {
          case Some(logging) => List(SimpleSpanProcessor(logging))
          case None          => Nil
        }

        val others = exporters.removed("logging")
        if (others.nonEmpty) {
          val exporter = others.values.toList.combineAll
          BatchSpanProcessorAutoConfigure[F](exporter)
            .configure(config)
            .map(processor => logging :+ processor)
        } else {
          Resource.pure(logging)
        }
      }

      Resource.eval(LocalProvider[F, Context].local).flatMap { implicit local =>
        Resource.eval(Random.scalaUtilRandom[F]).flatMap { implicit random =>
          val samplerConfigure =
            SamplerAutoConfigure[F](samplerConfigurers)

          val exporterAutoConfigure =
            SpanExportersAutoConfigure[F](exporterConfigurers)

          val propagatorsAutoConfigure =
            ContextPropagatorsAutoConfigure[F](
              textMapPropagatorConfigurers
            )

          for {
            sampler <- samplerConfigure.configure(config)
            exporters <- exporterAutoConfigure.configure(config)
            processors <- configureProcessors(exporters)

            tpBuilder = {
              val builder = SdkTracerProvider
                .builder[F]
                .withResource(resource)
                .withSampler(sampler)

              processors.foldLeft(builder)(_.addSpanProcessor(_))
            }

            tracerProvider <- Resource.eval(
              tracerProviderCustomizer(tpBuilder, config).build
            )

            propagators <- propagatorsAutoConfigure.configure(config)
          } yield new Impl[F](tracerProvider, propagators, resource, local)
        }
      }
    }

    private final case class BuilderImpl[
        F[_]: Async: Parallel: Console: LocalContextProvider
    ](
        customConfig: Option[Config],
        propertiesLoader: F[Map[String, String]],
        propertiesCustomizers: List[Config => Map[String, String]],
        resourceCustomizer: Customizer[TelemetryResource],
        tracerProviderCustomizer: Customizer[SdkTracerProvider.Builder[F]],
        exporterConfigurers: Set[AutoConfigure.Named[F, SpanExporter[F]]],
        samplerConfigurers: Set[AutoConfigure.Named[F, Sampler]],
        textMapPropagatorConfigurers: Set[
          AutoConfigure.Named[F, TextMapPropagator[Context]]
        ]
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

      def addTracerProviderCustomizer(
          customizer: Customizer[SdkTracerProvider.Builder[F]]
      ): Builder[F] =
        copy(tracerProviderCustomizer =
          merge(this.tracerProviderCustomizer, customizer)
        )

      def addExporterConfigurer(
          configurer: AutoConfigure.Named[F, SpanExporter[F]]
      ): Builder[F] =
        copy(exporterConfigurers = exporterConfigurers + configurer)

      def addSamplerConfigurer(
          configurer: AutoConfigure.Named[F, Sampler]
      ): Builder[F] =
        copy(samplerConfigurers = samplerConfigurers + configurer)

      def addTextMapPropagatorConfigurer(
          configurer: AutoConfigure.Named[F, TextMapPropagator[Context]]
      ): Builder[F] =
        copy(textMapPropagatorConfigurers =
          textMapPropagatorConfigurers + configurer
        )

      def build: Resource[F, SdkTraces[F]] = {
        def loadConfig: F[Config] =
          for {
            props <- propertiesLoader
            config <- Config.load(props)
          } yield propertiesCustomizers.foldLeft(config)((cfg, c) =>
            cfg.withOverrides(c(cfg))
          )

        def loadNoop: Resource[F, SdkTraces[F]] =
          Resource.eval(
            for {
              local <- LocalProvider[F, Context].local
            } yield SdkTraces.noop[F](Async[F], local)
          )

        def loadTraces(
            config: Config,
            resource: TelemetryResource
        ): Resource[F, SdkTraces[F]] =
          configure(
            config,
            resource,
            tracerProviderCustomizer,
            samplerConfigurers,
            exporterConfigurers,
            textMapPropagatorConfigurers
          )

        for {
          config <- Resource.eval(customConfig.fold(loadConfig)(Async[F].pure))
          resource <- TelemetryResourceAutoConfigure[F].configure(config)

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
      val resource: TelemetryResource,
      val localContext: LocalContext[F]
  ) extends SdkTraces[F] {
    override def toString: String =
      s"SdkTraces{tracerProvider=$tracerProvider,propagators=$propagators,resource=$resource}"
  }

}
