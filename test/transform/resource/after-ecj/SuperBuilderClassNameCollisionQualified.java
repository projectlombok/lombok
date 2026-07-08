@lombok.SuperBuilder class SuperBuilderClassNameCollisionQualified<T> {
  public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class SuperBuilder<T, C extends SuperBuilderClassNameCollisionQualified<T>, B extends SuperBuilderClassNameCollisionQualified.SuperBuilder<T, C, B>> {
    private @java.lang.SuppressWarnings("all") @lombok.Generated T value;
    public SuperBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated B value(final T value) {
      this.value = value;
      return self();
    }
    protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B self();
    public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C build();
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (("SuperBuilderClassNameCollisionQualified.SuperBuilder(value=" + this.value) + ")");
    }
  }
  private static final @java.lang.SuppressWarnings("all") @lombok.Generated class SuperBuilderImpl<T> extends SuperBuilderClassNameCollisionQualified.SuperBuilder<T, SuperBuilderClassNameCollisionQualified<T>, SuperBuilderClassNameCollisionQualified.SuperBuilderImpl<T>> {
    private SuperBuilderImpl() {
      super();
    }
    protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderClassNameCollisionQualified.SuperBuilderImpl<T> self() {
      return this;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderClassNameCollisionQualified<T> build() {
      return new SuperBuilderClassNameCollisionQualified<T>(this);
    }
  }
  T value;
  protected @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderClassNameCollisionQualified(final SuperBuilderClassNameCollisionQualified.SuperBuilder<T, ?, ?> b) {
    super();
    this.value = b.value;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated <T>SuperBuilderClassNameCollisionQualified.SuperBuilder<T, ?, ?> builder() {
    return new SuperBuilderClassNameCollisionQualified.SuperBuilderImpl<T>();
  }
}
