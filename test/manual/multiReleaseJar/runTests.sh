#!/bin/bash
set -euo pipefail
echo 'This will build a project with a multi release jar dependency. If the compilation works without error or warning, lombok is working as designed.'
mkdir -p out/javac

# We cannot use the link because javac can infer the module name
JAR=$(realpath ../../../lib/test/org.jetbrains-annotations.jar)
LOMBOK_JAR=$(realpath ../../../dist/lombok.jar)

javac --processor-path $LOMBOK_JAR -p $LOMBOK_JAR:$JAR -d out/javac src/module-info.java src/pkg/MultiReleaseJarTest.java

echo Now we try to delombok and see if it works as designed.

java -jar $LOMBOK_JAR delombok --module-path $JAR -d out/delombok src
