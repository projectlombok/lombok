## Configuration

### `/workspace`

Each docker image contains a `/workspace` where all relevant files are located.


### `ARG lombokjar=lombok.jar`

When building the image, a lombok.jar will be downloaded to `/workspace/classpath` and `/workspace/modules`.
By default, this is the latest released version. You can download a specific version by adding `--build-arg lombokjar=lombok-<major.minor.build>.jar`

### `ARG jdk=21`

The jdk version to be used. Supported values:
- `22`(based on openjdk GA)
- `21` (default)(based on openjdk instead of adoptium)
- `17`
- `11`
- `8`

The version is also accessible in `JDK_VERSION`.


### Use fresh lombok.jar
If you want to use a lombok.jar from your system, assuming `<lombokhome>` contains the path to the lombok directory (where the .git subdirectory is located)
you can mount your recently built lombok.jar by providing `-v /<lombokhome>/dist/lombok.jar:/workspace/lombok.jar` to the `docker run` command.


## Examples

- [ant](ant/readme.md)
- [bazel](bazel/readme.md)
- [gradle](gradle/readme.md)
- [maven](maven/readme.md)
