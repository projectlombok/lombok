apt-get update && apt-get install -y wget
wget https://download.java.net/java/GA/jdk23/3c5b90190c68498b986a97f276efd28a/37/GPL/openjdk-23_linux-x64_bin.tar.gz -O jdk.tar.gz
tar -xzf jdk.tar.gz -C /opt/
mv /opt/jdk-23 /opt/jdk
