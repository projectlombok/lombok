import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
public class ConstructorsInAnonymousClass {
  Object annonymous = new Object() {
    @AllArgsConstructor @RequiredArgsConstructor @NoArgsConstructor class Inner {
      private String string;
      private @NonNull String string2;
      public @java.lang.SuppressWarnings("all") @lombok.Generated Inner(final String string, final @NonNull String string2) {
        super();
        if ((string2 == null))
            {
              throw new java.lang.NullPointerException("string2 is marked non-null but is null");
            }
        this.string = string;
        this.string2 = string2;
      }
      public @java.lang.SuppressWarnings("all") @lombok.Generated Inner(final @NonNull String string2) {
        super();
        if ((string2 == null))
            {
              throw new java.lang.NullPointerException("string2 is marked non-null but is null");
            }
        this.string2 = string2;
      }
      public @java.lang.SuppressWarnings("all") @lombok.Generated Inner() {
        super();
      }
    }
    x() {
      super();
    }
  };
  public ConstructorsInAnonymousClass() {
    super();
  }
}
