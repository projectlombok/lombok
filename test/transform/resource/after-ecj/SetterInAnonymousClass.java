import lombok.Setter;
public class SetterInAnonymousClass {
  Object annonymous = new Object() {
    @Setter class Inner {
      private String string;
      Inner() {
        super();
      }
      public @java.lang.SuppressWarnings("all") void setString(final String string) {
        this.string = string;
      }
    }
    x() {
      super();
    }
  };
  public SetterInAnonymousClass() {
    super();
  }
}
