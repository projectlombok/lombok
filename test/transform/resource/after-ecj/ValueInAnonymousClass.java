import lombok.Value;
public class ValueInAnonymousClass {
  Object annonymous = new Object() {
    final @Value class Inner {
      private final String string;
      public @java.lang.SuppressWarnings("all") @lombok.Generated String getString() {
        return this.string;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
        if ((o == this))
            return true;
        if ((! (o instanceof Inner)))
            return false;
        final Inner other = (Inner) o;
        final java.lang.Object this$string = this.getString();
        final java.lang.Object other$string = other.getString();
        if (((this$string == null) ? (other$string != null) : (! this$string.equals(other$string))))
            return false;
        return true;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $string = this.getString();
        result = ((result * PRIME) + (($string == null) ? 43 : $string.hashCode()));
        return result;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (("Inner(string=" + this.getString()) + ")");
      }
      public @java.lang.SuppressWarnings("all") @lombok.Generated Inner(final String string) {
        super();
        this.string = string;
      }
    }
    x() {
      super();
    }
  };
  public ValueInAnonymousClass() {
    super();
  }
}
