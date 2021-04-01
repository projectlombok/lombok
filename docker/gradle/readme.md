## Configuration

[_(general configuration and options)_](../readme.md)

### `ARG gradle=6.2.2`

The gradle version to be used. Supported values:

- `6.8.3` (default)
- `6.2.2`
- `6.0.1`
- `5.6`
- `5.1.1`
- `4.10.2`
- `4.7`
- `4.2.1`

## Example build commands:

(To be executed from the `<lombokhome>/docker` directory)

```
docker build -t lombok-gradle-jdk16 -f gradle/Dockerfile .

docker build -t lombok-gradle-jdk16 --build-arg lombokjar=lombok-1.18.20.jar -f gradle/Dockerfile .
```

## Example run commands:

```
docker run -it lombok-gradle-jdk16

docker run --rm -it -v /<lombokhome>/dist/lombok.jar:/workspace/classpath/lombok.jar lombok-gradle-jdk16
```

## Example container commands:

```
gradle assemble
```
