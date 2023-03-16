
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

import cats.Monad
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import cats.syntax.all._
import io.opentelemetry.api.GlobalOpenTelemetry
import org.typelevel.otel4s.Otel4s
import org.typelevel.otel4s.TextMapPropagator
import org.typelevel.otel4s.java.OtelJava
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.Tracer
import org.typelevel.vault.Vault

import scala.concurrent.duration._

case class User(name: String, userId: String)
trait UserDatabase[F[_]] { 
    def readUser(userId: String): F[User]
}

trait InstitutionService[F[_]] { 
    def getUserIds(institutionId: String): F[List[String]]
}

trait UsersAlg[F[_]] {
  def getAllUsersForInstitution(institutionId: String): F[List[User]]
}

object UserDatabase { 
    def apply[F[_]: Monad : Tracer]: UserDatabase[F] = new UserDatabase[F] { 
        def readUser(userId: String): F[User] = Tracer[F].span("Read User from DB").surround { //Can add attribute here and should
            Monad[F].pure(User("Zach", "12345"))
        }
    }
  }

object InstitutionService { 
    def apply[F[_]: Monad : Tracer: TextMapPropagator]: InstitutionService[F] = new InstitutionService[F] { 
        def getUserIds(institutionId: String): F[List[String]] = Tracer[F].span("Read User from DB").surround { 
          Monad[F].pure(List("12345", "123456"))
        }
    }
}

object UserIdsAlg {
  def apply[F[_]: Monad : Tracer: TextMapPropagator](institutionService: InstitutionService[F], userDB: UserDatabase[F]): UsersAlg[F] = new UsersAlg[F] { 
    def getAllUsersForInstitution(institutionId: String): F[List[User]] = Tracer[F].span("Get users for institution").surround { 
      institutionService.getUserIds(institutionId).flatMap(userIds => userIds.traverse(userDB.readUser))
    }
  }
}

object TraceExample extends IOApp.Simple {
  def globalOtel4s: Resource[IO, Otel4s[IO]] =
    Resource
      .eval(IO(GlobalOpenTelemetry.get))
      .evalMap(OtelJava.forSync[IO])

  def run: IO[Unit] = {
    globalOtel4s.use { (otel4s: Otel4s[IO]) =>
      implicit val textMapProp: TextMapPropagator[IO] =
        otel4s.propagators.textMapPropagator
      otel4s.tracerProvider.tracer("TraceExample").get.flatMap { implicit tracer: Tracer[IO] =>
          val instService = InstitutionService.apply[IO]
          val userDB = UserDatabase.apply[IO]
          val userIdAlg = UserIdsAlg.apply[IO](instService, userDB)
          val resource: Resource[IO, Unit] =
            Resource.make(IO.sleep(50.millis))(_ => IO.sleep(100.millis))
          tracer
            .resourceSpan("Start up")(resource)
            .surround(
              userIdAlg.getAllUsersForInstitution("instId").flatMap(IO.println)
            )
      }
    }
  }
}

