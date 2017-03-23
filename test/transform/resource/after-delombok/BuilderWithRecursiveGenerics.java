import java.util.Set;
public class BuilderWithRecursiveGenerics {
	interface Inter<T, U extends Inter<T, U>> {
	}
	public static final class Test<Foo, Bar extends Set<Foo>, Quz extends Inter<Bar, Quz>> {
		private final Foo foo;
		private final Bar bar;
		@java.beans.ConstructorProperties({"foo", "bar"})
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		Test(final Foo foo, final Bar bar) {
			this.foo = foo;
			this.bar = bar;
		}
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		public static class TestBuilder<Foo, Bar extends Set<Foo>, Quz extends Inter<Bar, Quz>> {
			@java.lang.SuppressWarnings("all")
			@javax.annotation.Generated("lombok")
			private Foo foo;
			@java.lang.SuppressWarnings("all")
			@javax.annotation.Generated("lombok")
			private Bar bar;
			@java.lang.SuppressWarnings("all")
			@javax.annotation.Generated("lombok")
			TestBuilder() {
			}
			@java.lang.SuppressWarnings("all")
			@javax.annotation.Generated("lombok")
			public TestBuilder<Foo, Bar, Quz> foo(final Foo foo) {
				this.foo = foo;
				return this;
			}
			@java.lang.SuppressWarnings("all")
			@javax.annotation.Generated("lombok")
			public TestBuilder<Foo, Bar, Quz> bar(final Bar bar) {
				this.bar = bar;
				return this;
			}
			@java.lang.SuppressWarnings("all")
			@javax.annotation.Generated("lombok")
			public Test<Foo, Bar, Quz> build() {
				return new Test<Foo, Bar, Quz>(foo, bar);
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@javax.annotation.Generated("lombok")
			public java.lang.String toString() {
				return "BuilderWithRecursiveGenerics.Test.TestBuilder(foo=" + this.foo + ", bar=" + this.bar + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		public static <Foo, Bar extends Set<Foo>, Quz extends Inter<Bar, Quz>> TestBuilder<Foo, Bar, Quz> builder() {
			return new TestBuilder<Foo, Bar, Quz>();
		}
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		public Foo getFoo() {
			return this.foo;
		}
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		public Bar getBar() {
			return this.bar;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		public boolean equals(final java.lang.Object o) {
			if (o == this) return true;
			if (!(o instanceof BuilderWithRecursiveGenerics.Test)) return false;
			final BuilderWithRecursiveGenerics.Test<?, ?, ?> other = (BuilderWithRecursiveGenerics.Test<?, ?, ?>) o;
			final java.lang.Object this$foo = this.getFoo();
			final java.lang.Object other$foo = other.getFoo();
			if (this$foo == null ? other$foo != null : !this$foo.equals(other$foo)) return false;
			final java.lang.Object this$bar = this.getBar();
			final java.lang.Object other$bar = other.getBar();
			if (this$bar == null ? other$bar != null : !this$bar.equals(other$bar)) return false;
			return true;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			final java.lang.Object $foo = this.getFoo();
			result = result * PRIME + ($foo == null ? 43 : $foo.hashCode());
			final java.lang.Object $bar = this.getBar();
			result = result * PRIME + ($bar == null ? 43 : $bar.hashCode());
			return result;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		public java.lang.String toString() {
			return "BuilderWithRecursiveGenerics.Test(foo=" + this.getFoo() + ", bar=" + this.getBar() + ")";
		}
	}
}
