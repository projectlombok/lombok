import lombok.AccessLevel;
import lombok.extern.java.Log;

@Log(access = AccessLevel.PUBLIC)
class LoggerJulAccessPublic {
}

@Log(access = AccessLevel.MODULE)
class LoggerJulAccessModule {
}

@Log(access = AccessLevel.PROTECTED)
class LoggerJulAccessProtected {
}

@Log(access = AccessLevel.PACKAGE)
class LoggerJulAccessPackage {
}

@Log(access = AccessLevel.PRIVATE)
class LoggerJulAccessPrivate {
}

@Log(access = AccessLevel.NONE)
class LoggerJulAccessNone {
}
