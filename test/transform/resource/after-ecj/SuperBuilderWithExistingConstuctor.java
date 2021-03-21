public @lombok.experimental.SuperBuilder class SuperBuilderWithExistingConstuctor {
  public static abstract @java.lang.SuppressWarnings("all") class SuperBuilderWithExistingConstuctorBuilder<C extends SuperBuilderWithExistingConstuctor, B extends SuperBuilderWithExistingConstuctor.SuperBuilderWithExistingConstuctorBuilder<C, B>> {
    public SuperBuilderWithExistingConstuctorBuilder() {
      super();
    }
    protected abstract @java.lang.SuppressWarnings("all") B self();
    public abstract @java.lang.SuppressWarnings("all") C build();
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return "SuperBuilderWithExistingConstuctor.SuperBuilderWithExistingConstuctorBuilder()";
    }
  }
  private static final @java.lang.SuppressWarnings("all") class SuperBuilderWithExistingConstuctorBuilderImpl extends SuperBuilderWithExistingConstuctor.SuperBuilderWithExistingConstuctorBuilder<SuperBuilderWithExistingConstuctor, SuperBuilderWithExistingConstuctor.SuperBuilderWithExistingConstuctorBuilderImpl> {
    private SuperBuilderWithExistingConstuctorBuilderImpl() {
      super();
    }
    protected @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderWithExistingConstuctor.SuperBuilderWithExistingConstuctorBuilderImpl self() {
      return this;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderWithExistingConstuctor build() {
      return new SuperBuilderWithExistingConstuctor(this);
    }
  }
  public SuperBuilderWithExistingConstuctor() {
    super();
  }
  protected @java.lang.SuppressWarnings("all") SuperBuilderWithExistingConstuctor(final SuperBuilderWithExistingConstuctor.SuperBuilderWithExistingConstuctorBuilder<?, ?> b) {
    super();
  }
  public static @java.lang.SuppressWarnings("all") SuperBuilderWithExistingConstuctor.SuperBuilderWithExistingConstuctorBuilder<?, ?> builder() {
    return new SuperBuilderWithExistingConstuctor.SuperBuilderWithExistingConstuctorBuilderImpl();
  }
}