apt-get update && apt-get install -y wget
wget https://github.com/AdoptOpenJDK/openjdk13-binaries/releases/download/jdk13u-2019-12-10-19-42/OpenJDK13U-jdk_x64_linux_hotspot_2019-12-10-19-42.tar.gz -O jdk.tar.gz
tar -xzf jdk.tar.gz -C /opt/
mv /opt/jdk-13.0.1+9 /opt/jdk