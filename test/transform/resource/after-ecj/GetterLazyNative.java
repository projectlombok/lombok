class GetterLazyNative {
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.lang.Object> booleanField = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.lang.Object> byteField = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.lang.Object> shortField = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.lang.Object> intField = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.lang.Object> longField = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.lang.Object> floatField = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.lang.Object> doubleField = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.lang.Object> charField = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.lang.Object> intArrayField = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
  GetterLazyNative() {
    super();
  }
  public @java.lang.SuppressWarnings("all") boolean isBooleanField() {
    java.lang.Object value = this.booleanField.get();
    if ((value == null))
        {
          synchronized (this.booleanField)
            {
              value = this.booleanField.get();
              if ((value == null))
                  {
                    final boolean actualValue = true;
                    value = actualValue;
                    this.booleanField.set(value);
                  }
            }
        }
    return (java.lang.Boolean) value;
  }
  public @java.lang.SuppressWarnings("all") byte getByteField() {
    java.lang.Object value = this.byteField.get();
    if ((value == null))
        {
          synchronized (this.byteField)
            {
              value = this.byteField.get();
              if ((value == null))
                  {
                    final byte actualValue = 1;
                    value = actualValue;
                    this.byteField.set(value);
                  }
            }
        }
    return (java.lang.Byte) value;
  }
  public @java.lang.SuppressWarnings("all") short getShortField() {
    java.lang.Object value = this.shortField.get();
    if ((value == null))
        {
          synchronized (this.shortField)
            {
              value = this.shortField.get();
              if ((value == null))
                  {
                    final short actualValue = 1;
                    value = actualValue;
                    this.shortField.set(value);
                  }
            }
        }
    return (java.lang.Short) value;
  }
  public @java.lang.SuppressWarnings("all") int getIntField() {
    java.lang.Object value = this.intField.get();
    if ((value == null))
        {
          synchronized (this.intField)
            {
              value = this.intField.get();
              if ((value == null))
                  {
                    final int actualValue = 1;
                    value = actualValue;
                    this.intField.set(value);
                  }
            }
        }
    return (java.lang.Integer) value;
  }
  public @java.lang.SuppressWarnings("all") long getLongField() {
    java.lang.Object value = this.longField.get();
    if ((value == null))
        {
          synchronized (this.longField)
            {
              value = this.longField.get();
              if ((value == null))
                  {
                    final long actualValue = 1;
                    value = actualValue;
                    this.longField.set(value);
                  }
            }
        }
    return (java.lang.Long) value;
  }
  public @java.lang.SuppressWarnings("all") float getFloatField() {
    java.lang.Object value = this.floatField.get();
    if ((value == null))
        {
          synchronized (this.floatField)
            {
              value = this.floatField.get();
              if ((value == null))
                  {
                    final float actualValue = 1.0f;
                    value = actualValue;
                    this.floatField.set(value);
                  }
            }
        }
    return (java.lang.Float) value;
  }
  public @java.lang.SuppressWarnings("all") double getDoubleField() {
    java.lang.Object value = this.doubleField.get();
    if ((value == null))
        {
          synchronized (this.doubleField)
            {
              value = this.doubleField.get();
              if ((value == null))
                  {
                    final double actualValue = 1.0;
                    value = actualValue;
                    this.doubleField.set(value);
                  }
            }
        }
    return (java.lang.Double) value;
  }
  public @java.lang.SuppressWarnings("all") char getCharField() {
    java.lang.Object value = this.charField.get();
    if ((value == null))
        {
          synchronized (this.charField)
            {
              value = this.charField.get();
              if ((value == null))
                  {
                    final char actualValue = '1';
                    value = actualValue;
                    this.charField.set(value);
                  }
            }
        }
    return (java.lang.Character) value;
  }
  public @java.lang.SuppressWarnings("all") int[] getIntArrayField() {
    java.lang.Object value = this.intArrayField.get();
    if ((value == null))
        {
          synchronized (this.intArrayField)
            {
              value = this.intArrayField.get();
              if ((value == null))
                  {
                    final int[] actualValue = new int[]{1};
                    value = ((actualValue == null) ? this.intArrayField : actualValue);
                    this.intArrayField.set(value);
                  }
            }
        }
    return (int[]) ((value == this.intArrayField) ? null : value);
  }
}
