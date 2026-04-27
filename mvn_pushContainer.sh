#!/usr/bin/env bash

# clean up stopped containers
podman compose rm
podman container prune --force
podman pod prune --force

mvnw versions:set
mvnw clean install -DskipTests -Pcontainer

