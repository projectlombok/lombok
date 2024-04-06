apt-get update && apt-get install -y wget unzip
wget https://github.com/gradle/gradle-distributions/releases/download/v8.5.0/gradle-8.5-bin.zip -O gradle.zip
mkdir /opt/gradle && unzip -d /opt/gradle gradle.zip
mv /opt/gradle/gradle-8.5 /opt/gradle/gradle
