@lombok.EqualsAndHashCode(of = "booleanValue") @lombok.ToString(of = "booleanValue") class GetterLazyBoolean {
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.lang.Object> booleanValue = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.lang.Object> otherBooleanValue = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
  GetterLazyBoolean() {
    super();
  }
  private static boolean calculateBoolean() {
    return true;
  }
  public @java.lang.SuppressWarnings("all") boolean isBooleanValue() {
    java.lang.Object value = this.booleanValue.get();
    if ((value == null))
        {
          synchronized (this.booleanValue)
            {
              value = this.booleanValue.get();
              if ((value == null))
                  {
                    final boolean actualValue = calculateBoolean();
                    value = actualValue;
                    this.booleanValue.set(value);
                  }
            }
        }
    return (java.lang.Boolean) value;
  }
  public @java.lang.SuppressWarnings("all") boolean isOtherBooleanValue() {
    java.lang.Object value = this.otherBooleanValue.get();
    if ((value == null))
        {
          synchronized (this.otherBooleanValue)
            {
              value = this.otherBooleanValue.get();
              if ((value == null))
                  {
                    final boolean actualValue = (! calculateBoolean());
                    value = actualValue;
                    this.otherBooleanValue.set(value);
                  }
            }
        }
    return (java.lang.Boolean) value;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof GetterLazyBoolean)))
        return false;
    final GetterLazyBoolean other = (GetterLazyBoolean) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.isBooleanValue() != other.isBooleanValue()))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
    return (other instanceof GetterLazyBoolean);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + (this.isBooleanValue() ? 79 : 97));
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (("GetterLazyBoolean(booleanValue=" + this.isBooleanValue()) + ")");
  }
}
