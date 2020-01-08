## Configuration

[_(general configuration and options)_](../readme.md)

### `ARG ant=1.10.7`

The ant version to be used. Supported values:

- `1.10.7` (default)
- `1.10.6`
- `1.10.1`

## Example build commands:

(To be executed from the `<lombokhome>/docker` directory)

```
docker build -t lombok-ant-jdk13 -f ant/Dockerfile .

docker build -t lombok-ant-jdk13 --build-arg lombokjar=lombok-1.16.20.jar -f ant/Dockerfile .
```

## Example run commands:

```
docker run -it lombok-ant-jdk13

docker run --rm -it -v /<lombokhome>/dist/lombok.jar:/workspace/lombok.jar lombok-ant-jdk13
```

## Example container commands:

```
ant dist
```
