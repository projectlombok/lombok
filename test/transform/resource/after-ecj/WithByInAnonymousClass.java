import lombok.experimental.WithBy;
public class WithByInAnonymousClass {
  Object annonymous = new Object() {
    @WithBy class Inner {
      private String string;
      private Inner(String string) {
        super();
      }
      public @java.lang.SuppressWarnings("all") @lombok.Generated Inner withStringBy(final java.util.function.Function<? super String, ? extends String> transformer) {
        return new Inner(transformer.apply(this.string));
      }
    }
    x() {
      super();
    }
  };
  public WithByInAnonymousClass() {
    super();
  }
}
