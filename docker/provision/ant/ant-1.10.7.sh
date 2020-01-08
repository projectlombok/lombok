apt-get update && apt-get install -y wget
wget https://archive.apache.org/dist/ant/binaries/apache-ant-1.10.7-bin.tar.gz -O ant.tar.gz
mkdir /usr/local/apache-ant/ && tar xvf ant.tar.gz -C /usr/local/apache-ant/
mv /usr/local/apache-ant/apache-ant-1.10.7 /usr/local/apache-ant/apache-ant