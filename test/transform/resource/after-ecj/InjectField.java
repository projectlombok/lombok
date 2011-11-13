import java.util.logging.Level;
import lombok.extern.java.Log;
import lombok.Synchronized;
@Log enum InjectField1 {
  A(),
  B(),
  private final java.lang.Object $lock = new java.lang.Object[0];
  private static final java.lang.Object $LOCK = new java.lang.Object[0];
  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(InjectField1.class.getName());
  private static final String LOG_MESSAGE = "static initializer";
  private String fieldA;
  static {
    log.log(Level.FINE, LOG_MESSAGE);
  }
  private String fieldB;
  <clinit>() {
  }
  InjectField1() {
    super();
  }
  @Synchronized void generateLockField() {
    synchronized (this.$lock)
      {
        System.out.println("lock field");
      }
  }
  static @Synchronized void generateStaticLockField() {
    synchronized (InjectField1.$LOCK)
      {
        System.out.println("static lock field");
      }
  }
}
@Log class InjectField2 {
  private final java.lang.Object $lock = new java.lang.Object[0];
  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(InjectField2.class.getName());
  private static final String LOG_MESSAGE = "static initializer";
  static {
    log.log(Level.FINE, LOG_MESSAGE);
  }
  <clinit>() {
  }
  InjectField2() {
    super();
  }
  @Synchronized void generateLockField() {
    synchronized (this.$lock)
      {
        System.out.println("lock field");
      }
  }
}
@Log class InjectField3 {
  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(InjectField3.class.getName());
  static {
    log.log(Level.FINE, "static initializer");
  }
  <clinit>() {
  }
  InjectField3() {
    super();
  }
}
