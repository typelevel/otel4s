
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

import cats.effect.{IO, IOApp, Resource, Temporal}
import cats.Parallel
import cats.syntax.all._
import io.opentelemetry.api.GlobalOpenTelemetry
import org.typelevel.otel4s.Otel4s
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.java.OtelJava
import org.typelevel.otel4s.trace.Tracer

import scala.concurrent.duration._

case class User(name: String, userId: String)
trait UserDatabase[F[_]] { 
    def readUser(userId: String): F[User]
}

trait InstitutionServiceClient[F[_]] { 
    def getUserIds(institutionId: String): F[List[String]]
}

trait UsersAlg[F[_]] {
  def getAllUsersForInstitution(institutionId: String): F[List[User]]
}

object UserDatabase { 
    def apply[F[_]: Tracer: Temporal]: UserDatabase[F] = new UserDatabase[F] {
        def readUser(userId: String): F[User] = 
          Tracer[F].span("Read User from DB", Attribute("userId", userId)).use { span => 
            val returnedUser = User("Zach", "12345")
              Temporal[F].sleep(30.millis) *>
                span.addAttribute(Attribute("User", returnedUser.name)).map(_ => returnedUser)
          } 
    }
  }

object InstitutionServiceClient { 
    def apply[F[_]: Tracer: Temporal]: InstitutionServiceClient[F] = new InstitutionServiceClient[F] { 
        def getUserIds(institutionId: String): F[List[String]] = 
          Tracer[F].span("Get User Ids from Institution Service", Attribute("institutionId", institutionId)).surround { 
            Temporal[F].sleep(110.millis).map(_ => List("12345", "87987", "12345", "87987", "12345", "87987"))
          }
    }
}

object UserIdsAlg {
  def apply[F[_]: Tracer: Temporal: Parallel](institutionService: InstitutionServiceClient[F], userDB: UserDatabase[F]): UsersAlg[F] = new UsersAlg[F] { 
    def getAllUsersForInstitution(institutionId: String): F[List[User]] = 
      Tracer[F].span("Get users for institution").surround { 
        institutionService.getUserIds(institutionId).flatMap(userIds => userIds.parTraverse(userDB.readUser))
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
      otel4s.tracerProvider.tracer("TraceExample").get.flatMap { implicit tracer: Tracer[IO] =>
          val userIdAlg = UserIdsAlg.apply[IO](InstitutionServiceClient.apply[IO], UserDatabase.apply[IO])
          val resource: Resource[IO, Unit] =
            Resource.make(IO.sleep(50.millis))(_ => IO.sleep(100.millis))
          tracer
            .resourceSpan("Start up")(resource)
            .surround(
              userIdAlg.getAllUsersForInstitution("9902181e-1d8d-4e00-913d-51532b493f1b").flatMap(IO.println)
            )
      }
    }
  }
}

