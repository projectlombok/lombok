interface EqualsAndHashCodeNestedShadow {
  interface Foo {
  }
  class Bar {
    public static @lombok.EqualsAndHashCode(callSuper = false) class Foo extends Bar implements EqualsAndHashCodeNestedShadow.Foo {
      public Foo() {
        super();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
        if ((o == this))
            return true;
        if ((! (o instanceof EqualsAndHashCodeNestedShadow.Bar.Foo)))
            return false;
        final EqualsAndHashCodeNestedShadow.Bar.Foo other = (EqualsAndHashCodeNestedShadow.Bar.Foo) o;
        if ((! other.canEqual((java.lang.Object) this)))
            return false;
        return true;
      }
      protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
        return (other instanceof EqualsAndHashCodeNestedShadow.Bar.Foo);
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
        final int result = 1;
        return result;
      }
    }
    Bar() {
      super();
    }
  }
  class Baz {
    public static @lombok.EqualsAndHashCode(callSuper = false) class Foo<T> extends Bar implements EqualsAndHashCodeNestedShadow.Foo {
      public Foo() {
        super();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
        if ((o == this))
            return true;
        if ((! (o instanceof EqualsAndHashCodeNestedShadow.Baz.Foo)))
            return false;
        final EqualsAndHashCodeNestedShadow.Baz.Foo<?> other = (EqualsAndHashCodeNestedShadow.Baz.Foo<?>) o;
        if ((! other.canEqual((java.lang.Object) this)))
            return false;
        return true;
      }
      protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
        return (other instanceof EqualsAndHashCodeNestedShadow.Baz.Foo);
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
        final int result = 1;
        return result;
      }
    }
    Baz() {
      super();
    }
  }
}