public class JacksonizedSuperBuilderSimple {
  public static @lombok.extern.jackson.Jacksonized @lombok.experimental.SuperBuilder @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true) @com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = JacksonizedSuperBuilderSimple.Parent.ParentBuilderImpl.class) class Parent {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilder<C extends JacksonizedSuperBuilderSimple.Parent, B extends JacksonizedSuperBuilderSimple.Parent.ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated int field1;
      public ParentBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B field1(final int field1) {
        this.field1 = field1;
        return self();
      }
      protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (("JacksonizedSuperBuilderSimple.Parent.ParentBuilder(field1=" + this.field1) + ")");
      }
    }
    static final @java.lang.SuppressWarnings("all") @lombok.Generated @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true) @com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "",buildMethodName = "build") class ParentBuilderImpl extends JacksonizedSuperBuilderSimple.Parent.ParentBuilder<JacksonizedSuperBuilderSimple.Parent, JacksonizedSuperBuilderSimple.Parent.ParentBuilderImpl> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedSuperBuilderSimple.Parent.ParentBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedSuperBuilderSimple.Parent build() {
        return new JacksonizedSuperBuilderSimple.Parent(this);
      }
    }
    int field1;
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Parent(final JacksonizedSuperBuilderSimple.Parent.ParentBuilder<?, ?> b) {
      super();
      this.field1 = b.field1;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated JacksonizedSuperBuilderSimple.Parent.ParentBuilder<?, ?> builder() {
      return new JacksonizedSuperBuilderSimple.Parent.ParentBuilderImpl();
    }
  }
  public JacksonizedSuperBuilderSimple() {
    super();
  }
  public static void test() {
    Parent x = Parent.builder().field1(5).build();
  }
}
