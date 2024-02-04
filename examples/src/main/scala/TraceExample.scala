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

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Temporal
import cats.effect.implicits._
import cats.implicits._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Otel4s
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.trace.Tracer

import scala.concurrent.duration._

/** An interface for reading a user from a database */
case class User(name: String, userId: String)
trait UserDatabase[F[_]] {
  def readUser(userId: String): F[User]
}

/** An interface for getting a list of user ids from another service */
trait InstitutionServiceClient[F[_]] {
  def getUserIds(institutionId: String): F[List[String]]
}

/** An interface to compose the getUserIds and readUser methods to get all Users
  * from a single institution.
  */
trait UsersAlg[F[_]] {
  def getAllUsersForInstitution(institutionId: String): F[List[User]]
}

/** User Database Mock
  *
  * This database implementation returns a single user out while waiting 30
  * milliseconds to simulate some time passing. The Tracer[F].span is used to
  * span over the simulated database call. Note that `span.addAttribute` is used
  * inside of the `.use` to add another attribute to the span with a value that
  * we only know at the time of running the database read.
  */
object UserDatabase {
  def mockDBQuery[F[_]: Temporal]: F[User] =
    Temporal[F].sleep(30.millis).as(User("Zach", "1"))

  def apply[F[_]: Tracer: Temporal]: UserDatabase[F] = new UserDatabase[F] {
    def readUser(userId: String): F[User] =
      Tracer[F].span("Read User from DB", Attribute("userId", userId)).use {
        span =>
          mockDBQuery[F].flatTap { user =>
            span
              .addAttribute(Attribute("User", user.name))
          }
      }
  }
}

/** Institution Service Client (Mock Http)
  *
  * This client implementation returns a list of ids out while waiting 110
  * milliseconds to simulate some time passing. The Tracer[F].span is used to
  * span over the http client call.
  */

object InstitutionServiceClient {
  def mockHttpCall[F[_]: Temporal]: F[List[String]] =
    Temporal[F].sleep(110.millis).as(List("1", "2", "3"))

  def apply[F[_]: Tracer: Temporal]: InstitutionServiceClient[F] =
    new InstitutionServiceClient[F] {
      def getUserIds(institutionId: String): F[List[String]] =
        Tracer[F]
          .span(
            "Get User Ids from Institution Service",
            Attribute("institutionId", institutionId),
            Attribute("span.kind", "client")
          )
          .surround {
            mockHttpCall[F]
          }
    }
}

/** UserIds Algebra
  *
  * This implementation composes our two methods from the separate traits into a
  * method that gives us the list of users for an institution. Note that in this
  * we will have a list of the same user repeated because of how the mocking
  * above works. The end result of this is we have a span named "Get users for
  * institution" that has 3 children spans (one for each id in getUserIds).
  */
object UserIdsAlg {
  def apply[F[_]: Tracer: Temporal](
      institutionService: InstitutionServiceClient[F],
      userDB: UserDatabase[F]
  ): UsersAlg[F] = new UsersAlg[F] {
    def getAllUsersForInstitution(institutionId: String): F[List[User]] =
      Tracer[F].span("Get users for institution").surround {
        institutionService
          .getUserIds(institutionId)
          .flatMap(userIds => userIds.parTraverse(userDB.readUser))
      }
  }
}

object TraceExample extends IOApp.Simple {

  /** Run Method
    *
    * This run methods creates a span over the resource taking 50 milliseconds
    * to acquire and then 100 to shutdown, but in the middle are the child spans
    * from our UserIdsAlg.
    */
  def run: IO[Unit] =
    OtelJava
      .autoConfigured[IO]()
      .evalMap { (otel4s: Otel4s[IO]) =>
        otel4s.tracerProvider.tracer("TraceExample").get.flatMap {
          implicit tracer: Tracer[IO] =>
            val userIdAlg = UserIdsAlg.apply[IO](
              InstitutionServiceClient.apply[IO],
              UserDatabase.apply[IO]
            )
            tracer
              .span("Start up")
              .use { span =>
                for {
                  _ <- tracer.span("acquire").surround(IO.sleep(50.millis))
                  _ <- span.addEvent("event")
                  _ <- tracer.span("use").surround {
                    userIdAlg
                      .getAllUsersForInstitution(
                        "9902181e-1d8d-4e00-913d-51532b493f1b"
                      )
                      .flatMap(IO.println)
                  }
                  _ <- tracer.span("release").surround(IO.sleep(100.millis))
                } yield ()
              }
        }
      }
      .use_
}
