import java.util.List;
public class SuperBuilderWithGenerics2 {
  public static @lombok.experimental.SuperBuilder class Parent<A> {
    public static abstract @java.lang.SuppressWarnings("all") class ParentBuilder<A, C extends SuperBuilderWithGenerics2.Parent<A>, B extends SuperBuilderWithGenerics2.Parent.ParentBuilder<A, C, B>> {
      private @java.lang.SuppressWarnings("all") A field1;
      private @java.lang.SuppressWarnings("all") java.util.ArrayList<String> items;
      public ParentBuilder() {
        super();
      }
      protected abstract @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.SuppressWarnings("all") C build();
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") B field1(final A field1) {
        this.field1 = field1;
        return self();
      }
      public @java.lang.SuppressWarnings("all") B item(final String item) {
        if ((this.items == null))
            this.items = new java.util.ArrayList<String>();
        this.items.add(item);
        return self();
      }
      public @java.lang.SuppressWarnings("all") B items(final java.util.Collection<? extends String> items) {
        if ((items == null))
            {
              throw new java.lang.NullPointerException("items cannot be null");
            }
        if ((this.items == null))
            this.items = new java.util.ArrayList<String>();
        this.items.addAll(items);
        return self();
      }
      public @java.lang.SuppressWarnings("all") B clearItems() {
        if ((this.items != null))
            this.items.clear();
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((("SuperBuilderWithGenerics2.Parent.ParentBuilder(field1=" + this.field1) + ", items=") + this.items) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ParentBuilderImpl<A> extends SuperBuilderWithGenerics2.Parent.ParentBuilder<A, SuperBuilderWithGenerics2.Parent<A>, SuperBuilderWithGenerics2.Parent.ParentBuilderImpl<A>> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderWithGenerics2.Parent.ParentBuilderImpl<A> self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderWithGenerics2.Parent<A> build() {
        return new SuperBuilderWithGenerics2.Parent<A>(this);
      }
    }
    A field1;
    @lombok.Singular List<String> items;
    protected @java.lang.SuppressWarnings("all") Parent(final SuperBuilderWithGenerics2.Parent.ParentBuilder<A, ?, ?> b) {
      super();
      this.field1 = b.field1;
      java.util.List<String> items;
      switch (((b.items == null) ? 0 : b.items.size())) {
      case 0 : 
          items = java.util.Collections.emptyList();
          break;
      case 1 : 
          items = java.util.Collections.singletonList(b.items.get(0));
          break;
      default : 
          items = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(b.items));
      }
      this.items = items;
    }
    public static @java.lang.SuppressWarnings("all") <A>SuperBuilderWithGenerics2.Parent.ParentBuilder<A, ?, ?> builder() {
      return new SuperBuilderWithGenerics2.Parent.ParentBuilderImpl<A>();
    }
  }
  public static @lombok.experimental.SuperBuilder(builderMethodName = "builder2") class Child<A> extends Parent<String> {
    public static abstract @java.lang.SuppressWarnings("all") class ChildBuilder<A, C extends SuperBuilderWithGenerics2.Child<A>, B extends SuperBuilderWithGenerics2.Child.ChildBuilder<A, C, B>> extends Parent.ParentBuilder<String, C, B> {
      private @java.lang.SuppressWarnings("all") A field3;
      public ChildBuilder() {
        super();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") C build();
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") B field3(final A field3) {
        this.field3 = field3;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((("SuperBuilderWithGenerics2.Child.ChildBuilder(super=" + super.toString()) + ", field3=") + this.field3) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ChildBuilderImpl<A> extends SuperBuilderWithGenerics2.Child.ChildBuilder<A, SuperBuilderWithGenerics2.Child<A>, SuperBuilderWithGenerics2.Child.ChildBuilderImpl<A>> {
      private ChildBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderWithGenerics2.Child.ChildBuilderImpl<A> self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderWithGenerics2.Child<A> build() {
        return new SuperBuilderWithGenerics2.Child<A>(this);
      }
    }
    A field3;
    protected @java.lang.SuppressWarnings("all") Child(final SuperBuilderWithGenerics2.Child.ChildBuilder<A, ?, ?> b) {
      super(b);
      this.field3 = b.field3;
    }
    public static @java.lang.SuppressWarnings("all") <A>SuperBuilderWithGenerics2.Child.ChildBuilder<A, ?, ?> builder2() {
      return new SuperBuilderWithGenerics2.Child.ChildBuilderImpl<A>();
    }
  }
  public SuperBuilderWithGenerics2() {
    super();
  }
  public static void test() {
    Child<Integer> x = Child.<Integer>builder2().field3(1).field1("value").item("").build();
  }
}
