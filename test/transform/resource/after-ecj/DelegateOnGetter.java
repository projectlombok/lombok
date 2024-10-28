import lombok.Delegate;
import lombok.Getter;
class DelegateOnGetter {
  private interface Bar {
    void setList(java.util.ArrayList<java.lang.String> list);
    int getInt();
  }
  private final @Delegate @Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.lang.Object> bar = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
  DelegateOnGetter() {
    super();
  }
  public @Delegate @java.lang.SuppressWarnings({"all", "unchecked"}) @lombok.Generated Bar getBar() {
    java.lang.Object $value = this.bar.get();
    if (($value == null))
        {
          synchronized (this.bar)
            {
              $value = this.bar.get();
              if (($value == null))
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
                    $value = ((actualValue == null) ? this.bar : actualValue);
                    this.bar.set($value);
                  }
            }
        }
    return (Bar) (($value == this.bar) ? null : $value);
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated int getInt() {
    return this.getBar().getInt();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setList(final java.util.ArrayList<java.lang.String> list) {
    this.getBar().setList(list);
  }
}
