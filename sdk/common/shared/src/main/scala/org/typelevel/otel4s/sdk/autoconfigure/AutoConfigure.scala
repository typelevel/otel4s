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

import cats.effect.MonadCancelThrow
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

  /** A component that must be associated with a specific name. Can be used for ad-hoc loading.
    *
    * See `PropagatorsAutoConfigure` and `SpanExportersAutoConfigure` for more examples.
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam A
    *   the type of the component
    */
  trait Named[F[_], A] extends AutoConfigure[F, A] {

    /** The name to associate the component with.
      */
    def name: String
  }

  object Named {

    /** Creates a [[Named]] auto configurer that always returns the same value.
      *
      * @param name
      *   the name to associate the component with
      *
      * @param component
      *   the component to return
      */
    def const[F[_], A](name: String, component: A): Named[F, A] =
      Const(name, component)

    private final case class Const[F[_], A](
        name: String,
        component: A
    ) extends Named[F, A] {
      def configure(config: Config): Resource[F, A] =
        Resource.pure(component)
    }

  }

  /** If the component cannot be created due to an error, the meaningful debug information will be added to the
    * exception.
    *
    * @param hint
    *   the name of the component. For example: Sampler, TelemetryResource
    *
    * @param configKeys
    *   the config keys that could be used to autoconfigure the component
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam A
    *   the type of the component
    */
  abstract class WithHint[F[_]: MonadCancelThrow, A](
      hint: String,
      configKeys: Set[Config.Key[_]]
  ) extends AutoConfigure[F, A] {

    final def configure(config: Config): Resource[F, A] =
      fromConfig(config).adaptError {
        case e: AutoConfigureError => e
        case cause                 => AutoConfigureError(hint, cause, configKeys, config)
      }

    protected def fromConfig(config: Config): Resource[F, A]
  }

}
