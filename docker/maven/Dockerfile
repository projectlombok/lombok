FROM ubuntu:16.04 as downloader

RUN apt-get update && apt-get install wget -y
RUN wget --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/9.0.1+11/jdk-9.0.1_linux-x64_bin.tar.gz -O jdk9_linux-x64_bin.tar.gz
RUN tar -xzf jdk9_linux-x64_bin.tar.gz -C /opt/

RUN wget http://www-us.apache.org/dist/maven/maven-3/3.5.0/binaries/apache-maven-3.5.0-bin.tar.gz -O maven.tar.gz
RUN mkdir /usr/local/apache-maven/ && tar xvf maven.tar.gz -C /usr/local/apache-maven/

FROM ubuntu:16.04

COPY --from=downloader /usr/local/apache-maven/ /usr/local/apache-maven/
COPY --from=downloader /opt/jdk-9.0.1/ /opt/jdk-9.0.1/

RUN update-alternatives  --install /usr/bin/java java /opt/jdk-9.0.1/bin/java 1000 && update-alternatives  --install /usr/bin/javac javac /opt/jdk-9.0.1/bin/javac 1000 && update-alternatives  --install /usr/bin/javadoc javadoc /opt/jdk-9.0.1/bin/javadoc 1000 && update-alternatives  --install /usr/bin/javap javap /opt/jdk-9.0.1/bin/javap 1000

WORKDIR workspace

ADD shared/ ./
ADD maven/files/ ./
ARG lombokjar=lombok.jar
ADD https://projectlombok.org/downloads/${lombokjar} lombok.jar

ENV JAVA_HOME=/opt/jdk-9.0.1
ENV M2_HOME=/usr/local/apache-maven/apache-maven-3.5.0
ENV M2=${M2_HOME}/bin 
ENV PATH="${M2}:${JAVA_HOME}/bin:${PATH}"

ENTRYPOINT bash
