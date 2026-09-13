#!/bin/sh
DIRNAME="$(dirname "$0")"
if [ -f "$DIRNAME/apache-maven-3.9.9/bin/mvn" ]; then
    exec "$DIRNAME/apache-maven-3.9.9/bin/mvn" "$@"
else
    exec mvn "$@"
fi
