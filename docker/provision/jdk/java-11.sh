apt-get update && apt-get install -y wget
wget https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_linux-x64_bin.tar.gz -O jdk.tar.gz
tar -xzf jdk.tar.gz -C /opt/
mv /opt/jdk-11.0.2 /opt/jdk