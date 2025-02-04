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
    extends BaggageManager[F] {
  def applicative: Applicative[F] = localContext.applicative
  def ask[E2 >: Baggage]: F[E2] =
    localContext.reader { ctx =>
      Option(JBaggage.fromContextOrNull(ctx.underlying))
        .fold(Baggage.empty)(_.toScala)
    }
  def local[A](fa: F[A])(f: Baggage => Baggage): F[A] =
    localContext.local(fa) { ctx =>
      val jCtx = ctx.underlying
      val jBaggage = JBaggage.fromContext(jCtx)
      val updated = f(jBaggage.toScala).toJava
      Context.wrap(jCtx.`with`(updated))
    }
  override def get(key: String): F[Option[Baggage.Entry]] =
    localContext.reader { ctx =>
      Option(JBaggage.fromContext(ctx.underlying).getEntry(key))
        .map(_.toScala)
    }
  override def getValue(key: String): F[Option[String]] =
    localContext.reader { ctx =>
      Option(JBaggage.fromContext(ctx.underlying).getEntryValue(key))
    }
}

private[oteljava] object BaggageManagerImpl {
  def fromLocal[F[_]: LocalContext]: BaggageManager[F] =
    new BaggageManagerImpl[F]
}
