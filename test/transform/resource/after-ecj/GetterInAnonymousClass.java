import lombok.Getter;
public class GetterInAnonymousClass {
  Object annonymous = new Object() {
    @Getter class Inner {
      private String string;
      Inner() {
        super();
      }
      public @java.lang.SuppressWarnings("all") @lombok.Generated String getString() {
        return this.string;
      }
    }
    x() {
      super();
    }
  };
  public GetterInAnonymousClass() {
    super();
  }
}
