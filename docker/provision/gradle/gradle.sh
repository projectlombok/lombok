apt-get update && apt-get install -y wget unzip
wget https://github.com/gradle/gradle-distributions/releases/download/v$1/gradle-$1-bin.zip -O gradle.zip
mkdir /opt/gradle && unzip -d /opt/gradle gradle.zip
mv /opt/gradle/gradle-$1 /opt/gradle/gradle
