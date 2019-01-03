package testPackage;
class JavadocGenerally {
  public interface TestingInner {
  }
  private int someField;
  private @lombok.Getter @lombok.Setter int someBeanField;
  private @lombok.Getter int someGetterField;
  private @lombok.Setter int someSetterField;
  private @lombok.Getter @lombok.Setter int someGetterSetterField;
  JavadocGenerally() {
    super();
  }
  public void test() {
  }
  public @java.lang.SuppressWarnings("all") int getSomeBeanField() {
    return this.someBeanField;
  }
  public @java.lang.SuppressWarnings("all") void setSomeBeanField(final int someBeanField) {
    this.someBeanField = someBeanField;
  }
  public @java.lang.SuppressWarnings("all") int getSomeGetterField() {
    return this.someGetterField;
  }
  public @java.lang.SuppressWarnings("all") void setSomeSetterField(final int someSetterField) {
    this.someSetterField = someSetterField;
  }
  public @java.lang.SuppressWarnings("all") int getSomeGetterSetterField() {
    return this.someGetterSetterField;
  }
  public @java.lang.SuppressWarnings("all") void setSomeGetterSetterField(final int someGetterSetterField) {
    this.someGetterSetterField = someGetterSetterField;
  }
}
