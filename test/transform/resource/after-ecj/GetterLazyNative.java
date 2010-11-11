class GetterLazyNative {
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>> booleanField = new java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>>();
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Byte>> byteField = new java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Byte>>();
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Short>> shortField = new java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Short>>();
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Integer>> intField = new java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Integer>>();
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Long>> longField = new java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Long>>();
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Float>> floatField = new java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Float>>();
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Double>> doubleField = new java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Double>>();
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Character>> charField = new java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Character>>();
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<int[]>> intArrayField = new java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<int[]>>();
  public @java.lang.SuppressWarnings("all") boolean isBooleanField() {
    java.util.concurrent.atomic.AtomicReference<java.lang.Boolean> value = this.booleanField.get();
    if ((value == null))
        {
          synchronized (this.booleanField)
            {
              value = this.booleanField.get();
              if ((value == null))
                  {
                    value = new java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>(true);
                    this.booleanField.set(value);
                  }
            }
        }
    return value.get();
  }
  public @java.lang.SuppressWarnings("all") byte getByteField() {
    java.util.concurrent.atomic.AtomicReference<java.lang.Byte> value = this.byteField.get();
    if ((value == null))
        {
          synchronized (this.byteField)
            {
              value = this.byteField.get();
              if ((value == null))
                  {
                    value = new java.util.concurrent.atomic.AtomicReference<java.lang.Byte>(1);
                    this.byteField.set(value);
                  }
            }
        }
    return value.get();
  }
  public @java.lang.SuppressWarnings("all") short getShortField() {
    java.util.concurrent.atomic.AtomicReference<java.lang.Short> value = this.shortField.get();
    if ((value == null))
        {
          synchronized (this.shortField)
            {
              value = this.shortField.get();
              if ((value == null))
                  {
                    value = new java.util.concurrent.atomic.AtomicReference<java.lang.Short>(1);
                    this.shortField.set(value);
                  }
            }
        }
    return value.get();
  }
  public @java.lang.SuppressWarnings("all") int getIntField() {
    java.util.concurrent.atomic.AtomicReference<java.lang.Integer> value = this.intField.get();
    if ((value == null))
        {
          synchronized (this.intField)
            {
              value = this.intField.get();
              if ((value == null))
                  {
                    value = new java.util.concurrent.atomic.AtomicReference<java.lang.Integer>(1);
                    this.intField.set(value);
                  }
            }
        }
    return value.get();
  }
  public @java.lang.SuppressWarnings("all") long getLongField() {
    java.util.concurrent.atomic.AtomicReference<java.lang.Long> value = this.longField.get();
    if ((value == null))
        {
          synchronized (this.longField)
            {
              value = this.longField.get();
              if ((value == null))
                  {
                    value = new java.util.concurrent.atomic.AtomicReference<java.lang.Long>(1);
                    this.longField.set(value);
                  }
            }
        }
    return value.get();
  }
  public @java.lang.SuppressWarnings("all") float getFloatField() {
    java.util.concurrent.atomic.AtomicReference<java.lang.Float> value = this.floatField.get();
    if ((value == null))
        {
          synchronized (this.floatField)
            {
              value = this.floatField.get();
              if ((value == null))
                  {
                    value = new java.util.concurrent.atomic.AtomicReference<java.lang.Float>(1.0f);
                    this.floatField.set(value);
                  }
            }
        }
    return value.get();
  }
  public @java.lang.SuppressWarnings("all") double getDoubleField() {
    java.util.concurrent.atomic.AtomicReference<java.lang.Double> value = this.doubleField.get();
    if ((value == null))
        {
          synchronized (this.doubleField)
            {
              value = this.doubleField.get();
              if ((value == null))
                  {
                    value = new java.util.concurrent.atomic.AtomicReference<java.lang.Double>(1.0);
                    this.doubleField.set(value);
                  }
            }
        }
    return value.get();
  }
  public @java.lang.SuppressWarnings("all") char getCharField() {
    java.util.concurrent.atomic.AtomicReference<java.lang.Character> value = this.charField.get();
    if ((value == null))
        {
          synchronized (this.charField)
            {
              value = this.charField.get();
              if ((value == null))
                  {
                    value = new java.util.concurrent.atomic.AtomicReference<java.lang.Character>('1');
                    this.charField.set(value);
                  }
            }
        }
    return value.get();
  }
  public @java.lang.SuppressWarnings("all") int[] getIntArrayField() {
    java.util.concurrent.atomic.AtomicReference<int[]> value = this.intArrayField.get();
    if ((value == null))
        {
          synchronized (this.intArrayField)
            {
              value = this.intArrayField.get();
              if ((value == null))
                  {
                    value = new java.util.concurrent.atomic.AtomicReference<int[]>(new int[]{1});
                    this.intArrayField.set(value);
                  }
            }
        }
    return value.get();
  }
  GetterLazyNative() {
    super();
  }
}