import java.util.List;
public class SuperBuilderWithDefaults {
  public static @lombok.experimental.SuperBuilder class Parent<N extends Number> {
    public static abstract @java.lang.SuppressWarnings("all") class ParentBuilder<N extends Number, C extends Parent<N>, B extends ParentBuilder<N, C, B>> {
      private @java.lang.SuppressWarnings("all") long millis;
      private @java.lang.SuppressWarnings("all") boolean millis$set;
      private @java.lang.SuppressWarnings("all") N numberField;
      private @java.lang.SuppressWarnings("all") boolean numberField$set;
      public ParentBuilder() {
        super();
      }
      protected abstract @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.SuppressWarnings("all") C build();
      public @java.lang.SuppressWarnings("all") B millis(final long millis) {
        this.millis = millis;
        millis$set = true;
        return self();
      }
      public @java.lang.SuppressWarnings("all") B numberField(final N numberField) {
        this.numberField = numberField;
        numberField$set = true;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((("SuperBuilderWithDefaults.Parent.ParentBuilder(millis=" + this.millis) + ", numberField=") + this.numberField) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ParentBuilderImpl<N extends Number> extends ParentBuilder<N, Parent<N>, ParentBuilderImpl<N>> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") ParentBuilderImpl<N> self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") Parent<N> build() {
        return new Parent<N>(this);
      }
    }
    private @lombok.Builder.Default long millis;
    private @lombok.Builder.Default N numberField;
    private static @java.lang.SuppressWarnings("all") <N extends Number>long $default$millis() {
      return System.currentTimeMillis();
    }
    private static @java.lang.SuppressWarnings("all") <N extends Number>N $default$numberField() {
      return null;
    }
    protected @java.lang.SuppressWarnings("all") Parent(final ParentBuilder<N, ?, ?> b) {
      super();
      if (b.millis$set)
          this.millis = b.millis;
      else
          this.millis = Parent.<N>$default$millis();
      if (b.numberField$set)
          this.numberField = b.numberField;
      else
          this.numberField = Parent.<N>$default$numberField();
    }
    public static @java.lang.SuppressWarnings("all") <N extends Number>ParentBuilder<N, ?, ?> builder() {
      return new ParentBuilderImpl<N>();
    }
  }
  public static @lombok.experimental.SuperBuilder class Child extends Parent<Integer> {
    public static abstract @java.lang.SuppressWarnings("all") class ChildBuilder<C extends Child, B extends ChildBuilder<C, B>> extends Parent.ParentBuilder<Integer, C, B> {
      private @java.lang.SuppressWarnings("all") double doubleField;
      private @java.lang.SuppressWarnings("all") boolean doubleField$set;
      public ChildBuilder() {
        super();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") C build();
      public @java.lang.SuppressWarnings("all") B doubleField(final double doubleField) {
        this.doubleField = doubleField;
        doubleField$set = true;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((("SuperBuilderWithDefaults.Child.ChildBuilder(super=" + super.toString()) + ", doubleField=") + this.doubleField) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ChildBuilderImpl extends ChildBuilder<Child, ChildBuilderImpl> {
      private ChildBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") ChildBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") Child build() {
        return new Child(this);
      }
    }
    private @lombok.Builder.Default double doubleField;
    private static @java.lang.SuppressWarnings("all") double $default$doubleField() {
      return Math.PI;
    }
    protected @java.lang.SuppressWarnings("all") Child(final ChildBuilder<?, ?> b) {
      super(b);
      if (b.doubleField$set) 
          this.doubleField = b.doubleField;
      else
          this.doubleField = Child.$default$doubleField();
    }
    public static @java.lang.SuppressWarnings("all") ChildBuilder<?, ?> builder() {
      return new ChildBuilderImpl();
    }
  }
  public SuperBuilderWithDefaults() {
    super();
  }
  public static void test() {
    Child x = Child.builder().doubleField(0.1).numberField(5).millis(1234567890L).build();
  }
}
