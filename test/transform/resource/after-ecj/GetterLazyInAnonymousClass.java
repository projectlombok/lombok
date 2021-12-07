import lombok.Getter;
public class GetterLazyInAnonymousClass {
  Object annonymous = new Object() {
    class Inner {
      private final @Getter(lazy = true) java.util.concurrent.atomic.AtomicReference<java.lang.Object> string = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
      Inner() {
        super();
      }
      public @java.lang.SuppressWarnings({"all", "unchecked"}) String getString() {
        java.lang.Object value = this.string.get();
        if ((value == null))
            {
              synchronized (this.string)
                {
                  value = this.string.get();
                  if ((value == null))
                      {
                        final String actualValue = "test";
                        value = ((actualValue == null) ? this.string : actualValue);
                        this.string.set(value);
                      }
                }
            }
        return (String) ((value == this.string) ? null : value);
      }
    }
    x() {
      super();
    }
  };
  public GetterLazyInAnonymousClass() {
    super();
  }
}
