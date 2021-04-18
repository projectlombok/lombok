apt-get update && apt-get install -y wget unzip
wget https://services.gradle.org/distributions/gradle-6.0.1-bin.zip -O gradle.zip
mkdir /opt/gradle && unzip -d /opt/gradle gradle.zip
mv /opt/gradle/gradle-6.0.1 /opt/gradle/gradle
