import lombok.AccessLevel;
import lombok.extern.apachecommons.CommonsLog;

@CommonsLog(access = AccessLevel.PUBLIC)
class LoggerCommonsAccessPublic {
}

@CommonsLog(access = AccessLevel.MODULE)
class LoggerCommonsAccessModule {
}

@CommonsLog(access = AccessLevel.PROTECTED)
class LoggerCommonsAccessProtected {
}

@CommonsLog(access = AccessLevel.PACKAGE)
class LoggerCommonsAccessPackage {
}

@CommonsLog(access = AccessLevel.PRIVATE)
class LoggerCommonsAccessPrivate {
}

@CommonsLog(access = AccessLevel.NONE)
class LoggerCommonsAccessNone {
}
