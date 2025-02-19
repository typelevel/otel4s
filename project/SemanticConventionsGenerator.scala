import sbt._
import scala.sys.process._

object SemanticConventionsGenerator {

  private val generatorVersion = "v0.13.2"

  // generates semantic conventions by using `otel/weaver` in docker
  def generate(version: String, rootDir: File): Unit = {
    generateAttributes(version, rootDir)
    generateMetrics(version, rootDir)
  }

  private def generateAttributes(version: String, rootDir: File): Unit = {
    generateAttributes(version, rootDir, experimental = false)
    generateAttributes(version, rootDir, experimental = true)
  }

  private def generateMetrics(version: String, rootDir: File): Unit = {
    generateMetrics(version, rootDir, experimental = false, tests = false)
    generateMetrics(version, rootDir, experimental = true, tests = false)
    // only stable semantics require tests
    generateMetrics(version, rootDir, experimental = false, tests = true)
  }

  private def generateAttributes(
      version: String,
      rootDir: File,
      experimental: Boolean
  ): Unit = {
    val outputDir =
      if (experimental)
        s"$rootDir/semconv/experimental/src/main/scala/org/typelevel/otel4s/semconv/experimental/attributes"
      else
        s"$rootDir/semconv/stable/src/main/scala/org/typelevel/otel4s/semconv/attributes"

    val target = "otel4s/attributes"

    val params: List[String] =
      if (experimental)
        List(
          "--param=object_prefix=Experimental",
          "--param=stable_only=false"
        )
      else
        Nil

    invokeWeaverGenerator(version, rootDir, outputDir, target, params)
  }

  private def generateMetrics(
      version: String,
      rootDir: File,
      experimental: Boolean,
      tests: Boolean
  ): Unit = {
    val dir = if (tests) "test" else "main"

    val outputDir =
      if (experimental)
        s"$rootDir/semconv/metrics/experimental/src/$dir/scala/org/typelevel/otel4s/semconv/experimental/metrics/"
      else
        s"$rootDir/semconv/metrics/stable/src/$dir/scala/org/typelevel/otel4s/semconv/metrics/"

    val target =
      if (tests) "otel4s/metrics-tests" else "otel4s/metrics"

    val params: List[String] =
      if (experimental)
        List(
          "--param=object_prefix=Experimental",
          "--param=stable_only=false"
        )
      else
        Nil

    invokeWeaverGenerator(version, rootDir, outputDir, target, params)
  }

  private def invokeWeaverGenerator(
      version: String,
      rootDir: File,
      outputDir: String,
      target: String,
      params: List[String]
  ): Unit = {
    val semanticConventionsRepoZip =
      s"https://github.com/open-telemetry/semantic-conventions/archive/v$version.zip"

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
