import lombok.Data;
public class DataInAnonymousClass {
  Object annonymous = new Object() {
    @Data class Inner {
      private String string;
      public @java.lang.SuppressWarnings("all") String getString() {
        return this.string;
      }
      public @java.lang.SuppressWarnings("all") void setString(final String string) {
        this.string = string;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
        if ((o == this))
            return true;
        if ((! (o instanceof Inner)))
            return false;
        final Inner other = (Inner) o;
        if ((! other.canEqual((java.lang.Object) this)))
            return false;
        final java.lang.Object this$string = this.getString();
        final java.lang.Object other$string = other.getString();
        if (((this$string == null) ? (other$string != null) : (! this$string.equals(other$string))))
            return false;
        return true;
      }
      protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
        return (other instanceof Inner);
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $string = this.getString();
        result = ((result * PRIME) + (($string == null) ? 43 : $string.hashCode()));
        return result;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (("Inner(string=" + this.getString()) + ")");
      }
      public @java.lang.SuppressWarnings("all") Inner() {
        super();
      }
    }
    x() {
      super();
    }
  };
  public DataInAnonymousClass() {
    super();
  }
}
