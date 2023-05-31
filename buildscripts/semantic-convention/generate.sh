#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT_DIR="${SCRIPT_DIR}/../../"

# freeze the spec & generator tools versions to make SemanticAttributes generation reproducible
SEMCONV_VERSION=1.19.0
SPEC_VERSION=v$SEMCONV_VERSION
SCHEMA_URL=https://opentelemetry.io/schemas/$SEMCONV_VERSION
GENERATOR_VERSION=0.18.0

cd "${SCRIPT_DIR}" || exit

rm -rf opentelemetry-specification || true
mkdir opentelemetry-specification
cd opentelemetry-specification || exit

git init
git remote add origin https://github.com/open-telemetry/opentelemetry-specification.git
git fetch origin "$SPEC_VERSION"
git reset --hard FETCH_HEAD
cd "${SCRIPT_DIR}" || exit

docker run --rm \
  -v ${SCRIPT_DIR}/opentelemetry-specification/semantic_conventions:/source \
  -v ${SCRIPT_DIR}/templates:/templates \
  -v ${ROOT_DIR}/semconv/src/main/scala/org/typelevel/otel4s/semconv/trace/attributes/:/output \
  otel/semconvgen:$GENERATOR_VERSION \
  --only span,event,attribute_group,scope \
  -f /source code \
  --template /templates/SemanticAttributes.scala.j2 \
  --output /output/SemanticAttributes.scala \
  -Dsemconv=trace \
  -Dclass=SemanticAttributes \
  -DschemaUrl=$SCHEMA_URL \
  -Dpkg=org.typelevel.otel4s.semconv.trace.attributes

docker run --rm \
  -v ${SCRIPT_DIR}/opentelemetry-specification/semantic_conventions:/source \
  -v ${SCRIPT_DIR}/templates:/templates \
  -v ${ROOT_DIR}/semconv/src/main/scala/org/typelevel/otel4s/semconv/resource/attributes/:/output \
  otel/semconvgen:$GENERATOR_VERSION \
  --only resource \
  -f /source code \
  --template /templates/SemanticAttributes.scala.j2 \
  --output /output/ResourceAttributes.scala \
  -Dclass=ResourceAttributes \
  -DschemaUrl=$SCHEMA_URL \
  -Dpkg=org.typelevel.otel4s.semconv.resource.attributes

cd "$ROOT_DIR" || exit
sbt scalafixAll scalafmtAll