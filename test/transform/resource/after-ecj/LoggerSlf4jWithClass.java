@lombok.slf4j.Log(String.class) class LoggerSlf4jWithClass {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(String.class);
  LoggerSlf4jWithClass() {
    super();
  }
}
@lombok.slf4j.Log(java.util.List.class) class LoggerSlf4jWithClassList {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(java.util.List.class);
  LoggerSlf4jWithClassList() {
    super();
  }
}
@lombok.slf4j.Log(value = java.lang.String.class) class LoggerSlf4jWithClassValue {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(java.lang.String.class);
  LoggerSlf4jWithClassValue() {
    super();
  }
}
@lombok.slf4j.Log(void.class) class LoggerSlf4jWithClassVoid {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggerSlf4jWithClassVoid.class);
  LoggerSlf4jWithClassVoid() {
    super();
  }
}
