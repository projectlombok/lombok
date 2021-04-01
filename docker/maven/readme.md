## Configuration

[_(general configuration and options)_](../readme.md)

### `ARG maven=3.6.3`

The maven version to be used. Supported values:

- `3.6.3` (default)
- `3.6.1`
- `3.6.0`
- `3.5.0`

## Example build commands:

(To be executed from the `<lombokhome>/docker` directory)

```
docker build -t lombok-maven-jdk16 -f maven/Dockerfile .

docker build -t lombok-maven-jdk16 --build-arg lombokjar=lombok-1.18.20.jar -f maven/Dockerfile .
```

## Example run commands:

```
docker run -it lombok-maven-jdk16

docker run --rm -it -v /<lombokhome>/dist/lombok.jar:/workspace/lombok.jar lombok-maven-jdk16
```

## Example container commands:

```
cd classpath
mvn compile
```
