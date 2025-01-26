import lombok.extern.apachecommons.CommonsLog;
@lombok.extern.apachecommons.CommonsLog class LoggerCommons {
  private static final @java.lang.SuppressWarnings("all") @lombok.Generated org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(LoggerCommons.class);
  <clinit>() {
  }
  LoggerCommons() {
    super();
  }
}
@CommonsLog class LoggerCommonsWithImport {
  private static final @java.lang.SuppressWarnings("all") @lombok.Generated org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(LoggerCommonsWithImport.class);
  <clinit>() {
  }
  LoggerCommonsWithImport() {
    super();
  }
}
@CommonsLog(topic = "DifferentName") class LoggerCommonsWithDifferentName {
  private static final @java.lang.SuppressWarnings("all") @lombok.Generated org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog("DifferentName");
  <clinit>() {
  }
  LoggerCommonsWithDifferentName() {
    super();
  }
}
@CommonsLog(topic = LoggerCommonsWithStaticField.TOPIC) class LoggerCommonsWithStaticField {
  private static final @java.lang.SuppressWarnings("all") @lombok.Generated org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(LoggerCommonsWithStaticField.TOPIC);
  static final String TOPIC = "StaticField";
  <clinit>() {
  }
  LoggerCommonsWithStaticField() {
    super();
  }
}
