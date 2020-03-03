#!/bin/bash

CURRENT_DIR=$(pwd)

echo "Building plugin"

cd $CURRENT_DIR/..

../gradlew buildPlugin

cd $CURRENT_DIR

echo "Downloading IDEs"

IDE_VERSION=(2019.1.4 2019.2.4 2019.3.3)

IDE_DIRECTORY=$CURRENT_DIR/ide
mkdir -p $IDE_DIRECTORY

for i in "${IDE_VERSION[@]}"
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

VERIFIER_VERSION=1.223

VERIFIER_FILE=$CURRENT_DIR/verifier-all.jar
if [ -f "$VERIFIER_FILE" ]; then
    echo "$VERIFIER_FILE exist"
else
    echo "$VERIFIER_FILE does not exist, downloading"
    wget -O $VERIFIER_FILE https://dl.bintray.com/jetbrains/intellij-plugin-service/org/jetbrains/intellij/plugins/verifier-cli/$VERIFIER_VERSION/verifier-cli-$VERIFIER_VERSION-all.jar
fi

JAVA_RUNTIME_DIR=/Library/Java/JavaVirtualMachines/openjdk-11.0.2.jdk/Contents/Home

echo "Executing verifier"

IDE_PATHS=""
for i in "${IDE_VERSION[@]}"
do
  IDE_PATHS="$IDE_PATHS $IDE_DIRECTORY/$i/ideaIC"
done

echo "Validating against $IDE_PATHS"

java -jar $VERIFIER_FILE check-plugin \
$CURRENT_DIR/../build/distributions/studio-idea-plugin-4.0.0.zip \
$IDE_PATHS \
-runtime-dir $JAVA_RUNTIME_DIR
