import lombok.extern.slf4j.XSlf4j;
@lombok.extern.slf4j.XSlf4j class LoggerXSlf4j {
  private static final @java.lang.SuppressWarnings("all") @lombok.Generated org.slf4j.ext.XLogger log = org.slf4j.ext.XLoggerFactory.getXLogger(LoggerXSlf4j.class);
  <clinit>() {
  }
  LoggerXSlf4j() {
    super();
  }
}
@XSlf4j class LoggerXSlf4jWithImport {
  private static final @java.lang.SuppressWarnings("all") @lombok.Generated org.slf4j.ext.XLogger log = org.slf4j.ext.XLoggerFactory.getXLogger(LoggerXSlf4jWithImport.class);
  <clinit>() {
  }
  LoggerXSlf4jWithImport() {
    super();
  }
}
@XSlf4j(topic = "DifferentName") class LoggerXSlf4jWithDifferentName {
  private static final @java.lang.SuppressWarnings("all") @lombok.Generated org.slf4j.ext.XLogger log = org.slf4j.ext.XLoggerFactory.getXLogger("DifferentName");
  <clinit>() {
  }
  LoggerXSlf4jWithDifferentName() {
    super();
  }
}
@XSlf4j(topic = LoggerXSlf4jWithStaticField.TOPIC) class LoggerXSlf4jWithStaticField {
  private static final @java.lang.SuppressWarnings("all") @lombok.Generated org.slf4j.ext.XLogger log = org.slf4j.ext.XLoggerFactory.getXLogger(LoggerXSlf4jWithStaticField.TOPIC);
  static final String TOPIC = "StaticField";
  <clinit>() {
  }
  LoggerXSlf4jWithStaticField() {
    super();
  }
}
