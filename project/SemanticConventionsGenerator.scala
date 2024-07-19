import sbt._
import scala.sys.process._

object SemanticConventionsGenerator {

  private val generatorVersion = "0.24.0"

  // generates semantic conventions by using `otel/semconvgen` in docker
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

    val schemaUrl =
      s"https://opentelemetry.io/schemas/$version"

    val filter = if (experimental) "any" else "is_stable"
    val classPrefix = if (experimental) "Experimental" else ""

    val outputDir =
      if (experimental)
        s"$rootDir/semconv/experimental/src/main/scala/org/typelevel/otel4s/semconv/experimental/"
      else
        s"$rootDir/semconv/stable/src/main/scala/org/typelevel/otel4s/semconv/"

    val packageNameArg =
      if (experimental) "org.typelevel.otel4s.semconv.experimental.attributes"
      else "org.typelevel.otel4s.semconv.attributes"

    val stablePackageNameArg =
      if (experimental) "org.typelevel.otel4s.semconv.attributes" else ""

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
      "-v", s"$conventionsDir/model:/source",
      "-v", s"$buildDir/templates:/templates",
      "-v", s"$outputDir:/output",
      "--platform", "linux/amd64",
      s"otel/semconvgen:$generatorVersion",
      "--yaml-root", "/source",
      "--continue-on-validation-errors", "compatibility",
      "code",
      "--template", "/templates/SemanticAttributes.scala.j2",
      s"--output", s"/output/{{pascal_prefix}}${classPrefix}Attributes.scala",
      "--file-per-group", "root_namespace",
      // Space delimited list of root namespaces to excluded (i.e. "foo bar")
      "-Dexcluded_namespaces=\"ios aspnetcore signalr\"",
      "-Dexcluded_attributes=messaging.client_id",
      s"-Dfilter=$filter",
      s"-DclassPrefix=$classPrefix",
      s"-Dpkg=$packageNameArg",
      s"-DstablePkg=$stablePackageNameArg"
    )
    // format: on

    Process(command, rootDir).!
  }
}
