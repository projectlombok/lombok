import lombok.Delegate;
import lombok.Getter;
class DelegateOnGetter {
  private interface Bar {
    void setList(java.util.ArrayList<java.lang.String> list);
    int getInt();
  }
  private final @Delegate @Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<Bar>> bar = new java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<Bar>>();
  public @Delegate @java.lang.SuppressWarnings("all") Bar getBar() {
    java.util.concurrent.atomic.AtomicReference<Bar> value = this.bar.get();
    if ((value == null))
        {
          synchronized (this.bar)
            {
              value = this.bar.get();
              if ((value == null))
                  {
                    final Bar actualValue = new Bar() {
                      x() {
                        super();
                      }
                      public void setList(java.util.ArrayList<String> list) {
                      }
                      public int getInt() {
                        return 42;
                      }
                    };
                    value = new java.util.concurrent.atomic.AtomicReference<Bar>(actualValue);
                    this.bar.set(value);
                  }
            }
        }
    return value.get();
  }
  public @java.lang.SuppressWarnings("all") int getInt() {
    return this.getBar().getInt();
  }
  public @java.lang.SuppressWarnings("all") void setList(final java.util.ArrayList<java.lang.String> list) {
    this.getBar().setList(list);
  }
  DelegateOnGetter() {
    super();
  }
}