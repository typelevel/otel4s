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

package org.typelevel.otel4s
package sdk
package internal

import cats.Applicative
import cats.effect.kernel.Concurrent
import cats.effect.std.AtomicCell
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.common.InstrumentationScope

/** A registry that caches components by `key`, `version`, `schemaUrl`, and `attributes`.
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  *
  * @tparam A
  *   the type of the component
  */
private[sdk] sealed trait ComponentRegistry[F[_], A] {

  /** Returns the component associated with the `name`, `version`, `schemaUrl`, and `attributes`.
    *
    * @param name
    *   the name to associate with a component
    *
    * @param version
    *   the version to associate with a component
    *
    * @param schemaUrl
    *   the schema URL to associate with a component
    *
    * @param attributes
    *   the attributes to associate with a component
    */
  def get(name: String, version: Option[String], schemaUrl: Option[String], attributes: Attributes): F[A]

  /** Returns the collection of the registered components.
    */
  def components: F[Vector[A]]

}

private[sdk] object ComponentRegistry {

  /** Creates a [[ComponentRegistry]] that uses `buildComponent` to build a component if it is not already present in
    * the cache.
    *
    * @param buildComponent
    *   how to build a component
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam A
    *   the type of the component
    */
  def create[F[_]: Concurrent, A](buildComponent: InstrumentationScope => F[A]): F[ComponentRegistry[F, A]] =
    for {
      cache <- AtomicCell[F].of(Map.empty[InstrumentationScope, A])
    } yield new Impl(cache, buildComponent)

  private final class Impl[F[_]: Applicative, A](
      cache: AtomicCell[F, Map[InstrumentationScope, A]],
      buildComponent: InstrumentationScope => F[A]
  ) extends ComponentRegistry[F, A] {

    def get(name: String, version: Option[String], schemaUrl: Option[String], attributes: Attributes): F[A] =
      cache.evalModify { cache =>
        val scope = InstrumentationScope(name, version, schemaUrl, attributes)

        cache.get(scope) match {
          case Some(component) =>
            Applicative[F].pure((cache, component))

          case None =>
            for {
              component <- buildComponent(scope)
            } yield (cache.updated(scope, component), component)
        }
      }

    def components: F[Vector[A]] =
      cache.get.map(_.values.toVector)
  }

}
