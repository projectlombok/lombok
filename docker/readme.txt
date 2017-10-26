ARG lombokjar=lombok.jar

Example build commands:

docker build -t lombok-gradle-jdk9 -f gradle/Dockerfile .

docker build -t lombok-gradle-jdk9 --build-arg lombokjar=lombok-1.16.18.jar -f gradle/Dockerfile .

