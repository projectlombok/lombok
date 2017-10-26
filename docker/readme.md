## Configuration

### `/workspace`

Each docker image contains a `/workspace` where all relevant files are located.


### `ARG lombokjar=lombok.jar`

When building the image, a lombok.jar will be downloaded to `/workspace`. By default, this is the latest released version. You
can download a specific version by adding `--build-arg lombokjar=lombok-<major.minor.build>.jar`


### Use fresh lombok.jar
If you want to use a lombok.jar from your system, assuming `<lombokhome>` contains the path to the lombok directory (where the .git subdirectory is located)
you can mount your recently built lombok.jar by providing `-v /<lombokhome>/dist/lombok.jar:/workspace/lombok.jar` to the `docker run` command.


## Example build commands:

```
docker build -t lombok-gradle-jdk9 -f gradle/Dockerfile .

docker build -t lombok-gradle-jdk9 --build-arg lombokjar=lombok-1.16.18.jar -f gradle/Dockerfile .
```

## Example run commands:

```
docker run -it lombok-gradle-jdk9

docker run --rm -it -v /<lombokhome>/dist/lombok.jar:/workspace/lombok.jar lombok-gradle-jdk9
```

## Example container commands:

```
gradle assemble
```
