@lombok.EqualsAndHashCode(doNotUseGetters = true) @lombok.ToString(doNotUseGetters = true) class GetterLazyEahcToString {
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.lang.Object> value = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
  private final @lombok.Getter String value2 = "";
  GetterLazyEahcToString() {
    super();
  }
  public @java.lang.SuppressWarnings({"all", "unchecked"}) @lombok.Generated String getValue() {
    java.lang.Object $value = this.value.get();
    if (($value == null))
        {
          synchronized (this.value)
            {
              $value = this.value.get();
              if (($value == null))
                  {
                    final String actualValue = "";
                    $value = ((actualValue == null) ? this.value : actualValue);
                    this.value.set($value);
                  }
            }
        }
    return (String) (($value == this.value) ? null : $value);
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated String getValue2() {
    return this.value2;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof GetterLazyEahcToString)))
        return false;
    final GetterLazyEahcToString other = (GetterLazyEahcToString) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    final java.lang.Object this$value = this.getValue();
    final java.lang.Object other$value = other.getValue();
    if (((this$value == null) ? (other$value != null) : (! this$value.equals(other$value))))
        return false;
    final java.lang.Object this$value2 = this.value2;
    final java.lang.Object other$value2 = other.value2;
    if (((this$value2 == null) ? (other$value2 != null) : (! this$value2.equals(other$value2))))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean canEqual(final java.lang.Object other) {
    return (other instanceof GetterLazyEahcToString);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final java.lang.Object $value = this.getValue();
    result = ((result * PRIME) + (($value == null) ? 43 : $value.hashCode()));
    final java.lang.Object $value2 = this.value2;
    result = ((result * PRIME) + (($value2 == null) ? 43 : $value2.hashCode()));
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (((("GetterLazyEahcToString(value=" + this.getValue()) + ", value2=") + this.value2) + ")");
  }
}
