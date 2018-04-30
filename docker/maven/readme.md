## Configuration

[_(general configuration and options)_](../readme.md)

### `ARG maven=3.5.0`

The maven version to be used. Supported values:

- `3.5.0` (default)

## Example build commands:

(To be executed from the `<lombokhome>/docker` directory)

```
docker build -t lombok-maven-jdk10 -f maven/Dockerfile .

docker build -t lombok-maven-jdk10 --build-arg lombokjar=lombok-1.16.20.jar -f maven/Dockerfile .
```

## Example run commands:

```
docker run -it lombok-maven-jdk10

docker run --rm -it -v /<lombokhome>/dist/lombok.jar:/workspace/lombok.jar lombok-maven-jdk10
```

## Example container commands:

```
mvn compile
```
