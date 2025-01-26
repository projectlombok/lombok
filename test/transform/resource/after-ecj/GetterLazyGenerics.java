class GetterLazyGenerics<E> {
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.lang.Object> field = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.lang.Object> field2 = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
  GetterLazyGenerics() {
    super();
  }
  public static <E>E getAny() {
    return null;
  }
  public @java.lang.SuppressWarnings({"all", "unchecked"}) @lombok.Generated E getField() {
    java.lang.Object $value = this.field.get();
    if (($value == null))
        {
          synchronized (this.field)
            {
              $value = this.field.get();
              if (($value == null))
                  {
                    final E actualValue = getAny();
                    $value = ((actualValue == null) ? this.field : actualValue);
                    this.field.set($value);
                  }
            }
        }
    return (E) (($value == this.field) ? null : $value);
  }
  public @java.lang.SuppressWarnings({"all", "unchecked"}) @lombok.Generated long getField2() {
    java.lang.Object $value = this.field2.get();
    if (($value == null))
        {
          synchronized (this.field2)
            {
              $value = this.field2.get();
              if (($value == null))
                  {
                    final long actualValue = System.currentTimeMillis();
                    $value = actualValue;
                    this.field2.set($value);
                  }
            }
        }
    return (java.lang.Long) $value;
  }
}
