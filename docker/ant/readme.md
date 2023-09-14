## Configuration

[_(general configuration and options)_](../readme.md)

### `ARG ant=1.10.9`

The ant version to be used. Supported values:

- `1.10.9` (default)
- `1.10.7`
- `1.10.6`
- `1.10.1`

## Example build commands:

(To be executed from the `<lombokhome>/docker` directory)

```
docker build -t lombok-ant-jdk17 -f ant/Dockerfile .

docker build -t lombok-ant-jdk17 --build-arg lombokjar=lombok-1.18.20.jar -f ant/Dockerfile .
```

## Example run commands:

```
docker run -it lombok-ant-jdk17

docker run --rm -it -v /<lombokhome>/dist/lombok.jar:/workspace/lombok.jar lombok-ant-jdk17
```

## Example container commands:

```
cd classpath
ant dist
```
