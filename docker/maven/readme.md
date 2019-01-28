## Configuration

[_(general configuration and options)_](../readme.md)

### `ARG maven=3.6.0`

The maven version to be used. Supported values:

- `3.6.0` (default)
- `3.5.0`

## Example build commands:

(To be executed from the `<lombokhome>/docker` directory)

```
docker build -t lombok-maven-jdk11 -f maven/Dockerfile .

docker build -t lombok-maven-jdk11 --build-arg lombokjar=lombok-1.16.20.jar -f maven/Dockerfile .
```

## Example run commands:

```
docker run -it lombok-maven-jdk11

docker run --rm -it -v /<lombokhome>/dist/lombok.jar:/workspace/lombok.jar lombok-maven-jdk11
```

## Example container commands:

```
mvn compile
```
