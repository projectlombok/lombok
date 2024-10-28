FROM ubuntu:22.04 as downloader

ARG jdk=21
ADD provision/jdk/java-${jdk}.sh provision/jdk/java-${jdk}.sh
RUN provision/jdk/java-${jdk}.sh

ARG lombokjar=lombok.jar
ADD https://projectlombok.org/downloads/${lombokjar} /lombok.jar

ARG gradle=8.10.2
ADD provision/gradle/gradle.sh provision/gradle/gradle.sh
RUN provision/gradle/gradle.sh ${gradle}

FROM ubuntu:22.04

COPY --from=downloader /opt/gradle/ /opt/gradle/
COPY --from=downloader /opt/jdk/ /opt/jdk/

RUN update-alternatives  --install /usr/bin/java java /opt/jdk/bin/java 1000 && update-alternatives  --install /usr/bin/javac javac /opt/jdk/bin/javac 1000 && update-alternatives  --install /usr/bin/javadoc javadoc /opt/jdk/bin/javadoc 1000 && update-alternatives  --install /usr/bin/javap javap /opt/jdk/bin/javap 1000

WORKDIR workspace

ADD shared/ ./
ADD gradle/files/ ./

COPY --from=downloader /lombok.jar /workspace/lombok.jar

ARG jdk=21
ENV JDK_VERSION=${jdk}
ENV JAVA_HOME=/opt/jdk
ENV GRADLE_HOME=/opt/gradle/gradle
ENV PATH="${JAVA_HOME}/bin:${GRADLE_HOME}/bin:${PATH}"

ENTRYPOINT bash
