## Configuration

[_(general configuration and options)_](../readme.md)

### `ARG ant=1.10.6`

The ant version to be used. Supported values:

- `1.10.6` (default)
- `1.10.1` (default)

## Example build commands:

(To be executed from the `<lombokhome>/docker` directory)

```
docker build -t lombok-ant-jdk12 -f ant/Dockerfile .

docker build -t lombok-ant-jdk12 --build-arg lombokjar=lombok-1.16.20.jar -f ant/Dockerfile .
```

## Example run commands:

```
docker run -it lombok-ant-jdk12

docker run --rm -it -v /<lombokhome>/dist/lombok.jar:/workspace/lombok.jar lombok-ant-jdk12
```

## Example container commands:

```
ant dist
```
