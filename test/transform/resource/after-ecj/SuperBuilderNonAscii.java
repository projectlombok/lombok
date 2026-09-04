public class SuperBuilderNonAscii {
  public static @lombok.SuperBuilder class 부모 {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class 부모Builder<C extends SuperBuilderNonAscii.부모, B extends SuperBuilderNonAscii.부모.부모Builder<C, B>> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated Long a;
      public 부모Builder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B a(final Long a) {
        this.a = a;
        return self();
      }
      protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (("SuperBuilderNonAscii.부모.부모Builder(a=" + this.a) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class 부모BuilderImpl extends SuperBuilderNonAscii.부모.부모Builder<SuperBuilderNonAscii.부모, SuperBuilderNonAscii.부모.부모BuilderImpl> {
      private 부모BuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNonAscii.부모.부모BuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNonAscii.부모 build() {
        return new SuperBuilderNonAscii.부모(this);
      }
    }
    Long a;
    protected @java.lang.SuppressWarnings("all") @lombok.Generated 부모(final SuperBuilderNonAscii.부모.부모Builder<?, ?> b) {
      super();
      this.a = b.a;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNonAscii.부모.부모Builder<?, ?> builder() {
      return new SuperBuilderNonAscii.부모.부모BuilderImpl();
    }
  }
  public static @lombok.SuperBuilder class 자식 extends 부모 {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class 자식Builder<C extends SuperBuilderNonAscii.자식, B extends SuperBuilderNonAscii.자식.자식Builder<C, B>> extends 부모.부모Builder<C, B> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated String b;
      public 자식Builder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B b(final String b) {
        this.b = b;
        return self();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (((("SuperBuilderNonAscii.자식.자식Builder(super=" + super.toString()) + ", b=") + this.b) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class 자식BuilderImpl extends SuperBuilderNonAscii.자식.자식Builder<SuperBuilderNonAscii.자식, SuperBuilderNonAscii.자식.자식BuilderImpl> {
      private 자식BuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNonAscii.자식.자식BuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNonAscii.자식 build() {
        return new SuperBuilderNonAscii.자식(this);
      }
    }
    String b;
    protected @java.lang.SuppressWarnings("all") @lombok.Generated 자식(final SuperBuilderNonAscii.자식.자식Builder<?, ?> b) {
      super(b);
      this.b = b.b;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNonAscii.자식.자식Builder<?, ?> builder() {
      return new SuperBuilderNonAscii.자식.자식BuilderImpl();
    }
  }
  public SuperBuilderNonAscii() {
    super();
  }
  public static void test() {
    자식 x = 자식.builder().b("").a(5L).build();
  }
}
