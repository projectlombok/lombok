import java.util.List;
public class SuperBuilderBasic {
  public static @lombok.experimental.SuperBuilder class Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ParentBuilder<C extends Parent, B extends ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") int field1;
      private @java.lang.SuppressWarnings("all") java.util.ArrayList<String> items;
      protected abstract @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.SuppressWarnings("all") C build();
      public @java.lang.SuppressWarnings("all") B field1(final int field1) {
        this.field1 = field1;
        return self();
      }
      public @java.lang.SuppressWarnings("all") B item(String item) {
        if ((this.items == null))
            this.items = new java.util.ArrayList<String>();
        this.items.add(item);
        return self();
      }
      public @java.lang.SuppressWarnings("all") B items(java.util.Collection<? extends String> items) {
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
    private static final @java.lang.SuppressWarnings("all") class ParentBuilderImpl extends ParentBuilder<Parent, ParentBuilderImpl> {
      private @java.lang.SuppressWarnings("all") ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") ParentBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") Parent build() {
        return new Parent(this);
      }
    }
    int field1;
    @lombok.Singular List<String> items;
    protected @java.lang.SuppressWarnings("all") Parent(final ParentBuilder<?, ?> b) {
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
    public static @java.lang.SuppressWarnings("all") ParentBuilder<?, ?> builder() {
    	return new ParentBuilderImpl();
    }
  }

  public static @lombok.experimental.SuperBuilder class Child extends Parent {
    double field3;

    @java.lang.SuppressWarnings("all")
    protected Child(final ChildBuilder<?, ?> b) {
      super(b);
      this.field3 = b.field3;
    }


    @java.lang.SuppressWarnings("all")
    public static abstract class ChildBuilder<C extends Child, B extends ChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
      @java.lang.SuppressWarnings("all")
      private double field3;

      @java.lang.Override
      @java.lang.SuppressWarnings("all")
      protected abstract B self();

      @java.lang.Override
      @java.lang.SuppressWarnings("all")
      public abstract C build();

      @java.lang.SuppressWarnings("all")
      public B field3(final double field3) {
        this.field3 = field3;
        return self();
      }

      @java.lang.Override
      @java.lang.SuppressWarnings("all")
      public java.lang.String toString() {
        return "SuperBuilderBasic.Child.ChildBuilder(super=" + super.toString() + ", field3=" + this.field3 + ")";
      }
    }


    @java.lang.SuppressWarnings("all")
    private static final class ChildBuilderImpl extends ChildBuilder<Child, ChildBuilderImpl> {
      @java.lang.SuppressWarnings("all")
      private ChildBuilderImpl() {
      }

      @java.lang.Override
      @java.lang.SuppressWarnings("all")
      protected ChildBuilderImpl self() {
        return this;
      }

      @java.lang.Override
      @java.lang.SuppressWarnings("all")
      public Child build() {
        return new Child(this);
      }
    }

    @java.lang.SuppressWarnings("all")
    public static ChildBuilder<?, ?> builder() {
      return new ChildBuilderImpl();
    }
  }

  public static void test() {
    Child x = Child.builder().field3(0.0).field1(5).item("").build();
  }
}
