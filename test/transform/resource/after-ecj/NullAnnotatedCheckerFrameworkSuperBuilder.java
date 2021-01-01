import java.util.List;
import lombok.Singular;
class NullAnnotatedCheckerFrameworkSuperBuilder {
  public static @lombok.experimental.SuperBuilder class Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ParentBuilder<C extends NullAnnotatedCheckerFrameworkSuperBuilder.Parent, B extends NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") int x$value;
      private @java.lang.SuppressWarnings("all") boolean x$set;
      private @java.lang.SuppressWarnings("all") int y;
      private @java.lang.SuppressWarnings("all") int z;
      private @java.lang.SuppressWarnings("all") java.util.ArrayList<String> names;
      public ParentBuilder() {
        super();
      }
      protected abstract @org.checkerframework.common.returnsreceiver.qual.This @org.checkerframework.dataflow.qual.Pure @java.lang.SuppressWarnings("all") B self();
      public abstract @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") C build(NullAnnotatedCheckerFrameworkSuperBuilder.Parent. @org.checkerframework.checker.calledmethods.qual.CalledMethods({"y", "z"}) ParentBuilder<C, B> this);
      /**
       * @return {@code this}.
       */
      public @org.checkerframework.common.returnsreceiver.qual.This @java.lang.SuppressWarnings("all") B x(NullAnnotatedCheckerFrameworkSuperBuilder.Parent. @org.checkerframework.checker.calledmethods.qual.NotCalledMethods("x") ParentBuilder<C, B> this, final int x) {
        this.x$value = x;
        x$set = true;
        return self();
      }
      /**
       * @return {@code this}.
       */
      public @org.checkerframework.common.returnsreceiver.qual.This @java.lang.SuppressWarnings("all") B y(NullAnnotatedCheckerFrameworkSuperBuilder.Parent. @org.checkerframework.checker.calledmethods.qual.NotCalledMethods("y") ParentBuilder<C, B> this, final int y) {
        this.y = y;
        return self();
      }
      /**
       * @return {@code this}.
       */
      public @org.checkerframework.common.returnsreceiver.qual.This @java.lang.SuppressWarnings("all") B z(NullAnnotatedCheckerFrameworkSuperBuilder.Parent. @org.checkerframework.checker.calledmethods.qual.NotCalledMethods("z") ParentBuilder<C, B> this, final int z) {
        this.z = z;
        return self();
      }
      public @org.checkerframework.common.returnsreceiver.qual.This @java.lang.SuppressWarnings("all") B name(final String name) {
        if ((this.names == null))
            this.names = new java.util.ArrayList<String>();
        this.names.add(name);
        return self();
      }
      public @org.checkerframework.common.returnsreceiver.qual.This @java.lang.SuppressWarnings("all") B names(final java.util.Collection<? extends String> names) {
        if ((names == null))
            {
              throw new java.lang.NullPointerException("names cannot be null");
            }
        if ((this.names == null))
            this.names = new java.util.ArrayList<String>();
        this.names.addAll(names);
        return self();
      }
      public @org.checkerframework.common.returnsreceiver.qual.This @java.lang.SuppressWarnings("all") B clearNames() {
        if ((this.names != null))
            this.names.clear();
        return self();
      }
      public @java.lang.Override @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((((((("NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilder(x$value=" + this.x$value) + ", y=") + this.y) + ", z=") + this.z) + ", names=") + this.names) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ParentBuilderImpl extends NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilder<NullAnnotatedCheckerFrameworkSuperBuilder.Parent, NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilderImpl> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @org.checkerframework.common.returnsreceiver.qual.This @org.checkerframework.dataflow.qual.Pure @java.lang.SuppressWarnings("all") NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") NullAnnotatedCheckerFrameworkSuperBuilder.Parent build(NullAnnotatedCheckerFrameworkSuperBuilder.Parent.@org.checkerframework.checker.calledmethods.qual.CalledMethods({"y", "z"}) ParentBuilderImpl this) {
        return new NullAnnotatedCheckerFrameworkSuperBuilder.Parent(this);
      }
    }
    @lombok.Builder.Default int x;
    int y;
    int z;
    @Singular List<String> names;
    private static @java.lang.SuppressWarnings("all") int $default$x() {
      return 5;
    }
    protected @java.lang.SuppressWarnings("all") Parent(final NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilder<?, ?> b) {
      super();
      if (b.x$set)
          this.x = b.x$value;
      else
          this.x = NullAnnotatedCheckerFrameworkSuperBuilder.Parent.$default$x();
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
    public static @java.lang.SuppressWarnings("all") NullAnnotatedCheckerFrameworkSuperBuilder.Parent. @org.checkerframework.common.aliasing.qual.Unique ParentBuilder<?, ?> builder() {
      return new NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilderImpl();
    }
  }
  public static @lombok.experimental.SuperBuilder class ZChild extends Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ZChildBuilder<C extends NullAnnotatedCheckerFrameworkSuperBuilder.ZChild, B extends NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") int a$value;
      private @java.lang.SuppressWarnings("all") boolean a$set;
      private @java.lang.SuppressWarnings("all") int b;
      public ZChildBuilder() {
        super();
      }
      protected abstract @java.lang.Override @org.checkerframework.common.returnsreceiver.qual.This @org.checkerframework.dataflow.qual.Pure @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.Override @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") C build(NullAnnotatedCheckerFrameworkSuperBuilder.ZChild. @org.checkerframework.checker.calledmethods.qual.CalledMethods("b") ZChildBuilder<C, B> this);
      /**
       * @return {@code this}.
       */
      public @org.checkerframework.common.returnsreceiver.qual.This @java.lang.SuppressWarnings("all") B a(NullAnnotatedCheckerFrameworkSuperBuilder.ZChild. @org.checkerframework.checker.calledmethods.qual.NotCalledMethods("a") ZChildBuilder<C, B> this, final int a) {
        this.a$value = a;
        a$set = true;
        return self();
      }
      /**
       * @return {@code this}.
       */
      public @org.checkerframework.common.returnsreceiver.qual.This @java.lang.SuppressWarnings("all") B b(NullAnnotatedCheckerFrameworkSuperBuilder.ZChild. @org.checkerframework.checker.calledmethods.qual.NotCalledMethods("b") ZChildBuilder<C, B> this, final int b) {
        this.b = b;
        return self();
      }
      public @java.lang.Override @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((((("NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilder(super=" + super.toString()) + ", a$value=") + this.a$value) + ", b=") + this.b) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ZChildBuilderImpl extends NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilder<NullAnnotatedCheckerFrameworkSuperBuilder.ZChild, NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilderImpl> {
      private ZChildBuilderImpl() {
        super();
      }
      protected @java.lang.Override @org.checkerframework.common.returnsreceiver.qual.This @org.checkerframework.dataflow.qual.Pure @java.lang.SuppressWarnings("all") NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") NullAnnotatedCheckerFrameworkSuperBuilder.ZChild build(NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.@org.checkerframework.checker.calledmethods.qual.CalledMethods("b") ZChildBuilderImpl this) {
        return new NullAnnotatedCheckerFrameworkSuperBuilder.ZChild(this);
      }
    }
    @lombok.Builder.Default int a;
    int b;
    private static @java.lang.SuppressWarnings("all") int $default$a() {
      return 1;
    }
    protected @java.lang.SuppressWarnings("all") ZChild(final NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilder<?, ?> b) {
      super(b);
      if (b.a$set)
          this.a = b.a$value;
      else
          this.a = NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.$default$a();
      this.b = b.b;
    }
    public static @java.lang.SuppressWarnings("all") NullAnnotatedCheckerFrameworkSuperBuilder.ZChild. @org.checkerframework.common.aliasing.qual.Unique ZChildBuilder<?, ?> builder() {
      return new NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilderImpl();
    }
  }
  NullAnnotatedCheckerFrameworkSuperBuilder() {
    super();
  }
}
