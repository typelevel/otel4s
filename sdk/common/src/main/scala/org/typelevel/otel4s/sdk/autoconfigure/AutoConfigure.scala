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

package org.typelevel.otel4s.sdk.autoconfigure

import cats.MonadThrow
import cats.effect.Resource
import cats.syntax.monadError._

/** Creates and autoconfigures the component of type `A` using `config`.
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  *
  * @tparam A
  *   the type of the component
  */
trait AutoConfigure[F[_], A] {
  def configure(config: Config): Resource[F, A]
}

object AutoConfigure {

  abstract class WithHint[F[_]: MonadThrow, A](
      hint: String
  ) extends AutoConfigure[F, A] {

    final def configure(config: Config): Resource[F, A] =
      fromConfig(config).adaptError {
        case e: AutoConfigureError => e
        case cause                 => new AutoConfigureError(hint, cause)
      }

    protected def fromConfig(config: Config): Resource[F, A]
  }

}
