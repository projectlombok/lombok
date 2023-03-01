apt-get update && apt-get install -y wget
wget --progress=bar:force https://github.com/AdoptOpenJDK/openjdk9-binaries/releases/download/jdk-9.0.4%2B11/OpenJDK9U-jdk_x64_linux_hotspot_9.0.4_11.tar.gz -O jdk.tar.gz
tar -xzf jdk.tar.gz -C /opt/
mv /opt/jdk-9.0.4+11 /opt/jdk
/opt/jdk/bin/keytool -importcert -trustcacerts -cacerts -file /etc/ssl/certs/GlobalSign_Root_CA_-_R3.pem -storepass changeit -noprompt -alias globalsignrootca-r3