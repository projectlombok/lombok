//version 9:
import java.lang.annotation.*;
@lombok.RequiredArgsConstructor @lombok.Getter @lombok.Setter @org.jspecify.annotations.NullMarked class NullMarkedPlain {
  int i;
  String s;
  @org.jspecify.annotations.Nullable Object o;
  public @java.lang.SuppressWarnings("all") @lombok.Generated NullMarkedPlain() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated int getI() {
    return this.i;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated String getS() {
    return this.s;
  }
  public @org.jspecify.annotations.Nullable @java.lang.SuppressWarnings("all") @lombok.Generated Object getO() {
    return this.o;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setI(final int i) {
    this.i = i;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setS(final String s) {
    if ((s == null))
        {
          throw new java.lang.NullPointerException("s is marked non-null but is null");
        }
    this.s = s;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setO(final @org.jspecify.annotations.Nullable Object o) {
    this.o = o;
  }
}
