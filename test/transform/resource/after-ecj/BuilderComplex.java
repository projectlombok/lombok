import java.util.List;
import lombok.Builder;
class BuilderComplex {
  public static @java.lang.SuppressWarnings("all") class TestVoidName<T extends Number> {
    private @java.lang.SuppressWarnings("all") T number;
    private @java.lang.SuppressWarnings("all") int arg2;
    private @java.lang.SuppressWarnings("all") String arg3;
    private @java.lang.SuppressWarnings("all") BuilderComplex selfRef;
    @java.lang.SuppressWarnings("all") TestVoidName() {
      super();
    }
    public @java.lang.SuppressWarnings("all") TestVoidName<T> number(final T number) {
      this.number = number;
      return this;
    }
    public @java.lang.SuppressWarnings("all") TestVoidName<T> arg2(final int arg2) {
      this.arg2 = arg2;
      return this;
    }
    public @java.lang.SuppressWarnings("all") TestVoidName<T> arg3(final String arg3) {
      this.arg3 = arg3;
      return this;
    }
    public @java.lang.SuppressWarnings("all") TestVoidName<T> selfRef(final BuilderComplex selfRef) {
      this.selfRef = selfRef;
      return this;
    }
    public @java.lang.SuppressWarnings("all") void execute() {
      BuilderComplex.<T>testVoidWithGenerics(number, arg2, arg3, selfRef);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((((("BuilderComplex.TestVoidName(number=" + this.number) + ", arg2=") + this.arg2) + ", arg3=") + this.arg3) + ", selfRef=") + this.selfRef) + ")");
    }
  }
  BuilderComplex() {
    super();
  }
  private static @Builder(buildMethodName = "execute") <T extends Number>void testVoidWithGenerics(T number, int arg2, String arg3, BuilderComplex selfRef) {
  }
  public static @java.lang.SuppressWarnings("all") <T extends Number>TestVoidName<T> builder() {
    return new TestVoidName<T>();
  }
}
