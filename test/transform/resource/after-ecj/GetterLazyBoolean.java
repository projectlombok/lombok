@lombok.EqualsAndHashCode(of = "booleanValue") @lombok.ToString(of = "booleanValue") class GetterLazyBoolean {
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>> booleanValue = new java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>>();
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>> otherBooleanValue = new java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>>();
  public @java.lang.SuppressWarnings("all") boolean isBooleanValue() {
    java.util.concurrent.atomic.AtomicReference<java.lang.Boolean> value = this.booleanValue.get();
    if ((value == null))
        {
          synchronized (this.booleanValue)
            {
              value = this.booleanValue.get();
              if ((value == null))
                  {
                    final boolean actualValue = calculateBoolean();
                    value = new java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>(actualValue);
                    this.booleanValue.set(value);
                  }
            }
        }
    return value.get();
  }
  public @java.lang.SuppressWarnings("all") boolean isOtherBooleanValue() {
    java.util.concurrent.atomic.AtomicReference<java.lang.Boolean> value = this.otherBooleanValue.get();
    if ((value == null))
        {
          synchronized (this.otherBooleanValue)
            {
              value = this.otherBooleanValue.get();
              if ((value == null))
                  {
                    final boolean actualValue = (! calculateBoolean());
                    value = new java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>(actualValue);
                    this.otherBooleanValue.set(value);
                  }
            }
        }
    return value.get();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof GetterLazyBoolean)))
        return false;
    final @java.lang.SuppressWarnings("all") GetterLazyBoolean other = (GetterLazyBoolean) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.isBooleanValue() != other.isBooleanValue()))
        return false;
    return true;
  }
  public @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
    return (other instanceof GetterLazyBoolean);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = ((result * PRIME) + (this.isBooleanValue() ? 1231 : 1237));
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (("GetterLazyBoolean(booleanValue=" + this.isBooleanValue()) + ")");
  }
  GetterLazyBoolean() {
    super();
  }
  private static boolean calculateBoolean() {
    return true;
  }
}
