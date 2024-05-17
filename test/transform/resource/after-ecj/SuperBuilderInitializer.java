import lombok.experimental.SuperBuilder;
class SuperBuilderInitializer {
  public static @SuperBuilder class One {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class OneBuilder<C extends SuperBuilderInitializer.One, B extends SuperBuilderInitializer.One.OneBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated String world;
      public OneBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B world(final String world) {
        this.world = world;
        return self();
      }
      protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (("SuperBuilderInitializer.One.OneBuilder(world=" + this.world) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class OneBuilderImpl extends SuperBuilderInitializer.One.OneBuilder<SuperBuilderInitializer.One, SuperBuilderInitializer.One.OneBuilderImpl> {
      private OneBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderInitializer.One.OneBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderInitializer.One build() {
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
    protected @java.lang.SuppressWarnings("all") @lombok.Generated One(final SuperBuilderInitializer.One.OneBuilder<?, ?> b) {
      super();
      this.world = b.world;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderInitializer.One.OneBuilder<?, ?> builder() {
      return new SuperBuilderInitializer.One.OneBuilderImpl();
    }
  }
  SuperBuilderInitializer() {
    super();
  }
}
