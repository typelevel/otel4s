package org.typelevel.otel4s.sdk.trace.exporter

import cats._
import cats.effect.kernel.Async
import cats.effect.std.Queue
import cats.syntax.all._
import cats.effect.kernel.Sync
import cats.effect.std.Console
import java.nio.charset.Charset

class InMemoryConsole[F[_]: Sync](queue: Queue[F, InMemoryConsole.Entry]) extends Console[F] {
  import InMemoryConsole.Entry
  import InMemoryConsole.Op

  def readLineWithCharset(charset: Charset): F[String] =
    Sync[F].delay(sys.error("not implemented"))

  def entries: F[List[Entry]] = 
    queue.tryTakeN(None)

  def print[A](a: A)(implicit S: Show[A]): F[Unit] =
    queue.offer(Entry(Op.Print, S.show(a)))

  def println[A](a: A)(implicit S: Show[A]): F[Unit] =
    queue.offer(Entry(Op.Println, S.show(a)))

  def error[A](a: A)(implicit S: Show[A]): F[Unit] =
    queue.offer(Entry(Op.Error, S.show(a)))

  def errorln[A](a: A)(implicit S: Show[A]): F[Unit] =
    queue.offer(Entry(Op.Errorln, S.show(a)))
}

object InMemoryConsole {

  sealed trait Op
  object Op {
    case object Print extends Op
    case object Println extends Op
    case object Error extends Op
    case object Errorln extends Op
  }

  final case class Entry(operation: Op, value: String)

  def create[F[_]: Async]: F[InMemoryConsole[F]] =
    Queue.unbounded[F, Entry].map { queue =>
      new InMemoryConsole[F](queue)
    }

}
