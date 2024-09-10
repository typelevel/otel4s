import sbt._
import scala.sys.process._

object SemanticConventionsGenerator {

  private val generatorVersion = "v0.9.1"

  // generates semantic conventions by using `otel/weaver` in docker
  def generate(version: String, rootDir: File): Unit = {
    generateOne(version, rootDir, experimental = false)
    generateOne(version, rootDir, experimental = true)
  }

  private def generateOne(
      version: String,
      rootDir: File,
      experimental: Boolean
  ): Unit = {
    val semanticConventionsRepoZip =
      s"https://github.com/open-telemetry/semantic-conventions/archive/v$version.zip"

    val outputDir =
      if (experimental)
        s"$rootDir/semconv/experimental/src/main/scala/org/typelevel/otel4s/semconv/experimental/attributes"
      else
        s"$rootDir/semconv/stable/src/main/scala/org/typelevel/otel4s/semconv/attributes"

    val target = "otel4s/attributes"

    val params: List[String] =
      if (experimental)
        List(
          "--param=excluded_stability=[]",
          "--param=object_prefix=Experimental",
          "--param=experimental=true"
        )
      else
        Nil

    val buildDir = rootDir / "buildscripts" / "semantic-convention"
    val zip = buildDir / "semantic-conventions.zip"
    val conventionsDir = buildDir / s"semantic-conventions-$version"

    IO.delete(zip)
    IO.delete(conventionsDir)

    (url(semanticConventionsRepoZip) #> zip).!

    IO.unzip(zip, buildDir)

    // format: off
    val command = List(
      "docker",
      "run",
      "--rm",
      "-v", s"$conventionsDir/model:/home/weaver/source",
      "-v", s"$buildDir/templates:/home/weaver/templates",
      "-v", s"$outputDir:/home/weaver/target",
      "--platform", "linux/amd64",
      s"otel/weaver:$generatorVersion",
      "registry", "generate",
      "--registry=/home/weaver/source",
      "--templates=/home/weaver/templates",
    ) ++ params ++ List(target, "/home/weaver/target/")
    // format: on

    Process(command, rootDir).!
  }
}
