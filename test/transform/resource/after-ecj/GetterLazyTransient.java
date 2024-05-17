class GetterLazyTransient {
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.lang.Object> nonTransientField = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
  private final transient @lombok.Getter(lazy = true) int transientField = 2;
  private final transient @lombok.Getter int nonLazyTransientField = 3;
  GetterLazyTransient() {
    super();
  }
  public @java.lang.SuppressWarnings({"all", "unchecked"}) @lombok.Generated int getNonTransientField() {
    java.lang.Object $value = this.nonTransientField.get();
    if (($value == null))
        {
          synchronized (this.nonTransientField)
            {
              $value = this.nonTransientField.get();
              if (($value == null))
                  {
                    final int actualValue = 1;
                    $value = actualValue;
                    this.nonTransientField.set($value);
                  }
            }
        }
    return (java.lang.Integer) $value;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated int getNonLazyTransientField() {
    return this.nonLazyTransientField;
  }
}
