import lombok.extern.log4j.Log4j;

@lombok.extern.log4j.Log4j
class LoggerLog4j {
}

@Log4j
class LoggerLog4jWithImport {
}

@Log4j(topic="DifferentName")
class LoggerLog4jWithDifferentName {
}

@Log4j(isStatic=false)
class LoggerLog4jWithStatic {
}