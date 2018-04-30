FROM ubuntu:16.04 as downloader

ARG jdk=10
ADD provision/jdk/java-${jdk}.sh provision/jdk/java-${jdk}.sh
RUN provision/jdk/java-${jdk}.sh

ARG bazel=0.13.0
ADD provision/bazel/bazel-${bazel}.sh provision/bazel/bazel-${bazel}.sh
RUN provision/bazel/bazel-${bazel}.sh

FROM ubuntu:16.04

COPY --from=downloader /opt/bazel/ /opt/bazel/
COPY --from=downloader /opt/jdk/ /opt/jdk/

RUN update-alternatives  --install /usr/bin/java java /opt/jdk/bin/java 1000 && update-alternatives  --install /usr/bin/javac javac /opt/jdk/bin/javac 1000 && update-alternatives  --install /usr/bin/javadoc javadoc /opt/jdk/bin/javadoc 1000 && update-alternatives  --install /usr/bin/javap javap /opt/jdk/bin/javap 1000
RUN apt-get update && apt-get install -y g++

WORKDIR workspace

ADD shared/ ./
ADD bazel/files/ ./
ARG lombokjar=lombok.jar
ADD https://projectlombok.org/downloads/${lombokjar} lombok.jar

ARG jdk=10
ENV JDK_VERSION=${jdk}
ENV JAVA_HOME=/opt/jdk
ENV BAZEL_HOME=/opt/bazel
ENV PATH="${JAVA_HOME}/bin:${BAZEL_HOME}/bin:${PATH}"

ENTRYPOINT bash
