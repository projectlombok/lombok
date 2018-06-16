public class PrivateNoArgsConstructor {
  private static class Base {
    private Base() {
      super();
    }
  }
  public static @lombok.Data class PrivateNoArgsConstructorNotOnExtends extends Base {
    private final int a;
    public @java.lang.SuppressWarnings("all") int getA() {
      return this.a;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
      if ((o == this))
          return true;
      if ((! (o instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorNotOnExtends)))
          return false;
      final PrivateNoArgsConstructor.PrivateNoArgsConstructorNotOnExtends other = (PrivateNoArgsConstructor.PrivateNoArgsConstructorNotOnExtends) o;
      if ((! other.canEqual((java.lang.Object) this)))
          return false;
      if ((! super.equals(o)))
          return false;
      if ((this.getA() != other.getA()))
          return false;
      return true;
    }
    protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
      return (other instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorNotOnExtends);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
      final int PRIME = 59;
      int result = super.hashCode();
      result = ((result * PRIME) + this.getA());
      return result;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("PrivateNoArgsConstructor.PrivateNoArgsConstructorNotOnExtends(a=" + this.getA()) + ")");
    }
    public @java.lang.SuppressWarnings("all") PrivateNoArgsConstructorNotOnExtends(final int a) {
      super();
      this.a = a;
    }
  }
  public static @lombok.Data class PrivateNoArgsConstructorOnExtendsObject extends Object {
    private final int b;
    public @java.lang.SuppressWarnings("all") int getB() {
      return this.b;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
      if ((o == this))
          return true;
      if ((! (o instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorOnExtendsObject)))
          return false;
      final PrivateNoArgsConstructor.PrivateNoArgsConstructorOnExtendsObject other = (PrivateNoArgsConstructor.PrivateNoArgsConstructorOnExtendsObject) o;
      if ((! other.canEqual((java.lang.Object) this)))
          return false;
      if ((this.getB() != other.getB()))
          return false;
      return true;
    }
    protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
      return (other instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorOnExtendsObject);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
      final int PRIME = 59;
      int result = 1;
      result = ((result * PRIME) + this.getB());
      return result;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("PrivateNoArgsConstructor.PrivateNoArgsConstructorOnExtendsObject(b=" + this.getB()) + ")");
    }
    public @java.lang.SuppressWarnings("all") PrivateNoArgsConstructorOnExtendsObject(final int b) {
      super();
      this.b = b;
    }
    private @java.lang.SuppressWarnings("all") PrivateNoArgsConstructorOnExtendsObject() {
      super();
      this.b = 0;
    }
  }
  public static @lombok.NoArgsConstructor(force = true) @lombok.Data @lombok.RequiredArgsConstructor class PrivateNoArgsConstructorExplicitBefore {
    private final int c;
    public @java.lang.SuppressWarnings("all") PrivateNoArgsConstructorExplicitBefore() {
      super();
      this.c = 0;
    }
    public @java.lang.SuppressWarnings("all") int getC() {
      return this.c;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
      if ((o == this))
          return true;
      if ((! (o instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitBefore)))
          return false;
      final PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitBefore other = (PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitBefore) o;
      if ((! other.canEqual((java.lang.Object) this)))
          return false;
      if ((this.getC() != other.getC()))
          return false;
      return true;
    }
    protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
      return (other instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitBefore);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
      final int PRIME = 59;
      int result = 1;
      result = ((result * PRIME) + this.getC());
      return result;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitBefore(c=" + this.getC()) + ")");
    }
    public @java.lang.SuppressWarnings("all") PrivateNoArgsConstructorExplicitBefore(final int c) {
      super();
      this.c = c;
    }
  }
  public static @lombok.Data @lombok.NoArgsConstructor(force = true) @lombok.RequiredArgsConstructor class PrivateNoArgsConstructorExplicitAfter {
    private final int d;
    public @java.lang.SuppressWarnings("all") int getD() {
      return this.d;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
      if ((o == this))
          return true;
      if ((! (o instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitAfter)))
          return false;
      final PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitAfter other = (PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitAfter) o;
      if ((! other.canEqual((java.lang.Object) this)))
          return false;
      if ((this.getD() != other.getD()))
          return false;
      return true;
    }
    protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
      return (other instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitAfter);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
      final int PRIME = 59;
      int result = 1;
      result = ((result * PRIME) + this.getD());
      return result;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitAfter(d=" + this.getD()) + ")");
    }
    public @java.lang.SuppressWarnings("all") PrivateNoArgsConstructorExplicitAfter() {
      super();
      this.d = 0;
    }
    public @java.lang.SuppressWarnings("all") PrivateNoArgsConstructorExplicitAfter(final int d) {
      super();
      this.d = d;
    }
  }
  public static @lombok.Data @lombok.NoArgsConstructor(access = lombok.AccessLevel.NONE) @lombok.RequiredArgsConstructor class PrivateNoArgsConstructorExplicitNone {
    private final int e;
    public @java.lang.SuppressWarnings("all") int getE() {
      return this.e;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
      if ((o == this))
          return true;
      if ((! (o instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitNone)))
          return false;
      final PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitNone other = (PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitNone) o;
      if ((! other.canEqual((java.lang.Object) this)))
          return false;
      if ((this.getE() != other.getE()))
          return false;
      return true;
    }
    protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
      return (other instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitNone);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
      final int PRIME = 59;
      int result = 1;
      result = ((result * PRIME) + this.getE());
      return result;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitNone(e=" + this.getE()) + ")");
    }
    public @java.lang.SuppressWarnings("all") PrivateNoArgsConstructorExplicitNone(final int e) {
      super();
      this.e = e;
    }
  }
  public static @lombok.Data class PrivateNoArgsConstructorNoFields {
    public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
      if ((o == this))
          return true;
      if ((! (o instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorNoFields)))
          return false;
      final PrivateNoArgsConstructor.PrivateNoArgsConstructorNoFields other = (PrivateNoArgsConstructor.PrivateNoArgsConstructorNoFields) o;
      if ((! other.canEqual((java.lang.Object) this)))
          return false;
      return true;
    }
    protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
      return (other instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorNoFields);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
      int result = 1;
      return result;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return "PrivateNoArgsConstructor.PrivateNoArgsConstructorNoFields()";
    }
    public @java.lang.SuppressWarnings("all") PrivateNoArgsConstructorNoFields() {
      super();
    }
  }
  public PrivateNoArgsConstructor() {
    super();
  }
}
