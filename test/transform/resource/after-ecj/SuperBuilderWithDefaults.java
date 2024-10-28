import java.util.List;
public class SuperBuilderWithDefaults {
  public static @lombok.experimental.SuperBuilder class Parent<N extends Number> {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilder<N extends Number, C extends SuperBuilderWithDefaults.Parent<N>, B extends SuperBuilderWithDefaults.Parent.ParentBuilder<N, C, B>> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated long millis$value;
      private @java.lang.SuppressWarnings("all") @lombok.Generated boolean millis$set;
      private @java.lang.SuppressWarnings("all") @lombok.Generated N numberField$value;
      private @java.lang.SuppressWarnings("all") @lombok.Generated boolean numberField$set;
      public ParentBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B millis(final long millis) {
        this.millis$value = millis;
        millis$set = true;
        return self();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B numberField(final N numberField) {
        this.numberField$value = numberField;
        numberField$set = true;
        return self();
      }
      protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (((("SuperBuilderWithDefaults.Parent.ParentBuilder(millis$value=" + this.millis$value) + ", numberField$value=") + this.numberField$value) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilderImpl<N extends Number> extends SuperBuilderWithDefaults.Parent.ParentBuilder<N, SuperBuilderWithDefaults.Parent<N>, SuperBuilderWithDefaults.Parent.ParentBuilderImpl<N>> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithDefaults.Parent.ParentBuilderImpl<N> self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithDefaults.Parent<N> build() {
        return new SuperBuilderWithDefaults.Parent<N>(this);
      }
    }
    private @lombok.Builder.Default long millis;
    private @lombok.Builder.Default N numberField;
    private static @java.lang.SuppressWarnings("all") @lombok.Generated <N extends Number>long $default$millis() {
      return System.currentTimeMillis();
    }
    private static @java.lang.SuppressWarnings("all") @lombok.Generated <N extends Number>N $default$numberField() {
      return null;
    }
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Parent(final SuperBuilderWithDefaults.Parent.ParentBuilder<N, ?, ?> b) {
      super();
      if (b.millis$set)
          this.millis = b.millis$value;
      else
          this.millis = SuperBuilderWithDefaults.Parent.<N>$default$millis();
      if (b.numberField$set)
          this.numberField = b.numberField$value;
      else
          this.numberField = SuperBuilderWithDefaults.Parent.<N>$default$numberField();
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated <N extends Number>SuperBuilderWithDefaults.Parent.ParentBuilder<N, ?, ?> builder() {
      return new SuperBuilderWithDefaults.Parent.ParentBuilderImpl<N>();
    }
  }
  public static @lombok.experimental.SuperBuilder class Child extends Parent<Integer> {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ChildBuilder<C extends SuperBuilderWithDefaults.Child, B extends SuperBuilderWithDefaults.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<Integer, C, B> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated double doubleField$value;
      private @java.lang.SuppressWarnings("all") @lombok.Generated boolean doubleField$set;
      public ChildBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B doubleField(final double doubleField) {
        this.doubleField$value = doubleField;
        doubleField$set = true;
        return self();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (((("SuperBuilderWithDefaults.Child.ChildBuilder(super=" + super.toString()) + ", doubleField$value=") + this.doubleField$value) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ChildBuilderImpl extends SuperBuilderWithDefaults.Child.ChildBuilder<SuperBuilderWithDefaults.Child, SuperBuilderWithDefaults.Child.ChildBuilderImpl> {
      private ChildBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithDefaults.Child.ChildBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithDefaults.Child build() {
        return new SuperBuilderWithDefaults.Child(this);
      }
    }
    private @lombok.Builder.Default double doubleField;
    private static @java.lang.SuppressWarnings("all") @lombok.Generated double $default$doubleField() {
      return Math.PI;
    }
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Child(final SuperBuilderWithDefaults.Child.ChildBuilder<?, ?> b) {
      super(b);
      if (b.doubleField$set)
          this.doubleField = b.doubleField$value;
      else
          this.doubleField = SuperBuilderWithDefaults.Child.$default$doubleField();
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithDefaults.Child.ChildBuilder<?, ?> builder() {
      return new SuperBuilderWithDefaults.Child.ChildBuilderImpl();
    }
  }
  public SuperBuilderWithDefaults() {
    super();
  }
  public static void test() {
    Child x = Child.builder().doubleField(0.1).numberField(5).millis(1234567890L).build();
  }
}
