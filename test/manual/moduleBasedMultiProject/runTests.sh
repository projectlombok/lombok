#!/bin/bash
set -euo pipefail
echo 'This will build, module-style, 2 modules with lombok dependencies. If the compilation works without error or warning, lombok is working as designed.'
mkdir -p out/projA
mkdir -p out/projB
javac --processor-path ../../../dist/lombok.jar -p ../../../dist/lombok.jar -d out/projA projA/module-info.java projA/pkgA/ClassA.java
javac --processor-path ../../../dist/lombok.jar -p ../../../dist/lombok.jar:out/projA -d out/projB projB/module-info.java projB/pkgB/ClassB.java

echo Now we try to delombok and see if it works as designed.

java -jar ../../../dist/lombok.jar delombok -p --module-path out/projA projB/pkgB/ClassB.java projB/module-info.java
