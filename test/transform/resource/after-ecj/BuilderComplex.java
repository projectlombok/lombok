import java.util.List;
import lombok.experimental.Builder;
class BuilderComplex {
  public static @java.lang.SuppressWarnings("all") class VoidBuilder<T extends Number> {
    private T number;
    private int arg2;
    private String arg3;
    private BuilderComplex selfRef;
    @java.lang.SuppressWarnings("all") VoidBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") VoidBuilder<T> number(final T number) {
      this.number = number;
      return this;
    }
    public @java.lang.SuppressWarnings("all") VoidBuilder<T> arg2(final int arg2) {
      this.arg2 = arg2;
      return this;
    }
    public @java.lang.SuppressWarnings("all") VoidBuilder<T> arg3(final String arg3) {
      this.arg3 = arg3;
      return this;
    }
    public @java.lang.SuppressWarnings("all") VoidBuilder<T> selfRef(final BuilderComplex selfRef) {
      this.selfRef = selfRef;
      return this;
    }
    public @java.lang.SuppressWarnings("all") void execute() {
      BuilderComplex.<T>testVoidWithGenerics(number, arg2, arg3, selfRef);
    }
  }
  BuilderComplex() {
    super();
  }
  private static @Builder(buildMethodName = "execute") <T extends Number>void testVoidWithGenerics(T number, int arg2, String arg3, BuilderComplex selfRef) {
  }
  public static @java.lang.SuppressWarnings("all") <T extends Number>VoidBuilder<T> builder() {
    return new VoidBuilder<T>();
  }
}
