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
import org.typelevel.otel4s.logs.LoggerProvider
import org.typelevel.otel4s.oteljava.context.AskContext
import org.typelevel.otel4s.oteljava.context.Context
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

  /** Creates [[LogsTestkit]] that keeps logs in-memory.
    *
    * @param customize
    *   the customization of the builder
    */
  def inMemory[F[_]: Async](
      customize: SdkLoggerProviderBuilder => SdkLoggerProviderBuilder = identity
  ): Resource[F, LogsTestkit[F]] = {
    implicit val askContext: AskContext[F] = Ask.const(Context.root)
    create[F](customize)
  }

  private[oteljava] def create[F[_]: Async: AskContext](
      customize: SdkLoggerProviderBuilder => SdkLoggerProviderBuilder
  ): Resource[F, LogsTestkit[F]] = {

    def createExporter =
      Async[F].delay(InMemoryLogRecordExporter.create())

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
      exporter <- Resource.fromAutoCloseable(createExporter)
      processor <- Resource.fromAutoCloseable(createProcessor(exporter))
      provider <- Resource.fromAutoCloseable(createLoggerProvider(processor))
    } yield new Impl(new LoggerProviderImpl(provider), processor, exporter)
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
}
