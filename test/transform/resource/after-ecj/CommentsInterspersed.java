import lombok.Getter;
public class CommentsInterspersed {
  private int x;
  private @Getter String test = "foo";
  public CommentsInterspersed() {
    super();
  }
  public native void gwtTest();
  public @java.lang.SuppressWarnings("all") @lombok.Generated String getTest() {
    return this.test;
  }
}
