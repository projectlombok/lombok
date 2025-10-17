apt-get update && apt-get install -y wget
wget https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25%2B36/OpenJDK25U-jdk_x64_linux_hotspot_25_36.tar.gz -O jdk.tar.gz
tar -xzf jdk.tar.gz -C /opt/
mv /opt/jdk-25+36 /opt/jdk