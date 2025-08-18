import lombok.AccessLevel;
import lombok.extern.jbosslog.JBossLog;

@JBossLog(access = AccessLevel.PUBLIC)
class LoggerJBossLogAccessPublic {
}

@JBossLog(access = AccessLevel.MODULE)
class LoggerJBossLogAccessModule {
}

@JBossLog(access = AccessLevel.PROTECTED)
class LoggerJBossLogAccessProtected {
}

@JBossLog(access = AccessLevel.PACKAGE)
class LoggerJBossLogAccessPackage {
}

@JBossLog(access = AccessLevel.PRIVATE)
class LoggerJBossLogAccessPrivate {
}

@JBossLog(access = AccessLevel.NONE)
class LoggerJBossLogAccessNone {
}
