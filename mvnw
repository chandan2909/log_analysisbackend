#!/bin/sh
# Maven Wrapper script
# Downloads and runs Maven if not already installed

set -e

MAVEN_VERSION="3.9.6"
MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-${MAVEN_VERSION}"
MAVEN_BIN="$MAVEN_HOME/bin/mvn"

if [ ! -f "$MAVEN_BIN" ]; then
    echo "Downloading Maven ${MAVEN_VERSION}..."
    mkdir -p "$MAVEN_HOME"
    DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.zip"
    
    if command -v curl > /dev/null 2>&1; then
        curl -fsSL "$DOWNLOAD_URL" -o "/tmp/maven-${MAVEN_VERSION}.zip"
    elif command -v wget > /dev/null 2>&1; then
        wget -q "$DOWNLOAD_URL" -O "/tmp/maven-${MAVEN_VERSION}.zip"
    else
        echo "Error: curl or wget is required to download Maven"
        exit 1
    fi
    
    unzip -qo "/tmp/maven-${MAVEN_VERSION}.zip" -d "$HOME/.m2/wrapper/dists/"
    rm -f "/tmp/maven-${MAVEN_VERSION}.zip"
    echo "Maven ${MAVEN_VERSION} installed."
fi

export MAVEN_HOME
export PATH="$MAVEN_HOME/bin:$PATH"

exec mvn "$@"
