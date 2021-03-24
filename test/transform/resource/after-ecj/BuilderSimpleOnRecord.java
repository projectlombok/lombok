// version 14:
import java.util.List;
public @lombok.Builder(access = lombok.AccessLevel.PROTECTED) record BuilderSimpleOnRecord(List l, String a)<T> {
  protected static @java.lang.SuppressWarnings("all") class BuilderSimpleOnRecordBuilder<T> {
    private @java.lang.SuppressWarnings("all") List<T> l;
    private @java.lang.SuppressWarnings("all") String a;
    @java.lang.SuppressWarnings("all") BuilderSimpleOnRecordBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderSimpleOnRecord.BuilderSimpleOnRecordBuilder<T> l(final List<T> l) {
      this.l = l;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderSimpleOnRecord.BuilderSimpleOnRecordBuilder<T> a(final String a) {
      this.a = a;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSimpleOnRecord<T> build() {
      return new BuilderSimpleOnRecord<T>(this.l, this.a);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((("BuilderSimpleOnRecord.BuilderSimpleOnRecordBuilder(l=" + this.l) + ", a=") + this.a) + ")");
    }
  }
/* Implicit */  private final List<T> l;
/* Implicit */  private final String a;
  public BuilderSimpleOnRecord(List<T> l, String a) {
    super();
    .l = l;
    .a = a;
  }
  protected static @java.lang.SuppressWarnings("all") <T>BuilderSimpleOnRecord.BuilderSimpleOnRecordBuilder<T> builder() {
    return new BuilderSimpleOnRecord.BuilderSimpleOnRecordBuilder<T>();
  }
}
