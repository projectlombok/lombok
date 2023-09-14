apt-get update && apt-get install -y wget
wget https://download.java.net/java/GA/jdk21/fd2272bbf8e04c3dbaee13770090416c/35/GPL/openjdk-21_linux-x64_bin.tar.gz -O jdk.tar.gz
tar -xzf jdk.tar.gz -C /opt/
mv /opt/jdk-21 /opt/jdk