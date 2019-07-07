import lombok.experimental.SafeCall;
class SafeCallLambda {
  public SafeCallLambda() {
    super();
    @SafeCall Runnable r = () ->     {
      @SafeCall int i;
      {
        java.lang.Integer i1 = getNullInteger();
        i = ((i1 != null) ? i1 : 0);
      }
    };
    {
    }
  }

  public Integer getNullInteger() {
    return null;
  }
}