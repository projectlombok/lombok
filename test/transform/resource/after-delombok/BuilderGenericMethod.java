import java.util.List;
import java.util.*;
class BuilderGenericMethod<T> {
	public <N extends Number> Map<N, T> foo(int a, long b) {
		return null;
	}
	@java.lang.SuppressWarnings("all")
	public class MapBuilder<N extends Number> {
		@java.lang.SuppressWarnings("all")
		private int a;
		@java.lang.SuppressWarnings("all")
		private long b;
		@java.lang.SuppressWarnings("all")
		MapBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public MapBuilder<N> a(final int a) {
			this.a = a;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public MapBuilder<N> b(final long b) {
			this.b = b;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public Map<N, T> build() {
			return BuilderGenericMethod.this.<N>foo(a, b);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderGenericMethod.MapBuilder(a=" + this.a + ", b=" + this.b + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public <N extends Number> MapBuilder<N> builder() {
		return new MapBuilder<N>();
	}
}
