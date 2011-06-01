import lombok.extern.apachecommons.CommonsLog;
@lombok.extern.apachecommons.CommonsLog class LoggerCommons {
  private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(LoggerCommons.class);
  <clinit>() {
  }
  LoggerCommons() {
    super();
  }
}
@CommonsLog class LoggerCommonsWithImport {
  private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(LoggerCommonsWithImport.class);
  <clinit>() {
  }
  LoggerCommonsWithImport() {
    super();
  }
}