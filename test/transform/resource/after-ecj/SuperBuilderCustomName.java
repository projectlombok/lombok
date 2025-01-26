import java.util.List;
@lombok.experimental.SuperBuilder class SuperBuilderCustomName<T> {
  public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class SimpleTestBuilder<T, C extends SuperBuilderCustomName<T>, B extends SuperBuilderCustomName.SimpleTestBuilder<T, C, B>> {
    private @java.lang.SuppressWarnings("all") @lombok.Generated int field;
    public SimpleTestBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated B field(final int field) {
      this.field = field;
      return self();
    }
    protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B self();
    public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C build();
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (("SuperBuilderCustomName.SimpleTestBuilder(field=" + this.field) + ")");
    }
  }
  private static final @java.lang.SuppressWarnings("all") @lombok.Generated class SimpleTestBuilderImpl<T> extends SuperBuilderCustomName.SimpleTestBuilder<T, SuperBuilderCustomName<T>, SuperBuilderCustomName.SimpleTestBuilderImpl<T>> {
    private SimpleTestBuilderImpl() {
      super();
    }
    protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderCustomName.SimpleTestBuilderImpl<T> self() {
      return this;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderCustomName<T> build() {
      return new SuperBuilderCustomName<T>(this);
    }
  }
  private final int field;
  protected @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderCustomName(final SuperBuilderCustomName.SimpleTestBuilder<T, ?, ?> b) {
    super();
    this.field = b.field;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated <T>SuperBuilderCustomName.SimpleTestBuilder<T, ?, ?> builder() {
    return new SuperBuilderCustomName.SimpleTestBuilderImpl<T>();
  }
}
