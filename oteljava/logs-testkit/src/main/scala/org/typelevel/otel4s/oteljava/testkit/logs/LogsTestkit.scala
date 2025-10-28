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

package org.typelevel.otel4s.oteljava.testkit.logs

import cats.effect.Async
import cats.effect.Resource
import cats.mtl.Ask
import cats.syntax.all._
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder
import io.opentelemetry.sdk.logs.`export`.LogRecordExporter
import io.opentelemetry.sdk.logs.`export`.SimpleLogRecordProcessor
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.logs.LoggerProvider
import org.typelevel.otel4s.oteljava.context.{AskContext, Context, LocalContext, LocalContextProvider}
import org.typelevel.otel4s.oteljava.logs.LoggerProviderImpl
import org.typelevel.otel4s.oteljava.testkit.Conversions

import scala.jdk.CollectionConverters._

sealed trait LogsTestkit[F[_]] {

  /** The [[org.typelevel.otel4s.logs.LoggerProvider LoggerProvider]].
    */
  def loggerProvider: LoggerProvider[F, Context]

  /** Collects and returns logs.
    *
    * @example
    *   {{{
    * import io.opentelemetry.sdk.logs.data.LogRecordData
    *
    * LogsTestkit[F].collectLogs[LogRecordData] // OpenTelemetry Java models
    *   }}}
    *
    * @see
    *   [[resetLogs]] to reset the internal buffer
    */
  def collectLogs[A: FromLogRecordData]: F[List[A]]

  /** Resets the internal buffer.
    */
  def resetLogs: F[Unit]
}

object LogsTestkit {
  private[oteljava] trait Unsealed[F[_]] extends LogsTestkit[F]

  /** Builder for [[LogsTestkit]]. */
  sealed trait Builder[F[_]] {

    /** Adds the loger provider builder customizer. Multiple customizers can be added, and they will be applied in the
      * order they were added.
      *
      * @param customizer
      *   the customizer to add
      */
    def addLoggerProviderCustomizer(customizer: SdkLoggerProviderBuilder => SdkLoggerProviderBuilder): Builder[F]

    /** Sets the `InMemoryLogRecordExporter` to use. Useful when a Scala and Java instrumentation need to share the same
      * exporter.
      *
      * @param exporter
      *   the exporter to use
      */
    def withInMemoryLogRecordExporter(exporter: InMemoryLogRecordExporter): Builder[F]

    /** Creates [[LogsTestkit]] using the configuration of this builder.
      */
    def build: Resource[F, LogsTestkit[F]]

  }

  /** Creates a [[Builder]] of [[LogsTestkit]] with the default configuration.
    */
  def builder[F[_]: Async: LocalContextProvider]: Builder[F] =
    new BuilderImpl[F]()

  /** Creates a [[LogsTestkit]] using [[Builder]]. The instance keeps logs in memory.
    *
    * @param customize
    *   a function for customizing the builder
    */
  def inMemory[F[_]: Async: LocalContextProvider](
      customize: Builder[F] => Builder[F] = identity(_)
  ): Resource[F, LogsTestkit[F]] =
    customize(builder[F]).build

  /** Creates [[LogsTestkit]] that keeps logs in-memory.
    *
    * @param customize
    *   the customization of the builder
    *
    * @note
    *   this implementation uses a constant root `Context` via `Ask.const(Context.root)`. That means the module is
    *   isolated: it does not inherit or propagate the surrounding span context. This is useful if you only need logging
    *   (without traces or metrics) and want the module to operate independently. If instead you want interoperability -
    *   i.e. to capture the current span context so that logs, traces, and metrics can all work together - use
    *   `OtelJavaTestkit.inMemory`.
    */
  @deprecated(
    "Use `LogsTestkit.inMemory` overloaded alternative with `Builder[F]` customizer or `LogsTestkit.builder`",
    "0.15.0"
  )
  def inMemory[F[_]: Async](
      customize: SdkLoggerProviderBuilder => SdkLoggerProviderBuilder = identity
  ): Resource[F, LogsTestkit[F]] = {
    implicit val askContext: AskContext[F] = Ask.const(Context.root)
    for {
      inMemoryLogRecordExporter <- Resource.eval(Async[F].delay(InMemoryLogRecordExporter.create()))
      logsTestkit <- create[F](inMemoryLogRecordExporter, customize)
    } yield logsTestkit
  }

  /** Creates [[LogsTestkit]] that keeps logs in-memory from an existing exporter. Useful when a Scala instrumentation
    * requires a Java instrumentation, both sharing the same exporter.
    *
    * @param customize
    *   the customization of the builder
    *
    * @note
    *   this implementation uses a constant root `Context` via `Ask.const(Context.root)`. That means the module is
    *   isolated: it does not inherit or propagate the surrounding span context. This is useful if you only need logging
    *   (without traces or metrics) and want the module to operate independently. If instead you want interoperability -
    *   i.e. to capture the current span context so that logs, traces, and metrics can all work together - use
    *   `OtelJavaTestkit.inMemory`.
    */
  @deprecated(
    "Use `LogsTestkit.inMemory` overloaded alternative with `Builder[F]` customizer or `LogsTestkit.builder`",
    "0.15.0"
  )
  def fromInMemory[F[_]: Async](
      inMemoryLogRecordExporter: InMemoryLogRecordExporter,
      customize: SdkLoggerProviderBuilder => SdkLoggerProviderBuilder = identity
  ): Resource[F, LogsTestkit[F]] = {
    implicit val askContext: AskContext[F] = Ask.const(Context.root)
    create[F](inMemoryLogRecordExporter, customize)
  }

  private[oteljava] def create[F[_]: Async: AskContext](
      customize: SdkLoggerProviderBuilder => SdkLoggerProviderBuilder
  ): Resource[F, LogsTestkit[F]] =
    for {
      inMemoryLogRecordExporter <- Resource.eval(Async[F].delay(InMemoryLogRecordExporter.create()))
      logsTestkit <- create[F](inMemoryLogRecordExporter, customize)
    } yield logsTestkit

  private def create[F[_]: Async: AskContext](
      inMemoryLogRecordExporter: InMemoryLogRecordExporter,
      customize: SdkLoggerProviderBuilder => SdkLoggerProviderBuilder
  ): Resource[F, LogsTestkit[F]] = {
    def createProcessor(exporter: LogRecordExporter) =
      Async[F].delay(SimpleLogRecordProcessor.create(exporter))

    def createLoggerProvider(processor: LogRecordProcessor) =
      Async[F].delay {
        val builder = SdkLoggerProvider
          .builder()
          .addLogRecordProcessor(processor)

        customize(builder).build()
      }

    for {
      processor <- Resource.fromAutoCloseable(createProcessor(inMemoryLogRecordExporter))
      provider <- Resource.fromAutoCloseable(createLoggerProvider(processor))
    } yield new Impl(new LoggerProviderImpl(provider), processor, inMemoryLogRecordExporter)
  }

  private final class Impl[F[_]: Async](
      val loggerProvider: LoggerProvider[F, Context],
      processor: LogRecordProcessor,
      exporter: InMemoryLogRecordExporter
  ) extends LogsTestkit[F] {

    def collectLogs[A: FromLogRecordData]: F[List[A]] =
      for {
        _ <- Conversions.asyncFromCompletableResultCode(
          Async[F].delay(processor.forceFlush())
        )
        result <- Async[F].delay(exporter.getFinishedLogRecordItems)
      } yield result.asScala.toList.map(FromLogRecordData[A].from)

    def resetLogs: F[Unit] =
      Async[F].delay(exporter.reset())

  }

  private final case class BuilderImpl[F[_]: Async: LocalContextProvider](
      customizer: SdkLoggerProviderBuilder => SdkLoggerProviderBuilder = identity(_),
      inMemoryLogRecordExporter: Option[InMemoryLogRecordExporter] = None,
  ) extends Builder[F] {

    def addLoggerProviderCustomizer(f: SdkLoggerProviderBuilder => SdkLoggerProviderBuilder): Builder[F] =
      copy(customizer = this.customizer.andThen(f))

    def withInMemoryLogRecordExporter(exporter: InMemoryLogRecordExporter): Builder[F] =
      copy(inMemoryLogRecordExporter = Some(exporter))

    def build: Resource[F, LogsTestkit[F]] =
      Resource.eval(LocalProvider[F, Context].local).flatMap { implicit local =>
        inMemoryLogRecordExporter.fold(create[F](customizer))(create[F](_, customizer))
      }
  }
}
