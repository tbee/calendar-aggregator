#!/usr/bin/env bash

# clean up stopped containers
podman compose rm

# toolchain takes care of the correct JVM
mvnw clean package -DskipTests -Pproduction -Pcontainer $*

