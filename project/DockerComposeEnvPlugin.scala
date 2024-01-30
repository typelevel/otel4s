import sbt._
import sbt.Keys._

import scala.sys.process._

object DockerComposeEnvPlugin extends AutoPlugin {

  override def trigger = noTrigger

  object autoImport {
    lazy val dockerComposeEnvStart =
      taskKey[Unit]("Start local docker compose environment")
    lazy val dockerComposeEnvStop =
      taskKey[Unit]("Stop local docker compose environment")
    lazy val dockerComposeEnvTestOpts =
      taskKey[Seq[TestOption]]("Setup and cleanup options")
    lazy val dockerComposeEnvFile =
      settingKey[File]("The docker compose file to use")
  }

  import autoImport._

  override def projectSettings: Seq[Setting[_]] =
    Seq(
      dockerComposeEnvStart := {
        val projectName = name.value
        val file = dockerComposeEnvFile.value
        val log = streams.value.log

        start(projectName, file, log)
      },
      dockerComposeEnvStop := {
        val projectName = name.value
        val file = dockerComposeEnvFile.value
        val log = streams.value.log

        stop(projectName, file, log)
      },
      Test / testOptions := {
        val projectName = name.value
        val file = dockerComposeEnvFile.value
        val log = streams.value.log

        val setup = Tests.Setup(() => start(projectName, file, log))

        Seq(setup)
      }
    )

  private def start(projectName: String, file: File, log: Logger): Unit = {
    log.info(s"Project [$projectName]. Creating integration environment")
    s"docker compose -f $file -p $projectName up -d".!
  }

  private def stop(projectName: String, file: File, log: Logger): Unit = {
    log.info(s"Project [$projectName]. Terminating integration environment")
    s"docker compose -f $file -p $projectName down".!
  }

}
