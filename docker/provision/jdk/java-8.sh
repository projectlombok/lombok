apt-get update && apt-get install -y software-properties-common
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections
add-apt-repository -y ppa:webupd8team/java
apt-get update && apt-get install -y oracle-java8-installer

mv /usr/lib/jvm/java-8-oracle /opt/jdk
