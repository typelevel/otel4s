import sbt._
import scala.sys.process._

object SemanticConventionsGenerator {

  private val generatorVersion = "0.22.0"

  // generates semantic conventions by using `otel/semconvgen` in docker
  def generate(version: String, rootDir: File): Unit = {
    val semanticConventionsRepoZip =
      s"https://github.com/open-telemetry/semantic-conventions/archive/v$version.zip"

    val schemaUrl =
      s"https://opentelemetry.io/schemas/$version"

    val buildDir = rootDir / "buildscripts" / "semantic-convention"
    val zip = buildDir / "semantic-conventions.zip"
    val conventionsDir = buildDir / s"semantic-conventions-$version"

    IO.delete(zip)
    IO.delete(conventionsDir)

    (url(semanticConventionsRepoZip) #> zip).!

    IO.unzip(zip, buildDir)

    // format: off
    val genAttributes = List(
      "docker",
      "run",
      "--rm",
      "-v", s"$conventionsDir/model:/source",
      "-v", s"$buildDir/templates:/templates",
      "-v", s"$rootDir/semconv/src/main/scala/org/typelevel/otel4s/semconv/trace/attributes/:/output",
      "--platform", "linux/amd64",
      s"otel/semconvgen:$generatorVersion",
      "--only", "span,event,attribute_group,scope",
      "-f", "/source", "code",
      "--template", "/templates/SemanticAttributes.scala.j2",
      "--output", "/output/SemanticAttributes.scala",
      "-Dsemconv=trace",
      "-Dclass=SemanticAttributes",
      s"-DschemaUrl=$schemaUrl",
      "-Dpkg=org.typelevel.otel4s.semconv.trace.attributes"
    )

    val genResources = List(
      "docker",
      "run",
      "--rm",
      "-v", s"$conventionsDir/model:/source",
      "-v", s"$buildDir/templates:/templates",
      "-v", s"$rootDir/semconv/src/main/scala/org/typelevel/otel4s/semconv/resource/attributes/:/output",
      "--platform", "linux/amd64",
      s"otel/semconvgen:$generatorVersion",
      "--only", "resource",
      "-f", "/source", "code",
      "--template", "/templates/SemanticAttributes.scala.j2",
      "--output", "/output/ResourceAttributes.scala",
      "-Dsemconv=trace",
      "-Dclass=ResourceAttributes",
      s"-DschemaUrl=$schemaUrl",
      "-Dpkg=org.typelevel.otel4s.semconv.resource.attributes"
    )
    // format: on

    Process(genAttributes, rootDir).!
    Process(genResources, rootDir).!
  }
}
