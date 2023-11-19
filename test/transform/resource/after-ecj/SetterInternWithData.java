import lombok.Setter;
import lombok.Data;
public @Data class SetterInternWithData {
  private @Setter(internString = true) String str1;
  public @java.lang.SuppressWarnings("all") void setStr1(final String str1) {
    this.str1 = ((str1 == null) ? null : str1.intern());
  }
  public @java.lang.SuppressWarnings("all") String getStr1() {
    return this.str1;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof SetterInternWithData)))
        return false;
    final SetterInternWithData other = (SetterInternWithData) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    final java.lang.Object this$str1 = this.getStr1();
    final java.lang.Object other$str1 = other.getStr1();
    if (((this$str1 == null) ? (other$str1 != null) : (! this$str1.equals(other$str1))))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
    return (other instanceof SetterInternWithData);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final java.lang.Object $str1 = this.getStr1();
    result = ((result * PRIME) + (($str1 == null) ? 43 : $str1.hashCode()));
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (("SetterInternWithData(str1=" + this.getStr1()) + ")");
  }
  public @java.lang.SuppressWarnings("all") SetterInternWithData() {
    super();
  }
}