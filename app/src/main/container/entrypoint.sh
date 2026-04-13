#!/bin/sh
set -eu

# Initialize /data from immutable image defaults on first boot only.
if [ -z "$(find /data -mindepth 1 -print -quit)" ]; then
  cp -R /data-init/. /data/
  mkdir -p /data/backup
  mkdir -p /data/logs

  if [ -f /data-init/hsqldb.tar.gz ]; then
    mkdir -p /data/hsqldb
    tar -xzmf /data-init/hsqldb.tar.gz -C /data/hsqldb
    rm -f /data/hsqldb.tar.gz
  fi
fi

exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar /app/app.jar "$@"

