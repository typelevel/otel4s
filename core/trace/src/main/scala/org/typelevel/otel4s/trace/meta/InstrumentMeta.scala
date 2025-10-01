package org.typelevel.otel4s.trace.meta

import cats.Applicative
import cats.Monad
import cats.~>
import org.typelevel.otel4s.KindTransformer

/** The instrument's metadata. Indicates whether instrumentation is enabled.
  */
sealed trait InstrumentMeta[F[_]] extends org.typelevel.otel4s.meta.InstrumentMeta.Dynamic.Unsealed[F] {

  /** Indicates whether instrumentation is enabled or not.
    */
  def isEnabled: F[Boolean]

  /** A no-op effect.
    */
  def unit: F[Unit]

  /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to `G`.
    */
  override def liftTo[G[_]: Monad](implicit kt: KindTransformer[F, G]): InstrumentMeta[G] =
    new InstrumentMeta.MappedK(this)(kt.liftK)

}

object InstrumentMeta {

  private[otel4s] def from[F[_]: Monad](enabled: F[Boolean]): InstrumentMeta[F] =
    new InstrumentMeta[F] {
      def isEnabled: F[Boolean] = enabled
      def unit: F[Unit] = Monad[F].unit
      def whenEnabled(f: => F[Unit]): F[Unit] = Monad[F].ifM(isEnabled)(f, unit)
    }

  private[otel4s] def enabled[F[_]: Applicative]: InstrumentMeta[F] =
    new Const[F](true)

  private[otel4s] def disabled[F[_]: Applicative]: InstrumentMeta[F] =
    new Const[F](false)

  private final class Const[F[_]: Applicative](value: Boolean) extends InstrumentMeta[F] {
    val isEnabled: F[Boolean] = Applicative[F].pure(value)
    val unit: F[Unit] = Applicative[F].unit
    def whenEnabled(f: => F[Unit]): F[Unit] = Applicative[F].whenA(value)(f)
  }

  private final class MappedK[F[_], G[_]: Monad](meta: InstrumentMeta[F])(f: F ~> G) extends InstrumentMeta[G] {
    def isEnabled: G[Boolean] = f(meta.isEnabled)
    def unit: G[Unit] = f(meta.unit)
    def whenEnabled(f: => G[Unit]): G[Unit] = Monad[G].ifM(isEnabled)(f, Monad[G].unit)
  }
}
