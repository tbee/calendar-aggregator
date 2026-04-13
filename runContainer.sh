export PROJECT_VERSION=`mvnw help:evaluate -Dexpression=project.version -q -DforceStdout`
echo PROJECT_VERSION=$PROJECT_VERSION
rm -rf app/src/main/container/data-volume
podman compose -f app/src/main/container/compose.yaml up
