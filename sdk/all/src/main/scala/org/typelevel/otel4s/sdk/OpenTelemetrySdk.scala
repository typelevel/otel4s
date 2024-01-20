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

package org.typelevel.otel4s.sdk

import cats.Applicative
import cats.Parallel
import cats.effect.Async
import cats.effect.MonadCancelThrow
import cats.effect.Resource
import cats.effect.std.Console
import cats.effect.std.Random
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.Otel4s
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.LocalContextProvider
import org.typelevel.otel4s.sdk.trace.SdkTracerProvider
import org.typelevel.otel4s.sdk.trace.autoconfigure.PropagatorsConfiguration
import org.typelevel.otel4s.sdk.trace.autoconfigure.TracerProviderAutoConfigure
import org.typelevel.otel4s.trace.TracerProvider

sealed class OpenTelemetrySdk[F[_]] private (
    val propagators: ContextPropagators[Context],
    val meterProvider: MeterProvider[F],
    val tracerProvider: TracerProvider[F]
) extends Otel4s[F] {
  type Ctx = Context

  override def toString: String =
    s"OpenTelemetrySdk{tracerProvider=$tracerProvider, meterProvider=$meterProvider, propagators=$propagators}"
}

object OpenTelemetrySdk {

  def apply[F[_]](
      propagators: ContextPropagators[Context],
      meterProvider: MeterProvider[F],
      tracerProvider: TracerProvider[F]
  ): OpenTelemetrySdk[F] =
    new OpenTelemetrySdk[F](propagators, meterProvider, tracerProvider)

  def noop[F[_]: Applicative]: OpenTelemetrySdk[F] =
    new OpenTelemetrySdk[F](
      ContextPropagators.noop,
      MeterProvider.noop,
      TracerProvider.noop
    )

  /** @see
    *   [[https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md]]
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    */
  sealed trait AutoConfigured[F[_]] {

    /** The auto-configured OpenTelemetry SDK.
      * @return
      */
    def sdk: OpenTelemetrySdk[F]

    /** The resource that was auto-configured.
      */
    def resource: TelemetryResource

    /** The config used for auto-configuration.
      */
    def config: Config
  }

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

      def addTracerProviderCustomizer(
          customizer: Customizer[SdkTracerProvider.Builder[F]]
      ): Builder[F]

      def addResourceCustomizer(
          customizer: Customizer[TelemetryResource]
      ): Builder[F]

      def addPropertiesLoader(loader: F[Map[String, String]]): Builder[F]

      def addPropertiesCustomizer(
          customizer: Config => Map[String, String]
      ): Builder[F]

      def build: Resource[F, AutoConfigured[F]]
    }

    def load[
        F[_]: Async: Parallel: LocalContextProvider
    ]: Resource[F, AutoConfigured[F]] =
      builder[F].build

    def builder[F[_]: Async: Parallel: LocalContextProvider]: Builder[F] =
      BuilderImpl(
        customConfig = None,
        tracerProviderCustomizer = (a: SdkTracerProvider.Builder[F], _) => a,
        resourceCustomizer = (a, _) => a,
        propertiesLoader = Async[F].pure(Map.empty),
        propertiesCustomizers = Nil
      )

    private final case class BuilderImpl[
        F[_]: Async: Parallel: LocalContextProvider
    ](
        customConfig: Option[Config],
        tracerProviderCustomizer: Customizer[SdkTracerProvider.Builder[F]],
        resourceCustomizer: Customizer[TelemetryResource],
        propertiesLoader: F[Map[String, String]],
        propertiesCustomizers: List[Config => Map[String, String]]
    ) extends Builder[F] {

      def withConfig(config: Config): Builder[F] =
        copy(customConfig = Some(config))

      def addTracerProviderCustomizer(
          customizer: Customizer[SdkTracerProvider.Builder[F]]
      ): Builder[F] =
        copy(tracerProviderCustomizer =
          merge(this.tracerProviderCustomizer, customizer)
        )

      def addResourceCustomizer(
          customizer: Customizer[TelemetryResource]
      ): Builder[F] =
        copy(resourceCustomizer = merge(this.resourceCustomizer, customizer))

      def addPropertiesLoader(
          loader: F[Map[String, String]]
      ): Builder[F] =
        copy(propertiesLoader = (this.propertiesLoader, loader).mapN(_ ++ _))

      def addPropertiesCustomizer(
          customizer: Config => Map[String, String]
      ): Builder[F] =
        copy(propertiesCustomizers = this.propertiesCustomizers :+ customizer)

      def build: Resource[F, AutoConfigured[F]] = {
        def loadConfig: F[Config] =
          for {
            props <- propertiesLoader
            config <- Config.load(props)
          } yield propertiesCustomizers.foldLeft(config)((cfg, c) =>
            cfg.withOverrides(c(cfg))
          )

        def loadSdk(
            resource: TelemetryResource,
            config: Config
        ): Resource[F, OpenTelemetrySdk[F]] =
          Resource.eval(Random.scalaUtilRandom[F]).flatMap { implicit random =>
            Resource.eval(LocalProvider[F, Context].local).flatMap {
              implicit local =>
                implicit val console: Console[F] = Console.make[F]

                val builder =
                  SdkTracerProvider.builder[F].withResource(resource)

                for {
                  tpBuilder <-
                    TracerProviderAutoConfigure[F](builder).configure(config)

                  tracerProvider <- Resource.eval(
                    tracerProviderCustomizer(tpBuilder, config).build
                  )
                  propagators <- PropagatorsConfiguration[F].configure(config)
                } yield OpenTelemetrySdk(
                  propagators,
                  MeterProvider.noop[F],
                  tracerProvider
                )
            }
          }

        for {
          config <- Resource.eval(customConfig.fold(loadConfig)(Async[F].pure))
          // _ <- if (config.getString("OTEL_CONFIG_FILE")) return loadFromConfigFile
          resource <- TelemetryResourceAutoConfigure[F].configure(config)

          isDisabled <- Resource.eval(
            MonadCancelThrow[F].fromEither(
              config.get[Boolean]("otel.sdk.disabled")
            )
          )

          sdk <-
            if (isDisabled.getOrElse(false)) {
              Resource.pure[F, OpenTelemetrySdk[F]](OpenTelemetrySdk.noop[F])
            } else {
              loadSdk(resource, config)
            }
        } yield Impl[F](sdk, resource, config)
      }

      private def merge[A](
          first: Customizer[A],
          second: Customizer[A]
      ): Customizer[A] =
        (a, config) => second(first(a, config), config)

    }

    private final case class Impl[F[_]](
        sdk: OpenTelemetrySdk[F],
        resource: TelemetryResource,
        config: Config
    ) extends AutoConfigured[F]
  }

}
