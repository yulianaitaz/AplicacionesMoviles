#!/bin/sh

APP_HOME=$(cd "${0%/*}" && pwd -P)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_URL="https://services.gradle.org/distributions/gradle-9.6.0-wrapper.jar"

# El entorno de esta entrega no permite adjuntar binarios de terceros.
# Si falta el JAR oficial del wrapper, se obtiene automáticamente en el primer uso.
if [ ! -f "$CLASSPATH" ]; then
    mkdir -p "$(dirname "$CLASSPATH")"
    if command -v curl >/dev/null 2>&1; then
        curl -fL "$WRAPPER_URL" -o "$CLASSPATH" || exit 1
    elif command -v wget >/dev/null 2>&1; then
        wget -O "$CLASSPATH" "$WRAPPER_URL" || exit 1
    else
        echo "No se encontro gradle-wrapper.jar y tampoco curl/wget para descargarlo." >&2
        exit 1
    fi
fi

if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD=java
fi

exec "$JAVACMD" -Xmx64m -Xms64m -Dorg.gradle.appname=gradlew -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
