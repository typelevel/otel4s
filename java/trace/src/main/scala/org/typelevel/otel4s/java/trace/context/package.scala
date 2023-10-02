package org.typelevel.otel4s.java.trace

import cats.mtl.{Ask, Local}
import org.typelevel.vault.Vault

package object context {
  type LocalVault[F[_]] = Local[F, Vault]
  type AskVault[F[_]] = Ask[F, Vault]
}
