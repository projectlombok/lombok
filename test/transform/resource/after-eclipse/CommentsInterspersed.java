import lombok.Getter;
public class CommentsInterspersed {
  private int x;
  private @Getter String test = "foo";
  public @java.lang.SuppressWarnings("all") String getTest() {
    return this.test;
  }
  public CommentsInterspersed() {
    super();
  }
  public native void gwtTest();
}
