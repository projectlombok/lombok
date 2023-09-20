apt-get update && apt-get install -y wget
wget https://download.java.net/java/early_access/jdk22/15/GPL/openjdk-22-ea+15_linux-x64_bin.tar.gz -O jdk.tar.gz
tar -xzf jdk.tar.gz -C /opt/
mv /opt/jdk-22 /opt/jdk