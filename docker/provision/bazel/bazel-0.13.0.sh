apt-get update && apt-get install -y wget pkg-config zip g++ zlib1g-dev unzip python
wget https://github.com/bazelbuild/bazel/releases/download/0.13.0/bazel-0.13.0-installer-linux-x86_64.sh  -O bazel-installer.sh
chmod +x bazel-installer.sh
./bazel-installer.sh --prefix=/opt/bazel