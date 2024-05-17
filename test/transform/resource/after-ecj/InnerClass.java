class A {
  @lombok.AllArgsConstructor class B {
    String s;
    public @java.lang.SuppressWarnings("all") @lombok.Generated B(final String s) {
      super();
      this.s = s;
    }
  }
  A() {
    super();
  }
}
class C {
  final @lombok.Value class D {
    private final A a;
    A.B test(String s) {
      return a.new B(s) {
  x(<no type> $anonymous0) {
    super($anonymous0);
  }
};
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated A getA() {
      return this.a;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
      if ((o == this))
          return true;
      if ((! (o instanceof C.D)))
          return false;
      final C.D other = (C.D) o;
      final java.lang.Object this$a = this.getA();
      final java.lang.Object other$a = other.getA();
      if (((this$a == null) ? (other$a != null) : (! this$a.equals(other$a))))
          return false;
      return true;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final java.lang.Object $a = this.getA();
      result = ((result * PRIME) + (($a == null) ? 43 : $a.hashCode()));
      return result;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (("C.D(a=" + this.getA()) + ")");
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated D(final A a) {
      super();
      this.a = a;
    }
  }
  C() {
    super();
  }
}
