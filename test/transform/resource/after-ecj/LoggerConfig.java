@lombok.extern.slf4j.Slf4j class LoggerWithConfig {
  private final org.slf4j.Logger myLogger = org.slf4j.LoggerFactory.getLogger(LoggerWithConfig.class);
  LoggerWithConfig() {
    super();
  }
}