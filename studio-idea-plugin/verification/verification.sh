#!/bin/bash

# IDE versions that needs to be validated with plugin
IDE_VERSIONS=(2019.2.4 2019.3.4 2020.1)

# Verifier library version, see https://github.com/JetBrains/intellij-plugin-verifier
VERIFIER_VERSION=1.223

# OpenJDK 11 home directory
JAVA_RUNTIME_DIR=/Library/Java/JavaVirtualMachines/openjdk-11.0.2.jdk/Contents/Home

# MidPoint Studio plugin version
STUDIO_PLUGIN_VERSION=4.0.0

################################################################################################

CURRENT_DIR=$(pwd)

echo "Building plugin"

cd $CURRENT_DIR/..

../gradlew buildPlugin

cd $CURRENT_DIR

echo "Downloading IDEs"

IDE_DIRECTORY=$CURRENT_DIR/ide
mkdir -p $IDE_DIRECTORY

for i in "${IDE_VERSIONS[@]}"
do
  mkdir -p $IDE_DIRECTORY/$i

  IDE_NAME=$IDE_DIRECTORY/$i/ideaIC.zip

  if [ -f "$IDE_NAME" ]; then
    echo "$IDE_NAME exist"
  else
      echo "$IDE_NAME does not exist, downloading"

      wget -O $IDE_NAME https://www.jetbrains.com/intellij-repository/releases/com/jetbrains/intellij/idea/ideaIC/$i/ideaIC-$i.zip

      echo "Unzipping $IDE_NAME"

      unzip -q -d $IDE_DIRECTORY/$i/ideaIC $IDE_NAME
  fi
done

echo "Preparing verifier"

VERIFIER_FILE=$CURRENT_DIR/verifier-all.jar
if [ -f "$VERIFIER_FILE" ]; then
    echo "$VERIFIER_FILE exist"
else
    echo "$VERIFIER_FILE does not exist, downloading"
    wget -O $VERIFIER_FILE https://dl.bintray.com/jetbrains/intellij-plugin-service/org/jetbrains/intellij/plugins/verifier-cli/$VERIFIER_VERSION/verifier-cli-$VERIFIER_VERSION-all.jar
fi

echo "Executing verifier"

IDE_PATHS=""
for i in "${IDE_VERSIONS[@]}"
do
  IDE_PATHS="$IDE_PATHS $IDE_DIRECTORY/$i/ideaIC"
done

echo "Validating against $IDE_PATHS"

java -jar $VERIFIER_FILE check-plugin \
$CURRENT_DIR/../build/distributions/studio-idea-plugin-$STUDIO_PLUGIN_VERSION.zip \
$IDE_PATHS \
-runtime-dir $JAVA_RUNTIME_DIR
