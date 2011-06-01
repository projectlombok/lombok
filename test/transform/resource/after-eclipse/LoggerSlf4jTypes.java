@lombok.extern.slf4j.Slf4j interface LoggerSlf4jTypesInterface {
}
@lombok.extern.slf4j.Slf4j @interface LoggerSlf4jTypesAnnotation {
}
@lombok.extern.slf4j.Slf4j enum LoggerSlf4jTypesEnum {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggerSlf4jTypesEnum.class);
  <clinit>() {
  }
  LoggerSlf4jTypesEnum() {
    super();
  }
}
@lombok.extern.slf4j.Slf4j enum LoggerSlf4jTypesEnumWithElement {
  FOO(),
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggerSlf4jTypesEnumWithElement.class);
  <clinit>() {
  }
  LoggerSlf4jTypesEnumWithElement() {
    super();
  }
}
interface LoggerSlf4jTypesInterfaceOuter {
  @lombok.extern.slf4j.Slf4j class Inner {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Inner.class);
    <clinit>() {
    }
    Inner() {
      super();
    }
  }
}