#!/usr/bin/env bash

# clean up stopped containers
podman compose rm
podman container prune --force
podman pod prune --force

# toolchain takes care of the correct JVM
mvnw clean package -DskipTests -Pproduction -Pcontainer $*

