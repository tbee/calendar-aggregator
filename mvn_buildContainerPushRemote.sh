#!/usr/bin/env bash

mvnw versions:set
mvnw clean install -DskipTests -Pproduction -Pcontainer -Pcontainer-remote