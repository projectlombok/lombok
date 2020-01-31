import java.util.List;
public class SuperBuilderBasic {
  public static @lombok.experimental.SuperBuilder class Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ParentBuilder<C extends SuperBuilderBasic.Parent, B extends SuperBuilderBasic.Parent.ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") int field1;
      private @java.lang.SuppressWarnings("all") java.util.ArrayList<String> items;
      public ParentBuilder() {
        super();
      }
      protected abstract @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.SuppressWarnings("all") C build();
      public @java.lang.SuppressWarnings("all") B field1(final int field1) {
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
        return (((("SuperBuilderBasic.Parent.ParentBuilder(field1=" + this.field1) + ", items=") + this.items) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ParentBuilderImpl extends SuperBuilderBasic.Parent.ParentBuilder<SuperBuilderBasic.Parent, SuperBuilderBasic.Parent.ParentBuilderImpl> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderBasic.Parent.ParentBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderBasic.Parent build() {
        return new SuperBuilderBasic.Parent(this);
      }
    }
    int field1;
    @lombok.Singular List<String> items;
    protected @java.lang.SuppressWarnings("all") Parent(final SuperBuilderBasic.Parent.ParentBuilder<?, ?> b) {
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
    public static @java.lang.SuppressWarnings("all") SuperBuilderBasic.Parent.ParentBuilder<?, ?> builder() {
      return new SuperBuilderBasic.Parent.ParentBuilderImpl();
    }
  }
  public static @lombok.experimental.SuperBuilder class Child extends Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ChildBuilder<C extends SuperBuilderBasic.Child, B extends SuperBuilderBasic.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") double field3;
      public ChildBuilder() {
        super();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") C build();
      public @java.lang.SuppressWarnings("all") B field3(final double field3) {
        this.field3 = field3;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((("SuperBuilderBasic.Child.ChildBuilder(super=" + super.toString()) + ", field3=") + this.field3) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ChildBuilderImpl extends SuperBuilderBasic.Child.ChildBuilder<SuperBuilderBasic.Child, SuperBuilderBasic.Child.ChildBuilderImpl> {
      private ChildBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderBasic.Child.ChildBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderBasic.Child build() {
        return new SuperBuilderBasic.Child(this);
      }
    }
    double field3;
    protected @java.lang.SuppressWarnings("all") Child(final SuperBuilderBasic.Child.ChildBuilder<?, ?> b) {
      super(b);
      this.field3 = b.field3;
    }
    public static @java.lang.SuppressWarnings("all") SuperBuilderBasic.Child.ChildBuilder<?, ?> builder() {
      return new SuperBuilderBasic.Child.ChildBuilderImpl();
    }
  }
  public SuperBuilderBasic() {
    super();
  }
  public static void test() {
    Child x = Child.builder().field3(0.0).field1(5).item("").build();
  }
}
