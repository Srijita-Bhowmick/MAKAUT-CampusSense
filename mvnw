#!/bin/sh
# ==============================================================================
# MAKAUT CampusSense - Cross-Platform Maven Wrapper (macOS & Linux)
# ==============================================================================

# 1. Auto-detect JAVA_HOME if not already exported
if [ -z "$JAVA_HOME" ] || [ ! -x "$JAVA_HOME/bin/java" ]; then
    if [ -d "/opt/homebrew/opt/openjdk@21" ]; then
        export JAVA_HOME="/opt/homebrew/opt/openjdk@21"
    elif [ -d "/opt/homebrew/opt/openjdk" ]; then
        export JAVA_HOME="/opt/homebrew/opt/openjdk"
    elif [ -x "/usr/libexec/java_home" ]; then
        DETECTED_JAVA="$(/usr/libexec/java_home -v 21 2>/dev/null || /usr/libexec/java_home 2>/dev/null)"
        if [ -n "$DETECTED_JAVA" ] && [ -d "$DETECTED_JAVA" ]; then
            export JAVA_HOME="$DETECTED_JAVA"
        fi
    elif [ -d "/usr/lib/jvm/java-21-openjdk" ]; then
        export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
    elif [ -d "/usr/lib/jvm/java-21-openjdk-amd64" ]; then
        export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"
    elif [ -d "/usr/lib/jvm/java-21-openjdk-arm64" ]; then
        export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-arm64"
    elif [ -d "/usr/lib/jvm/default-java" ]; then
        export JAVA_HOME="/usr/lib/jvm/default-java"
    fi
fi

if [ -n "$JAVA_HOME" ] && [ -d "$JAVA_HOME/bin" ]; then
    export PATH="$JAVA_HOME/bin:$PATH"
fi

# 2. Locate Maven executable (bundled apache-maven-3.9.9 or system mvn)
DIRNAME="$(cd "$(dirname "$0")" && pwd)"
if [ -x "$DIRNAME/apache-maven-3.9.9/bin/mvn" ]; then
    exec "$DIRNAME/apache-maven-3.9.9/bin/mvn" "$@"
elif [ -f "$DIRNAME/apache-maven-3.9.9/bin/mvn" ]; then
    chmod +x "$DIRNAME/apache-maven-3.9.9/bin/mvn" 2>/dev/null || true
    exec "$DIRNAME/apache-maven-3.9.9/bin/mvn" "$@"
elif command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
else
    echo "[ERROR] Maven executable not found! Neither bundled apache-maven-3.9.9 nor global mvn was located." >&2
    echo "Please install Maven or ensure Java 21+ and Maven are available on your PATH." >&2
    exit 1
fi
