public class SuperBuilderNameClashes {
  public static @lombok.experimental.SuperBuilder class GenericsClash<B, C, C2> {
    public static abstract @java.lang.SuppressWarnings("all") class GenericsClashBuilder<B, C, C2, C3 extends GenericsClash<B, C, C2>, B2 extends GenericsClashBuilder<B, C, C2, C3, B2>> {
      public GenericsClashBuilder() {
        super();
      }
      protected abstract @java.lang.SuppressWarnings("all") B2 self();
      public abstract @java.lang.SuppressWarnings("all") C3 build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return "SuperBuilderNameClashes.GenericsClash.GenericsClashBuilder()";
      }
    }
    private static final @java.lang.SuppressWarnings("all") class GenericsClashBuilderImpl<B, C, C2> extends GenericsClashBuilder<B, C, C2, GenericsClash<B, C, C2>, GenericsClashBuilderImpl<B, C, C2>> {
      private GenericsClashBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") GenericsClashBuilderImpl<B, C, C2> self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") GenericsClash<B, C, C2> build() {
        return new GenericsClash<B, C, C2>(this);
      }
    }
    protected @java.lang.SuppressWarnings("all") GenericsClash(final GenericsClashBuilder<B, C, C2, ?, ?> b) {
      super();
    }
    public static @java.lang.SuppressWarnings("all") <B, C, C2>GenericsClashBuilder<B, C, C2, ?, ?> builder() {
      return new GenericsClashBuilderImpl<B, C, C2>();
    }
  }
  public static @lombok.experimental.SuperBuilder class B {
    public static abstract @java.lang.SuppressWarnings("all") class BBuilder<C extends B, B2 extends BBuilder<C, B2>> {
      public BBuilder() {
        super();
      }
      protected abstract @java.lang.SuppressWarnings("all") B2 self();
      public abstract @java.lang.SuppressWarnings("all") C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return "SuperBuilderNameClashes.B.BBuilder()";
      }
    }
    private static final @java.lang.SuppressWarnings("all") class BBuilderImpl extends BBuilder<B, BBuilderImpl> {
      private BBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") BBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") B build() {
        return new B(this);
      }
    }
    protected @java.lang.SuppressWarnings("all") B(final BBuilder<?, ?> b) {
      super();
    }
    public static @java.lang.SuppressWarnings("all") BBuilder<?, ?> builder() {
      return new BBuilderImpl();
    }
  }
  public static class C2 {
    public C2() {
      super();
    }
  }
  public static @lombok.experimental.SuperBuilder class C {
    public static abstract @java.lang.SuppressWarnings("all") class CBuilder<C3 extends C, B extends CBuilder<C3, B>> {
      private @java.lang.SuppressWarnings("all") C2 c2;
      public CBuilder() {
        super();
      }
      protected abstract @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.SuppressWarnings("all") C3 build();
      public @java.lang.SuppressWarnings("all") B c2(final C2 c2) {
        this.c2 = c2;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (("SuperBuilderNameClashes.C.CBuilder(c2=" + this.c2) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class CBuilderImpl extends CBuilder<C, CBuilderImpl> {
      private CBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") CBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") C build() {
        return new C(this);
      }
    }
    C2 c2;
    protected @java.lang.SuppressWarnings("all") C(final CBuilder<?, ?> b) {
      super();
      this.c2 = b.c2;
    }
    public static @java.lang.SuppressWarnings("all") CBuilder<?, ?> builder() {
      return new CBuilderImpl();
    }
  }
  public SuperBuilderNameClashes() {
    super();
  }
}