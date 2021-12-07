import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
public class AccessorsInAnonymousClass {
  Object annonymous = new Object() {
    @Getter @Setter @Accessors(fluent = true) class Inner {
      private String string;
      Inner() {
        super();
      }
      public @java.lang.SuppressWarnings("all") String string() {
        return this.string;
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") Inner string(final String string) {
        this.string = string;
        return this;
      }
    }
    x() {
      super();
    }
  };
  public AccessorsInAnonymousClass() {
    super();
  }
}
