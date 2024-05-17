class SkipSuppressWarnings {
  private @lombok.Getter String field = "";
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.lang.Object> field2 = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
  SkipSuppressWarnings() {
    super();
  }
  public @lombok.Generated String getField() {
    return this.field;
  }
  public @java.lang.SuppressWarnings({"unchecked"}) @lombok.Generated String getField2() {
    java.lang.Object $value = this.field2.get();
    if (($value == null))
        {
          synchronized (this.field2)
            {
              $value = this.field2.get();
              if (($value == null))
                  {
                    final String actualValue = "";
                    $value = ((actualValue == null) ? this.field2 : actualValue);
                    this.field2.set($value);
                  }
            }
        }
    return (String) (($value == this.field2) ? null : $value);
  }
}
