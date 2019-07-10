interface EqualsAndHashCodeNestedShadow {
	interface Foo {
	}
	class Bar {
		public static class Foo extends Bar implements EqualsAndHashCodeNestedShadow.Foo {
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public boolean equals(final java.lang.Object o) {
				if (o == this) return true;
				if (!(o instanceof EqualsAndHashCodeNestedShadow.Bar.Foo)) return false;
				final EqualsAndHashCodeNestedShadow.Bar.Foo other = (EqualsAndHashCodeNestedShadow.Bar.Foo) o;
				if (!other.canEqual((java.lang.Object) this)) return false;
				return true;
			}
			@java.lang.SuppressWarnings("all")
			protected boolean canEqual(final java.lang.Object other) {
				return other instanceof EqualsAndHashCodeNestedShadow.Bar.Foo;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public int hashCode() {
				final int result = 1;
				return result;
			}
		}
	}
	class Baz {
		public static class Foo<T> extends Bar implements EqualsAndHashCodeNestedShadow.Foo {
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public boolean equals(final java.lang.Object o) {
				if (o == this) return true;
				if (!(o instanceof EqualsAndHashCodeNestedShadow.Baz.Foo)) return false;
				final EqualsAndHashCodeNestedShadow.Baz.Foo<?> other = (EqualsAndHashCodeNestedShadow.Baz.Foo<?>) o;
				if (!other.canEqual((java.lang.Object) this)) return false;
				return true;
			}
			@java.lang.SuppressWarnings("all")
			protected boolean canEqual(final java.lang.Object other) {
				return other instanceof EqualsAndHashCodeNestedShadow.Baz.Foo;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public int hashCode() {
				final int result = 1;
				return result;
			}
		}
	}
}
