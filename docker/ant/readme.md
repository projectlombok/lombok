## Configuration

[_(general configuration and options)_](../readme.md)

### `ARG ant=1.10.1`

The ant version to be used. Supported values:

- `1.10.1` (default)

## Example build commands:

(To be executed from the `<lombokhome>/docker` directory)

```
docker build -t lombok-ant-jdk9 -f gradle/Dockerfile .

docker build -t lombok-ant-jdk9 --build-arg lombokjar=lombok-1.16.18.jar -f gradle/Dockerfile .
```

## Example run commands:

```
docker run -it lombok-ant-jdk9

docker run --rm -it -v /<lombokhome>/dist/lombok.jar:/workspace/lombok.jar lombok-ant-jdk9
```

## Example container commands:

```
ant dist
```
