## Configuration

[_(general configuration and options)_](../readme.md)

### `ARG bazel=2.0.0

The bazel version to be used. Supported values:

- `2.0.0` (default)
- `0.28.1`
- `0.13.0`

## Example build commands:

(To be executed from the `<lombokhome>/docker` directory)

```
docker build -t lombok-bazel-jdk13 -f bazel/Dockerfile .

docker build -t lombok-bazel-jdk13 --build-arg lombokjar=lombok-1.16.20.jar -f bazel/Dockerfile .
```

## Example run commands:

```
docker run -it lombok-bazel-jdk13

docker run --rm -it -v /<lombokhome>/dist/lombok.jar:/workspace/lombok.jar lombok-bazel-jdk13
```

## Example container commands:

```
bazel build //:ProjectRunner
```
