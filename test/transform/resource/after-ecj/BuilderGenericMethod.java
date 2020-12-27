import java.util.List;
import lombok.Builder;
import java.util.*;
class BuilderGenericMethod<T> {
  public @java.lang.SuppressWarnings("all") class MapBuilder<N extends Number> {
    private @java.lang.SuppressWarnings("all") int a;
    private @java.lang.SuppressWarnings("all") long b;
    @java.lang.SuppressWarnings("all") MapBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderGenericMethod<T>.MapBuilder<N> a(final int a) {
      this.a = a;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderGenericMethod<T>.MapBuilder<N> b(final long b) {
      this.b = b;
      return this;
    }
    public @java.lang.SuppressWarnings("all") Map<N, T> build() {
      return BuilderGenericMethod.this.<N>foo(this.a, this.b);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((("BuilderGenericMethod.MapBuilder(a=" + this.a) + ", b=") + this.b) + ")");
    }
  }
  BuilderGenericMethod() {
    super();
  }
  public @Builder <N extends Number>Map<N, T> foo(int a, long b) {
    return null;
  }
  public @java.lang.SuppressWarnings("all") <N extends Number>BuilderGenericMethod<T>.MapBuilder<N> builder() {
    return this.new MapBuilder<N>();
  }
}
