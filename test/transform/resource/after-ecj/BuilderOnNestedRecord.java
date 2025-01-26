public record BuilderOnNestedRecord(T t)<T> {
  public @lombok.Builder record Nested(String a) {
    public static @java.lang.SuppressWarnings("all") @lombok.Generated class NestedBuilder {
      private @java.lang.SuppressWarnings("all") @lombok.Generated String a;
      @java.lang.SuppressWarnings("all") @lombok.Generated NestedBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderOnNestedRecord.Nested.NestedBuilder a(final String a) {
        this.a = a;
        return this;
      }
      public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderOnNestedRecord.Nested build() {
        return new BuilderOnNestedRecord.Nested(this.a);
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (("BuilderOnNestedRecord.Nested.NestedBuilder(a=" + this.a) + ")");
      }
    }
/* Implicit */    private final String a;
    public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderOnNestedRecord.Nested.NestedBuilder builder() {
      return new BuilderOnNestedRecord.Nested.NestedBuilder();
    }
  }
/* Implicit */  private final T t;
}
