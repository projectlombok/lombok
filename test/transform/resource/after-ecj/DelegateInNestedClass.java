import lombok.experimental.Delegate;
class DelegateInNestedClass {
  class InnerClass {
    private final @Delegate java.lang.Runnable field = null;
    InnerClass() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated void run() {
      this.field.run();
    }
  }
  static class StaticClass {
    private final @Delegate java.lang.Runnable field = null;
    StaticClass() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated void run() {
      this.field.run();
    }
  }
  DelegateInNestedClass() {
    super();
  }
  void localClass() {
    class LocalClass {
      private final @Delegate java.lang.Runnable field = null;
      LocalClass() {
        super();
      }
      public @java.lang.SuppressWarnings("all") @lombok.Generated void run() {
        this.field.run();
      }
    }
  }
  void anonymousClass() {
    Runnable r = new Runnable() {
      private final @Delegate java.lang.Runnable field = null;
      x() {
        super();
      }
      public @java.lang.SuppressWarnings("all") @lombok.Generated void run() {
        this.field.run();
      }
    };
  }
}