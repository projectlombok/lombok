import lombok.AccessLevel;
import lombok.extern.slf4j.XSlf4j;

@XSlf4j(access = AccessLevel.PUBLIC)
class LoggerXslf4jAccessPublic {
}

@XSlf4j(access = AccessLevel.MODULE)
class LoggerXslf4jAccessModule {
}

@XSlf4j(access = AccessLevel.PROTECTED)
class LoggerXslf4jAccessProtected {
}

@XSlf4j(access = AccessLevel.PACKAGE)
class LoggerXslf4jAccessPackage {
}

@XSlf4j(access = AccessLevel.PRIVATE)
class LoggerXslf4jAccessPrivate {
}

@XSlf4j(access = AccessLevel.NONE)
class LoggerXslf4jAccessNone {
}
