import java.util.List;
@lombok.experimental.SuperBuilder class SuperBuilderCustomName<T> {
  public static abstract @java.lang.SuppressWarnings("all") class SimpleTestBuilder<T, C extends SuperBuilderCustomName<T>, B extends SuperBuilderCustomName.SimpleTestBuilder<T, C, B>> {
    private @java.lang.SuppressWarnings("all") int field;
    public SimpleTestBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") B field(final int field) {
      this.field = field;
      return self();
    }
    protected abstract @java.lang.SuppressWarnings("all") B self();
    public abstract @java.lang.SuppressWarnings("all") C build();
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("SuperBuilderCustomName.SimpleTestBuilder(field=" + this.field) + ")");
    }
  }
  private static final @java.lang.SuppressWarnings("all") class SimpleTestBuilderImpl<T> extends SuperBuilderCustomName.SimpleTestBuilder<T, SuperBuilderCustomName<T>, SuperBuilderCustomName.SimpleTestBuilderImpl<T>> {
    private SimpleTestBuilderImpl() {
      super();
    }
    protected @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderCustomName.SimpleTestBuilderImpl<T> self() {
      return this;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderCustomName<T> build() {
      return new SuperBuilderCustomName<T>(this);
    }
  }
  private final int field;
  protected @java.lang.SuppressWarnings("all") SuperBuilderCustomName(final SuperBuilderCustomName.SimpleTestBuilder<T, ?, ?> b) {
    super();
    this.field = b.field;
  }
  public static @java.lang.SuppressWarnings("all") <T>SuperBuilderCustomName.SimpleTestBuilder<T, ?, ?> builder() {
    return new SuperBuilderCustomName.SimpleTestBuilderImpl<T>();
  }
}
