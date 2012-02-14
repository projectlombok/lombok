@lombok.EqualsAndHashCode(doNotUseGetters = true) @lombok.ToString(doNotUseGetters = true) class GetterLazyEahcToString {
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<String>> value = new java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<String>>();
  private final @lombok.Getter String value2 = "";
  public @java.lang.SuppressWarnings("all") String getValue() {
    java.util.concurrent.atomic.AtomicReference<String> value = this.value.get();
    if ((value == null))
        {
          synchronized (this.value)
            {
              value = this.value.get();
              if ((value == null))
                  {
                    final String actualValue = "";
                    value = new java.util.concurrent.atomic.AtomicReference<String>(actualValue);
                    this.value.set(value);
                  }
            }
        }
    return value.get();
  }
  public @java.lang.SuppressWarnings("all") String getValue2() {
    return this.value2;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof GetterLazyEahcToString)))
        return false;
    final @java.lang.SuppressWarnings("all") GetterLazyEahcToString other = (GetterLazyEahcToString) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if (((this.getValue() == null) ? (other.getValue() != null) : (! this.getValue().equals((java.lang.Object) other.getValue()))))
        return false;
    if (((this.value2 == null) ? (other.value2 != null) : (! this.value2.equals((java.lang.Object) other.value2))))
        return false;
    return true;
  }
  public @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
    return (other instanceof GetterLazyEahcToString);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = ((result * PRIME) + ((this.getValue() == null) ? 0 : this.getValue().hashCode()));
    result = ((result * PRIME) + ((this.value2 == null) ? 0 : this.value2.hashCode()));
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (((("GetterLazyEahcToString(value=" + this.getValue()) + ", value2=") + this.value2) + ")");
  }
  GetterLazyEahcToString() {
    super();
  }
}