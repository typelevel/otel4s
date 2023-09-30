package org.typelevel.otel4s.sdk

import cats.mtl.Ask
import cats.mtl.Local

package object context {

  type AskContext[F[_]] = Ask[F, Context]
  type LocalContext[F[_]] = Local[F, Context]

}
