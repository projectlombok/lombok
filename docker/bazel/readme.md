## Configuration

[_(general configuration and options)_](../readme.md)

### `ARG bazel=2.0.0

The bazel version to be used. Supported values:

- `2.0.0` (default)

## Example build commands:

(To be executed from the `<lombokhome>/docker` directory)

```
docker build -t lombok-bazel-jdk17 -f bazel/Dockerfile .

docker build -t lombok-bazel-jdk17 --build-arg lombokjar=lombok-1.18.28.jar -f bazel/Dockerfile .
```

## Example run commands:

```
docker run -it lombok-bazel-jdk17

docker run --rm -it -v /<lombokhome>/dist/lombok.jar:/workspace/lombok.jar lombok-bazel-jdk17
```

## Example container commands:

```
bazel build //:ProjectRunner
```
