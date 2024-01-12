## Configuration

[_(general configuration and options)_](../readme.md)

### `ARG gradle=8.5`

The gradle version to be used. Supported values:

- `8.5` (default)
- `8.3`
- `7.6.1`
- `6.8.3`

## Example build commands:

(To be executed from the `<lombokhome>/docker` directory)

```
docker build -t lombok-gradle-jdk17 -f gradle/Dockerfile .

docker build -t lombok-gradle-jdk17 --build-arg lombokjar=lombok-1.18.28.jar -f gradle/Dockerfile .
```

## Example run commands:

```
docker run -it lombok-gradle-jdk17

docker run --rm -it -v /<lombokhome>/dist/lombok.jar:/workspace/classpath/lombok.jar lombok-gradle-jdk17
```

## Example container commands:

```
gradle assemble
```
