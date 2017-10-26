FROM ubuntu:16.04 as downloader

RUN apt-get update && apt-get install wget -y
RUN wget --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/9.0.1+11/jdk-9.0.1_linux-x64_bin.tar.gz -O jdk9_linux-x64_bin.tar.gz
RUN tar -xzf jdk9_linux-x64_bin.tar.gz -C /opt/

RUN wget https://services.gradle.org/distributions/gradle-4.2.1-bin.zip -O gradle.zip
RUN apt-get update && apt-get install unzip -y
RUN mkdir /opt/gradle && unzip -d /opt/gradle gradle.zip

FROM ubuntu:16.04

COPY --from=downloader /opt/gradle/ /opt/gradle/
COPY --from=downloader /opt/jdk-9.0.1/ /opt/jdk-9.0.1/

RUN update-alternatives  --install /usr/bin/java java /opt/jdk-9.0.1/bin/java 1000 && update-alternatives  --install /usr/bin/javac javac /opt/jdk-9.0.1/bin/javac 1000 && update-alternatives  --install /usr/bin/javadoc javadoc /opt/jdk-9.0.1/bin/javadoc 1000 && update-alternatives  --install /usr/bin/javap javap /opt/jdk-9.0.1/bin/javap 1000

WORKDIR workspace

ADD shared/ ./
ADD gradle/files/ ./
ARG lombokjar=lombok.jar
ADD https://projectlombok.org/downloads/${lombokjar} lombok.jar

ENV JAVA_HOME=/opt/jdk-9.0.1
ENV GRADLE_HOME=/opt/gradle/gradle-4.2.1
ENV PATH="${JAVA_HOME}/bin:${GRADLE_HOME}/bin:${PATH}"

ENTRYPOINT bash
