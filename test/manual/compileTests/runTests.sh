#!/bin/sh
set -e
echo 'This will compile a selection of files in test/transform/resource/before. If the compilation works without error, lombok is working as designed.'

mkdir -p out/

# list of files to iterate over
FILES="UtilityClassInner.java"
BASE_PATH="../../../test/transform/resource/before"
LOMBOK_JAR="../../../dist/lombok.jar"

# compile all files in list
for f in $FILES
do
  
  echo "Compiling $f"
  javac -processorpath $LOMBOK_JAR -cp $LOMBOK_JAR -d out/ $BASE_PATH/$f
done

rm -rf out/
