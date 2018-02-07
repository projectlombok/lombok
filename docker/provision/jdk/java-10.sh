apt-get update && apt-get install -y wget
wget https://download.java.net/java/jdk10/archive/42/BCL/jdk-10-ea+42_linux-x64_bin.tar.gz -O jdk.tar.gz
tar -xzf jdk.tar.gz -C /opt
mv /opt/jdk-10 /opt/jdk
