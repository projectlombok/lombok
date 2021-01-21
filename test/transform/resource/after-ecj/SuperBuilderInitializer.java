import lombok.experimental.SuperBuilder;
class SuperBuilderInitializer {
  public static @SuperBuilder class One {
    public static abstract @java.lang.SuppressWarnings("all") class OneBuilder<C extends SuperBuilderInitializer.One, B extends SuperBuilderInitializer.One.OneBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") String world;
      public OneBuilder() {
        super();
      }
      protected abstract @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.SuppressWarnings("all") C build();
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") B world(final String world) {
        this.world = world;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (("SuperBuilderInitializer.One.OneBuilder(world=" + this.world) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class OneBuilderImpl extends SuperBuilderInitializer.One.OneBuilder<SuperBuilderInitializer.One, SuperBuilderInitializer.One.OneBuilderImpl> {
      private OneBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderInitializer.One.OneBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderInitializer.One build() {
        return new SuperBuilderInitializer.One(this);
      }
    }
    private String world;
    {
      world = "Hello";
    }
    private static final String world2;
    static {
      world2 = "Hello";
    }
    <clinit>() {
    }
    protected @java.lang.SuppressWarnings("all") One(final SuperBuilderInitializer.One.OneBuilder<?, ?> b) {
      super();
      this.world = b.world;
    }
    public static @java.lang.SuppressWarnings("all") SuperBuilderInitializer.One.OneBuilder<?, ?> builder() {
      return new SuperBuilderInitializer.One.OneBuilderImpl();
    }
  }
  SuperBuilderInitializer() {
    super();
  }
}
