public class SuperBuilderWithExcludes {
  public static @lombok.experimental.SuperBuilder class Parent<N extends Number> {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilder<N extends Number, C extends SuperBuilderWithExcludes.Parent<N>, B extends SuperBuilderWithExcludes.Parent.ParentBuilder<N, C, B>> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated N numberField;
      private @java.lang.SuppressWarnings("all") @lombok.Generated long randomFieldP;

      public ParentBuilder() {
        super();
      }

      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B numberField(final N numberField) {
        this.numberField = numberField;
        return self();
      }

      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B randomFieldP(final long randomFieldP) {
        this.randomFieldP = randomFieldP;
        return self();
      }

      protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B self();

      public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C build();

      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (((("SuperBuilderWithExcludes.Parent.ParentBuilder(numberField=" + this.numberField) + ", randomFieldP=") + this.randomFieldP) + ")");
      }
    }

    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilderImpl<N extends Number> extends SuperBuilderWithExcludes.Parent.ParentBuilder<N, SuperBuilderWithExcludes.Parent<N>, SuperBuilderWithExcludes.Parent.ParentBuilderImpl<N>> {
      private ParentBuilderImpl() {
        super();
      }

      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithExcludes.Parent.ParentBuilderImpl<N> self() {
        return this;
      }

      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithExcludes.Parent<N> build() {
        return new SuperBuilderWithExcludes.Parent<N>(this);
      }
    }

    private @lombok.Builder.Exclude long excludedParentField;
    private N numberField;
    private long randomFieldP;

    protected @java.lang.SuppressWarnings("all") @lombok.Generated Parent(final SuperBuilderWithExcludes.Parent.ParentBuilder<N, ?, ?> b) {
      super();
      this.numberField = b.numberField;
      this.randomFieldP = b.randomFieldP;
    }

    public static @java.lang.SuppressWarnings("all") @lombok.Generated <N extends Number>SuperBuilderWithExcludes.Parent.ParentBuilder<N, ?, ?> builder() {
      return new SuperBuilderWithExcludes.Parent.ParentBuilderImpl<N>();
    }
  }

  public static @lombok.experimental.SuperBuilder class Child extends Parent<Integer> {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ChildBuilder<C extends SuperBuilderWithExcludes.Child, B extends SuperBuilderWithExcludes.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<Integer, C, B> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated double doubleField;
      private @java.lang.SuppressWarnings("all") @lombok.Generated long randomFieldC;

      public ChildBuilder() {
        super();
      }

      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B doubleField(final double doubleField) {
        this.doubleField = doubleField;
        return self();
      }

      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B randomFieldC(final long randomFieldC) {
        this.randomFieldC = randomFieldC;
        return self();
      }

      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated B self();

      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated C build();

      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (((((("SuperBuilderWithExcludes.Child.ChildBuilder(super=" + super.toString()) + ", doubleField=") + this.doubleField) + ", randomFieldC=") + this.randomFieldC) + ")");
      }
    }

    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ChildBuilderImpl extends SuperBuilderWithExcludes.Child.ChildBuilder<SuperBuilderWithExcludes.Child, SuperBuilderWithExcludes.Child.ChildBuilderImpl> {
      private ChildBuilderImpl() {
        super();
      }

      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithExcludes.Child.ChildBuilderImpl self() {
        return this;
      }

      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithExcludes.Child build() {
        return new SuperBuilderWithExcludes.Child(this);
      }
    }

    private double doubleField;
    private @lombok.Builder.Exclude long excludedChildField;
    private long randomFieldC;

    protected @java.lang.SuppressWarnings("all") @lombok.Generated Child(final SuperBuilderWithExcludes.Child.ChildBuilder<?, ?> b) {
      super(b);
      this.doubleField = b.doubleField;
      this.randomFieldC = b.randomFieldC;
    }

    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithExcludes.Child.ChildBuilder<?, ?> builder() {
      return new SuperBuilderWithExcludes.Child.ChildBuilderImpl();
    }
  }

  public SuperBuilderWithExcludes() {
    super();
  }
}