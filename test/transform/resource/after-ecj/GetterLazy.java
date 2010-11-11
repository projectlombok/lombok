class GetterLazy {
  static class ValueType {
    ValueType() {
      super();
    }
  }
  private final @lombok.Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<ValueType>> fieldName = new java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<ValueType>>();
  public @java.lang.SuppressWarnings("all") ValueType getFieldName() {
    java.util.concurrent.atomic.AtomicReference<ValueType> value = this.fieldName.get();
    if ((value == null))
        {
          synchronized (this.fieldName)
            {
              value = this.fieldName.get();
              if ((value == null))
                  {
                    value = new java.util.concurrent.atomic.AtomicReference<ValueType>(new ValueType());
                    this.fieldName.set(value);
                  }
            }
        }
    return value.get();
  }
  GetterLazy() {
    super();
  }
}
