import lombok.NoArgsConstructor;
public @NoArgsConstructor(force = true) class NoArgsConstructorForce {
  private final int[] i;
  private final Object[] o;
  private final java.util.List<?>[] fullQualifiedList;
  public @java.lang.SuppressWarnings("all") NoArgsConstructorForce() {
    super();
    this.i = null;
    this.o = null;
    this.fullQualifiedList = null;
  }
}