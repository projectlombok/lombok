import lombok.With;
public class WithInAnonymousClass {
  Object annonymous = new Object() {
    @With class Inner {
      private String string;
      private Inner(String string) {
        super();
      }
      /**
       * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated Inner withString(final String string) {
        return ((this.string == string) ? this : new Inner(string));
      }
    }
    x() {
      super();
    }
  };
  public WithInAnonymousClass() {
    super();
  }
}
