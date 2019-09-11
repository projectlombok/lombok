import java.util.List;
import lombok.Singular;
class CheckerFrameworkSuperBuilder {
  public static @lombok.experimental.SuperBuilder class Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ParentBuilder<C extends Parent, B extends ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") int x$value;
      private @java.lang.SuppressWarnings("all") boolean x$set;
      private @java.lang.SuppressWarnings("all") int y;
      private @java.lang.SuppressWarnings("all") int z;
      private @java.lang.SuppressWarnings("all") java.util.ArrayList<String> names;
      public ParentBuilder() {
      }
      protected abstract @org.checkerframework.checker.builder.qual.ReturnsReceiver @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") B self();
      public abstract @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") C build(final @org.checkerframework.checker.builder.qual.CalledMethods({"y", "z"}) ParentBuilder this);
      public @org.checkerframework.checker.builder.qual.ReturnsReceiver @java.lang.SuppressWarnings("all") B x(final @org.checkerframework.checker.builder.qual.NotCalledMethods("x") ParentBuilder this, final int x) {
        this.x$value = x;
        x$set = true;
        return self();
      }
      public @org.checkerframework.checker.builder.qual.ReturnsReceiver @java.lang.SuppressWarnings("all") B y(final @org.checkerframework.checker.builder.qual.NotCalledMethods("y") ParentBuilder this, final int y) {
        this.y = y;
        return self();
      }
      public @org.checkerframework.checker.builder.qual.ReturnsReceiver @java.lang.SuppressWarnings("all") B z(final @org.checkerframework.checker.builder.qual.NotCalledMethods("z") ParentBuilder this, final int z) {
        this.z = z;
        return self();
      }
      public @org.checkerframework.checker.builder.qual.ReturnsReceiver @java.lang.SuppressWarnings("all") B name(final String name) {
        if ((this.names == null))
            this.names = new java.util.ArrayList<String>();
        this.names.add(name);
        return self();
      }
      public @org.checkerframework.checker.builder.qual.ReturnsReceiver @java.lang.SuppressWarnings("all") B names(final java.util.Collection<? extends String> names) {
        if ((this.names == null))
            this.names = new java.util.ArrayList<String>();
        this.names.addAll(names);
        return self();
      }
      public @org.checkerframework.checker.builder.qual.ReturnsReceiver @java.lang.SuppressWarnings("all") B clearNames() {
        if ((this.names != null))
            this.names.clear();
        return self();
      }
      public @java.lang.Override @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((((((("CheckerFrameworkSuperBuilder.Parent.ParentBuilder(x$value=" + this.x$value) + ", y=") + this.y) + ", z=") + this.z) + ", names=") + this.names) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ParentBuilderImpl extends ParentBuilder<Parent, ParentBuilderImpl> {
      private ParentBuilderImpl() {
      }
      protected @java.lang.Override @org.checkerframework.checker.builder.qual.ReturnsReceiver @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") ParentBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") Parent build(final @org.checkerframework.checker.builder.qual.CalledMethods({"y", "z"}) ParentBuilderImpl this) {
        return new Parent(this);
      }
    }
    @lombok.Builder.Default int x;
    int y;
    int z;
    @Singular List<String> names;
    private static @java.lang.SuppressWarnings("all") int $default$x() {
      return 5;
    }
    protected @org.checkerframework.common.aliasing.qual.Unique @java.lang.SuppressWarnings("all") Parent(final ParentBuilder<?, ?> b) {
      super();
      if (b.x$set)
          this.x = b.x$value;
      else
          this.x = Parent.$default$x();
      this.y = b.y;
      this.z = b.z;
      java.util.List<String> names;
      switch (((b.names == null) ? 0 : b.names.size())) {
      case 0 :
          names = java.util.Collections.emptyList();
          break;
      case 1 :
          names = java.util.Collections.singletonList(b.names.get(0));
          break;
      default :
          names = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(b.names));
      }
      this.names = names;
    }
    public static @org.checkerframework.common.aliasing.qual.Unique @java.lang.SuppressWarnings("all") ParentBuilder<?, ?> builder() {
      return new ParentBuilderImpl();
    }
  }
  public static @lombok.experimental.SuperBuilder class Child extends Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ChildBuilder<C extends Child, B extends ChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") int a$value;
      private @java.lang.SuppressWarnings("all") boolean a$set;
      private @java.lang.SuppressWarnings("all") int b;
      public ChildBuilder() {
      }
      protected abstract @java.lang.Override @org.checkerframework.checker.builder.qual.ReturnsReceiver @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.Override @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") C build(final @org.checkerframework.checker.builder.qual.CalledMethods("b") ChildBuilder this);
      public @org.checkerframework.checker.builder.qual.ReturnsReceiver @java.lang.SuppressWarnings("all") B a(final @org.checkerframework.checker.builder.qual.NotCalledMethods("a") ChildBuilder this, final int a) {
        this.a$value = a;
        a$set = true;
        return self();
      }
      public @org.checkerframework.checker.builder.qual.ReturnsReceiver @java.lang.SuppressWarnings("all") B b(final @org.checkerframework.checker.builder.qual.NotCalledMethods("b") ChildBuilder this, final int b) {
        this.b = b;
        return self();
      }
      public @java.lang.Override @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((((("CheckerFrameworkSuperBuilder.Child.ChildBuilder(super=" + super.toString()) + ", a$value=") + this.a$value) + ", b=") + this.b) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ChildBuilderImpl extends ChildBuilder<Child, ChildBuilderImpl> {
      private ChildBuilderImpl() {
      }
      protected @java.lang.Override @org.checkerframework.checker.builder.qual.ReturnsReceiver @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") ChildBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") Child build(final @org.checkerframework.checker.builder.qual.CalledMethods("b") ChildBuilderImpl this) {
        return new Child(this);
      }
    }
    @lombok.Builder.Default int a;
    int b;
    private static @java.lang.SuppressWarnings("all") int $default$a() {
      return 1;
    }
    protected @org.checkerframework.common.aliasing.qual.Unique @java.lang.SuppressWarnings("all") Child(final ChildBuilder<?, ?> b) {
      super(b);
      if (b.a$set)
          this.a = b.a$value;
      else
          this.a = Child.$default$a();
      this.b = b.b;
    }
    public static @org.checkerframework.common.aliasing.qual.Unique @java.lang.SuppressWarnings("all") ChildBuilder<?, ?> builder() {
      return new ChildBuilderImpl();
    }
  }
  CheckerFrameworkSuperBuilder() {
  }
}
