// version 14:
import java.util.List;
public @lombok.Builder(access = lombok.AccessLevel.PROTECTED) record BuilderSimpleOnRecord(List l, String a)<T> {
  protected static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderSimpleOnRecordBuilder<T> {
    private @java.lang.SuppressWarnings("all") @lombok.Generated List<T> l;
    private @java.lang.SuppressWarnings("all") @lombok.Generated String a;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSimpleOnRecordBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSimpleOnRecord.BuilderSimpleOnRecordBuilder<T> l(final List<T> l) {
      this.l = l;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSimpleOnRecord.BuilderSimpleOnRecordBuilder<T> a(final String a) {
      this.a = a;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSimpleOnRecord<T> build() {
      return new BuilderSimpleOnRecord<T>(this.l, this.a);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((("BuilderSimpleOnRecord.BuilderSimpleOnRecordBuilder(l=" + this.l) + ", a=") + this.a) + ")");
    }
  }
  protected static @java.lang.SuppressWarnings("all") @lombok.Generated <T>BuilderSimpleOnRecord.BuilderSimpleOnRecordBuilder<T> builder() {
    return new BuilderSimpleOnRecord.BuilderSimpleOnRecordBuilder<T>();
  }
}
