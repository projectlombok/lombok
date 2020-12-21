public @lombok.extern.jackson.Jacksonized @lombok.experimental.SuperBuilder @com.fasterxml.jackson.databind.annotation.JsonDeserialize class JacksonizedSuperBuilderWithJsonDeserialize {
  public static abstract @java.lang.SuppressWarnings("all") class JacksonizedSuperBuilderWithJsonDeserializeBuilder<C extends JacksonizedSuperBuilderWithJsonDeserialize, B extends JacksonizedSuperBuilderWithJsonDeserialize.JacksonizedSuperBuilderWithJsonDeserializeBuilder<C, B>> {
    private @java.lang.SuppressWarnings("all") int field1;
    public JacksonizedSuperBuilderWithJsonDeserializeBuilder() {
      super();
    }
    protected abstract @java.lang.SuppressWarnings("all") B self();
    public abstract @java.lang.SuppressWarnings("all") C build();
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") B field1(final int field1) {
      this.field1 = field1;
      return self();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("JacksonizedSuperBuilderWithJsonDeserialize.JacksonizedSuperBuilderWithJsonDeserializeBuilder(field1=" + this.field1) + ")");
    }
  }
  private static final @java.lang.SuppressWarnings("all") class JacksonizedSuperBuilderWithJsonDeserializeBuilderImpl extends JacksonizedSuperBuilderWithJsonDeserialize.JacksonizedSuperBuilderWithJsonDeserializeBuilder<JacksonizedSuperBuilderWithJsonDeserialize, JacksonizedSuperBuilderWithJsonDeserialize.JacksonizedSuperBuilderWithJsonDeserializeBuilderImpl> {
    private JacksonizedSuperBuilderWithJsonDeserializeBuilderImpl() {
      super();
    }
    protected @java.lang.Override @java.lang.SuppressWarnings("all") JacksonizedSuperBuilderWithJsonDeserialize.JacksonizedSuperBuilderWithJsonDeserializeBuilderImpl self() {
      return this;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") JacksonizedSuperBuilderWithJsonDeserialize build() {
      return new JacksonizedSuperBuilderWithJsonDeserialize(this);
    }
  }
  int field1;
  protected @java.lang.SuppressWarnings("all") JacksonizedSuperBuilderWithJsonDeserialize(final JacksonizedSuperBuilderWithJsonDeserialize.JacksonizedSuperBuilderWithJsonDeserializeBuilder<?, ?> b) {
    super();
    this.field1 = b.field1;
  }
  public static @java.lang.SuppressWarnings("all") JacksonizedSuperBuilderWithJsonDeserialize.JacksonizedSuperBuilderWithJsonDeserializeBuilder<?, ?> builder() {
    return new JacksonizedSuperBuilderWithJsonDeserialize.JacksonizedSuperBuilderWithJsonDeserializeBuilderImpl();
  }
}