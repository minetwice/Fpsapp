#!/bin/sh

APP_HOME=$(cd "$(dirname "$0")" && pwd)
APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

# Add default JVM options
DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"

# Combine JVM options
JVM_OPTS="$DEFAULT_JVM_OPTS"
if [ -n "$JAVA_OPTS" ]; then
    JVM_OPTS="$JVM_OPTS $JAVA_OPTS"
fi
if [ -n "$GRADLE_OPTS" ]; then
    JVM_OPTS="$GRADLE_OPTS $JVM_OPTS"
fi

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Find Java
if [ -n "$JAVA_HOME" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    JAVA_CMD="java"
fi

# Execute
exec "$JAVA_CMD" $JVM_OPTS -cp "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
