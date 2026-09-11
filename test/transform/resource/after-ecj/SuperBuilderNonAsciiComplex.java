public class SuperBuilderNonAsciiComplex {
  public static @lombok.SuperBuilder class 부모<T> {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class 부모Builder<T, C extends SuperBuilderNonAsciiComplex.부모<T>, B extends SuperBuilderNonAsciiComplex.부모.부모Builder<T, C, B>> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated Long a;
      private @java.lang.SuppressWarnings("all") @lombok.Generated T z;
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
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B z(final T z) {
        this.z = z;
        return self();
      }
      protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (((("SuperBuilderNonAsciiComplex.부모.부모Builder(a=" + this.a) + ", z=") + this.z) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class 부모BuilderImpl<T> extends SuperBuilderNonAsciiComplex.부모.부모Builder<T, SuperBuilderNonAsciiComplex.부모<T>, SuperBuilderNonAsciiComplex.부모.부모BuilderImpl<T>> {
      private 부모BuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNonAsciiComplex.부모.부모BuilderImpl<T> self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNonAsciiComplex.부모<T> build() {
        return new SuperBuilderNonAsciiComplex.부모<T>(this);
      }
    }
    Long a;
    T z;
    protected @java.lang.SuppressWarnings("all") @lombok.Generated 부모(final SuperBuilderNonAsciiComplex.부모.부모Builder<T, ?, ?> b) {
      super();
      this.a = b.a;
      this.z = b.z;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated <T>SuperBuilderNonAsciiComplex.부모.부모Builder<T, ?, ?> builder() {
      return new SuperBuilderNonAsciiComplex.부모.부모BuilderImpl<T>();
    }
  }
  public static @lombok.SuperBuilder class 자식 extends SuperBuilderNonAsciiComplex.부모<String> {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class 자식Builder<C extends SuperBuilderNonAsciiComplex.자식, B extends SuperBuilderNonAsciiComplex.자식.자식Builder<C, B>> extends SuperBuilderNonAsciiComplex.부모.부모Builder<String, C, B> {
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
        return (((("SuperBuilderNonAsciiComplex.자식.자식Builder(super=" + super.toString()) + ", b=") + this.b) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class 자식BuilderImpl extends SuperBuilderNonAsciiComplex.자식.자식Builder<SuperBuilderNonAsciiComplex.자식, SuperBuilderNonAsciiComplex.자식.자식BuilderImpl> {
      private 자식BuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNonAsciiComplex.자식.자식BuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNonAsciiComplex.자식 build() {
        return new SuperBuilderNonAsciiComplex.자식(this);
      }
    }
    String b;
    protected @java.lang.SuppressWarnings("all") @lombok.Generated 자식(final SuperBuilderNonAsciiComplex.자식.자식Builder<?, ?> b) {
      super(b);
      this.b = b.b;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNonAsciiComplex.자식.자식Builder<?, ?> builder() {
      return new SuperBuilderNonAsciiComplex.자식.자식BuilderImpl();
    }
  }
  public SuperBuilderNonAsciiComplex() {
    super();
  }
  public static void test() {
    부모<String> x = 자식.builder().b("").a(5L).build();
  }
}
