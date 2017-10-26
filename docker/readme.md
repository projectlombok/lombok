ARG lombokjar=lombok.jar

Example build commands:

docker build -t lombok-gradle-jdk9 -f gradle/Dockerfile .

docker build -t lombok-gradle-jdk9 --build-arg lombokjar=lombok-1.16.18.jar -f gradle/Dockerfile .


Example run commands:

docker run -it lombok-gradle-jdk9

docker run --rm -it -v /<lombokhome>/dist/lombok.jar:/workspace/lombok.jar lombok-gradle-jdk9

Example container commands:

gradle assemble
