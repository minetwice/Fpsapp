#!/bin/sh

# Gradle wrapper script - Simplified but correct

APP_HOME=$(cd "$(dirname "$0")" && pwd)
DEFAULT_JVM_OPTS=""
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Find java command
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

# Pass JVM options from environment if set
if [ -n "$GRADLE_OPTS" ]; then
    JVM_OPTS="$GRADLE_OPTS"
else
    JVM_OPTS="$DEFAULT_JVM_OPTS"
fi

# Execute
exec "$JAVACMD" $JVM_OPTS -cp "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
