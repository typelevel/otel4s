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

package org.typelevel.otel4s.oteljava.baggage

import cats.Applicative
import io.opentelemetry.api.baggage.{Baggage => JBaggage}
import org.typelevel.otel4s.baggage.Baggage
import org.typelevel.otel4s.baggage.BaggageManager
import org.typelevel.otel4s.oteljava.baggage.BaggageConverters._
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.context.LocalContext

private final class BaggageManagerImpl[F[_]] private (implicit localContext: LocalContext[F])
    extends BaggageManager.Unsealed[F] {
  import BaggageManagerImpl.jBaggageFromContext

  protected def applicative: Applicative[F] =
    localContext.applicative
  def current: F[Baggage] =
    localContext.reader(jBaggageFromContext(_).toScala)
  def local[A](f: Baggage => Baggage)(fa: F[A]): F[A] =
    localContext.local(fa) { ctx =>
      val jCtx = ctx.underlying
      val updated = f(JBaggage.fromContext(jCtx).toScala).toJava
      Context.wrap(jCtx.`with`(updated))
    }
  def scope[A](baggage: Baggage)(fa: F[A]): F[A] =
    localContext.local(fa)(_.map(_.`with`(baggage.toJava)))
  override def get(key: String): F[Option[Baggage.Entry]] =
    localContext.reader { ctx =>
      Option(jBaggageFromContext(ctx).getEntry(key))
        .map(_.toScala)
    }
  override def getValue(key: String): F[Option[String]] =
    localContext.reader { ctx =>
      Option(jBaggageFromContext(ctx).getEntryValue(key))
    }
}

private[oteljava] object BaggageManagerImpl {
  private def jBaggageFromContext(context: Context): JBaggage =
    JBaggage.fromContext(context.underlying)

  def fromLocal[F[_]: LocalContext]: BaggageManager[F] =
    new BaggageManagerImpl[F]
}
