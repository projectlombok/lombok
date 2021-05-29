import lombok.ToString;
public class ToStringInAnonymousClass {
  Object annonymous = new Object() {
    @ToString class Inner {
      private String string;
      Inner() {
        super();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (("Inner(string=" + this.string) + ")");
      }
    }
    x() {
      super();
    }
  };
  public ToStringInAnonymousClass() {
    super();
  }
}
