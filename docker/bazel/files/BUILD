java_binary(
    name = "ProjectRunner",
    main_class = "HelloWorld",
    srcs = glob(["src/main/java/*.java"]),
    deps = [":lombok"],
)

java_plugin(
    name = "lombok_plugin",
    processor_class = "lombok.launch.AnnotationProcessorHider$AnnotationProcessor",
    deps = ["@lombok_jar//:jar"],
)

java_library(
    name = "lombok",
    exports = ["@lombok_jar//:jar"],
    exported_plugins = [":lombok_plugin"],
)