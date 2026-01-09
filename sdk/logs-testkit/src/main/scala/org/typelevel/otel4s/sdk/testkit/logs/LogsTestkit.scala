/*
 * Copyright 2025 Typelevel
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

package org.typelevel.otel4s.sdk.testkit.logs

import cats.Parallel
import cats.effect.Resource
import cats.effect.Temporal
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.logs.LoggerProvider
import org.typelevel.otel4s.sdk.common.Diagnostic
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.LocalContextProvider
import org.typelevel.otel4s.sdk.logs.SdkLoggerProvider
import org.typelevel.otel4s.sdk.logs.data.LogRecordData
import org.typelevel.otel4s.sdk.logs.processor.SimpleLogRecordProcessor

sealed trait LogsTestkit[F[_]] {

  /** The [[org.typelevel.otel4s.logs.LoggerProvider LoggerProvider]].
    */
  def loggerProvider: LoggerProvider[F, Context]

  /** Collects and returns logs.
    *
    * @note
    *   logs are recollected on each invocation
    */
  def collectLogs: F[List[LogRecordData]]
}

object LogsTestkit {
  private[sdk] trait Unsealed[F[_]] extends LogsTestkit[F]

  /** Builder for [[LogsTestkit]]. */
  sealed trait Builder[F[_]] {

    /** Adds the logger provider builder customizer. Multiple customizers can be added, and they will be applied in the
      * order they were added.
      *
      * @param customizer
      *   the customizer to add
      */
    def addLoggerProviderCustomizer(
        customizer: SdkLoggerProvider.Builder[F] => SdkLoggerProvider.Builder[F]
    ): Builder[F]

    /** Creates [[LogsTestkit]] using the configuration of this builder. */
    def build: Resource[F, LogsTestkit[F]]

  }

  /** Creates a [[Builder]] of [[LogsTestkit]] with the default configuration. */
  def builder[F[_]: Temporal: Parallel: Diagnostic: LocalContextProvider]: Builder[F] =
    new BuilderImpl[F]()

  /** Creates a [[LogsTestkit]] using [[Builder]]. The instance keeps logs in memory.
    *
    * @param customize
    *   a function for customizing the builder
    */
  def inMemory[F[_]: Temporal: Parallel: Diagnostic: LocalContextProvider](
      customize: Builder[F] => Builder[F] = identity[Builder[F]](_)
  ): Resource[F, LogsTestkit[F]] =
    customize(builder[F]).build

  private def create[F[_]: Temporal: Parallel: Diagnostic: AskContext](
      customize: SdkLoggerProvider.Builder[F] => SdkLoggerProvider.Builder[F]
  ): Resource[F, LogsTestkit[F]] = {
    def createLoggerProvider(
        exporter: InMemoryLogRecordExporter[F]
    ): F[LoggerProvider[F, Context]] = {
      val processor = SimpleLogRecordProcessor[F](exporter)
      val builder = SdkLoggerProvider.builder[F].addLogRecordProcessor(processor)
      customize(builder).build
    }

    for {
      exporter <- Resource.eval(InMemoryLogRecordExporter.create[F](None))
      loggerProvider <- Resource.eval(createLoggerProvider(exporter))
    } yield new Impl(loggerProvider, exporter)
  }

  private final class Impl[F[_]](
      val loggerProvider: LoggerProvider[F, Context],
      exporter: InMemoryLogRecordExporter[F]
  ) extends LogsTestkit[F] {
    def collectLogs: F[List[LogRecordData]] =
      exporter.exportedLogs
  }

  private final case class BuilderImpl[F[_]: Temporal: Parallel: Diagnostic: LocalContextProvider](
      customizer: SdkLoggerProvider.Builder[F] => SdkLoggerProvider.Builder[F] = (b: SdkLoggerProvider.Builder[F]) => b
  ) extends Builder[F] {

    def addLoggerProviderCustomizer(
        customizer: SdkLoggerProvider.Builder[F] => SdkLoggerProvider.Builder[F]
    ): Builder[F] =
      copy(customizer = this.customizer.andThen(customizer))

    def build: Resource[F, LogsTestkit[F]] =
      Resource.eval(LocalProvider[F, Context].local).flatMap { implicit local =>
        create[F](customizer)
      }
  }

}
