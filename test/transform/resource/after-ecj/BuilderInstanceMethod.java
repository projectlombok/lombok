import java.util.List;
class BuilderInstanceMethod<T> {
  public @java.lang.SuppressWarnings("all") class StringBuilder {
    private @java.lang.SuppressWarnings("all") int show;
    private @java.lang.SuppressWarnings("all") int yes;
    private @java.lang.SuppressWarnings("all") List<T> also;
    private @java.lang.SuppressWarnings("all") int $andMe;
    @java.lang.SuppressWarnings("all") StringBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderInstanceMethod<T>.StringBuilder show(final int show) {
      this.show = show;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderInstanceMethod<T>.StringBuilder yes(final int yes) {
      this.yes = yes;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderInstanceMethod<T>.StringBuilder also(final List<T> also) {
      this.also = also;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderInstanceMethod<T>.StringBuilder $andMe(final int $andMe) {
      this.$andMe = $andMe;
      return this;
    }
    public @java.lang.SuppressWarnings("all") String build() {
      return BuilderInstanceMethod.this.create(this.show, this.yes, this.also, this.$andMe);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((((("BuilderInstanceMethod.StringBuilder(show=" + this.show) + ", yes=") + this.yes) + ", also=") + this.also) + ", $andMe=") + this.$andMe) + ")");
    }
  }
  BuilderInstanceMethod() {
    super();
  }
  public @lombok.Builder String create(int show, final int yes, List<T> also, int $andMe) {
    return (((("" + show) + yes) + also) + $andMe);
  }
  public @java.lang.SuppressWarnings("all") BuilderInstanceMethod<T>.StringBuilder builder() {
    return this.new StringBuilder();
  }
}
