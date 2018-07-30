public class NoPrivateNoArgsConstructor {
  public static @lombok.Data class NoPrivateNoArgsConstructorData {
    private final int i;
    public @java.lang.SuppressWarnings("all") int getI() {
      return this.i;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
      if ((o == this))
          return true;
      if ((! (o instanceof NoPrivateNoArgsConstructor.NoPrivateNoArgsConstructorData)))
          return false;
      final NoPrivateNoArgsConstructor.NoPrivateNoArgsConstructorData other = (NoPrivateNoArgsConstructor.NoPrivateNoArgsConstructorData) o;
      if ((! other.canEqual((java.lang.Object) this)))
          return false;
      if ((this.getI() != other.getI()))
          return false;
      return true;
    }
    protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
      return (other instanceof NoPrivateNoArgsConstructor.NoPrivateNoArgsConstructorData);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
      final int PRIME = 59;
      int result = 1;
      result = ((result * PRIME) + this.getI());
      return result;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("NoPrivateNoArgsConstructor.NoPrivateNoArgsConstructorData(i=" + this.getI()) + ")");
    }
    public @java.lang.SuppressWarnings("all") NoPrivateNoArgsConstructorData(final int i) {
      super();
      this.i = i;
    }
  }
  public static final @lombok.Value class NoPrivateNoArgsConstructorValue {
    private final int i;
    public @java.lang.SuppressWarnings("all") int getI() {
      return this.i;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
      if ((o == this))
          return true;
      if ((! (o instanceof NoPrivateNoArgsConstructor.NoPrivateNoArgsConstructorValue)))
          return false;
      final NoPrivateNoArgsConstructor.NoPrivateNoArgsConstructorValue other = (NoPrivateNoArgsConstructor.NoPrivateNoArgsConstructorValue) o;
      if ((this.getI() != other.getI()))
          return false;
      return true;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
      final int PRIME = 59;
      int result = 1;
      result = ((result * PRIME) + this.getI());
      return result;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("NoPrivateNoArgsConstructor.NoPrivateNoArgsConstructorValue(i=" + this.getI()) + ")");
    }
    public @java.lang.SuppressWarnings("all") NoPrivateNoArgsConstructorValue(final int i) {
      super();
      this.i = i;
    }
  }
  public NoPrivateNoArgsConstructor() {
    super();
  }
}