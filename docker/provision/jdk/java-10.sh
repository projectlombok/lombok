apt-get update && apt-get install -y wget
wget https://download.java.net/java/jdk10/archive/44/GPL/openjdk-10+44_linux-x64_bin.tar.gz -O jdk.tar.gz
tar -xzf jdk.tar.gz -C /opt/
mv /opt/jdk-10 /opt/jdk
