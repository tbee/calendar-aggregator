#!/usr/bin/env bash

# clean up stopped containers
podman container prune --force
podman pod prune --force
# toolchain takes care of the correct JVM
# not doing clean does not reduce the build time much
mvnw clean package -DskipTests -Pproduction -Pcontainer $*

