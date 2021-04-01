FROM ubuntu:20.04 as downloader

ARG jdk=16
ADD provision/jdk/java-${jdk}.sh provision/jdk/java-${jdk}.sh
RUN provision/jdk/java-${jdk}.sh

ARG lombokjar=lombok.jar
ADD https://projectlombok.org/downloads/${lombokjar} /lombok.jar

ARG maven=3.6.3
ADD provision/maven/maven-${maven}.sh provision/maven/maven-${maven}.sh
RUN provision/maven/maven-${maven}.sh

FROM ubuntu:20.04

COPY --from=downloader /usr/local/apache-maven/ /usr/local/apache-maven/
COPY --from=downloader /opt/jdk/ /opt/jdk/

RUN update-alternatives  --install /usr/bin/java java /opt/jdk/bin/java 1000 && update-alternatives  --install /usr/bin/javac javac /opt/jdk/bin/javac 1000 && update-alternatives  --install /usr/bin/javadoc javadoc /opt/jdk/bin/javadoc 1000 && update-alternatives  --install /usr/bin/javap javap /opt/jdk/bin/javap 1000

WORKDIR workspace

ADD shared/ ./

ARG jdk=16
ADD maven/files/jdk-${jdk} ./

COPY --from=downloader /lombok.jar /workspace/classpath/lombok.jar
COPY --from=downloader /lombok.jar /workspace/modules/lombok.jar

ENV JDK_VERSION=${jdk}
ENV JAVA_HOME=/opt/jdk
ENV M2_HOME=/usr/local/apache-maven/apache-maven
ENV M2=${M2_HOME}/bin 
ENV PATH="${M2}:${JAVA_HOME}/bin:${PATH}"

ENTRYPOINT bash
