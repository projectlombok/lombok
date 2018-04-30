## Configuration

[_(general configuration and options)_](../readme.md)

### `ARG gradle=4.7`

The gradle version to be used. Supported values:

- `4.7` (default)
- `4.2.1`

## Example build commands:

(To be executed from the `<lombokhome>/docker` directory)

```
docker build -t lombok-gradle-jdk10 -f gradle/Dockerfile .

docker build -t lombok-gradle-jdk10 --build-arg lombokjar=lombok-1.16.20.jar -f gradle/Dockerfile .
```

## Example run commands:

```
docker run -it lombok-gradle-jdk10

docker run --rm -it -v /<lombokhome>/dist/lombok.jar:/workspace/lombok.jar lombok-gradle-jdk10
```

## Example container commands:

```
gradle assemble
```
