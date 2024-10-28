public class SuperBuilderNameClashes {
  public static @lombok.experimental.SuperBuilder class GenericsClash<B, C, C2> {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class GenericsClashBuilder<B, C, C2, C3 extends SuperBuilderNameClashes.GenericsClash<B, C, C2>, B2 extends SuperBuilderNameClashes.GenericsClash.GenericsClashBuilder<B, C, C2, C3, B2>> {
      public GenericsClashBuilder() {
        super();
      }
      protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B2 self();
      public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C3 build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return "SuperBuilderNameClashes.GenericsClash.GenericsClashBuilder()";
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class GenericsClashBuilderImpl<B, C, C2> extends SuperBuilderNameClashes.GenericsClash.GenericsClashBuilder<B, C, C2, SuperBuilderNameClashes.GenericsClash<B, C, C2>, SuperBuilderNameClashes.GenericsClash.GenericsClashBuilderImpl<B, C, C2>> {
      private GenericsClashBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNameClashes.GenericsClash.GenericsClashBuilderImpl<B, C, C2> self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNameClashes.GenericsClash<B, C, C2> build() {
        return new SuperBuilderNameClashes.GenericsClash<B, C, C2>(this);
      }
    }
    protected @java.lang.SuppressWarnings("all") @lombok.Generated GenericsClash(final SuperBuilderNameClashes.GenericsClash.GenericsClashBuilder<B, C, C2, ?, ?> b) {
      super();
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated <B, C, C2>SuperBuilderNameClashes.GenericsClash.GenericsClashBuilder<B, C, C2, ?, ?> builder() {
      return new SuperBuilderNameClashes.GenericsClash.GenericsClashBuilderImpl<B, C, C2>();
    }
  }
  public static @lombok.experimental.SuperBuilder class B {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class BBuilder<C extends SuperBuilderNameClashes.B, B2 extends SuperBuilderNameClashes.B.BBuilder<C, B2>> {
      public BBuilder() {
        super();
      }
      protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B2 self();
      public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return "SuperBuilderNameClashes.B.BBuilder()";
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class BBuilderImpl extends SuperBuilderNameClashes.B.BBuilder<SuperBuilderNameClashes.B, SuperBuilderNameClashes.B.BBuilderImpl> {
      private BBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNameClashes.B.BBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNameClashes.B build() {
        return new SuperBuilderNameClashes.B(this);
      }
    }
    protected @java.lang.SuppressWarnings("all") @lombok.Generated B(final SuperBuilderNameClashes.B.BBuilder<?, ?> b) {
      super();
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNameClashes.B.BBuilder<?, ?> builder() {
      return new SuperBuilderNameClashes.B.BBuilderImpl();
    }
  }
  public static class C2 {
    public C2() {
      super();
    }
  }
  public static @lombok.experimental.SuperBuilder class C {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class CBuilder<C3 extends SuperBuilderNameClashes.C, B extends SuperBuilderNameClashes.C.CBuilder<C3, B>> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated C2 c2;
      public CBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B c2(final C2 c2) {
        this.c2 = c2;
        return self();
      }
      protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C3 build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (("SuperBuilderNameClashes.C.CBuilder(c2=" + this.c2) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class CBuilderImpl extends SuperBuilderNameClashes.C.CBuilder<SuperBuilderNameClashes.C, SuperBuilderNameClashes.C.CBuilderImpl> {
      private CBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNameClashes.C.CBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNameClashes.C build() {
        return new SuperBuilderNameClashes.C(this);
      }
    }
    C2 c2;
    protected @java.lang.SuppressWarnings("all") @lombok.Generated C(final SuperBuilderNameClashes.C.CBuilder<?, ?> b) {
      super();
      this.c2 = b.c2;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNameClashes.C.CBuilder<?, ?> builder() {
      return new SuperBuilderNameClashes.C.CBuilderImpl();
    }
  }
  interface B2 {
    interface B4<X> {
    }
  }
  interface B3<Y> {
  }
  public static @lombok.experimental.SuperBuilder class ExtendsClauseCollision extends B implements B2.B4<Object>, B3<Object> {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ExtendsClauseCollisionBuilder<C extends SuperBuilderNameClashes.ExtendsClauseCollision, B4 extends SuperBuilderNameClashes.ExtendsClauseCollision.ExtendsClauseCollisionBuilder<C, B4>> extends B.BBuilder<C, B4> {
      public ExtendsClauseCollisionBuilder() {
        super();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated B4 self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (("SuperBuilderNameClashes.ExtendsClauseCollision.ExtendsClauseCollisionBuilder(super=" + super.toString()) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ExtendsClauseCollisionBuilderImpl extends SuperBuilderNameClashes.ExtendsClauseCollision.ExtendsClauseCollisionBuilder<SuperBuilderNameClashes.ExtendsClauseCollision, SuperBuilderNameClashes.ExtendsClauseCollision.ExtendsClauseCollisionBuilderImpl> {
      private ExtendsClauseCollisionBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNameClashes.ExtendsClauseCollision.ExtendsClauseCollisionBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNameClashes.ExtendsClauseCollision build() {
        return new SuperBuilderNameClashes.ExtendsClauseCollision(this);
      }
    }
    protected @java.lang.SuppressWarnings("all") @lombok.Generated ExtendsClauseCollision(final SuperBuilderNameClashes.ExtendsClauseCollision.ExtendsClauseCollisionBuilder<?, ?> b) {
      super(b);
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderNameClashes.ExtendsClauseCollision.ExtendsClauseCollisionBuilder<?, ?> builder() {
      return new SuperBuilderNameClashes.ExtendsClauseCollision.ExtendsClauseCollisionBuilderImpl();
    }
  }
  public SuperBuilderNameClashes() {
    super();
  }
}
