import lombok.experimental.SafeCall;
class SafeCallErrorMethodParameters {
  public SafeCallErrorMethodParameters() {
    super();
    @SafeCall int i = nullObject(nullIndex(), nullString(), 2).intNullArray()[0];
    {
    }
    @SafeCall int[] ia = nullObject2(nullIndex(), nullString()).intNullArray();
    {
    }
  }
  public int[] intNullArray() {
    return null;
  }
  public String nullString() {
    return null;
  }
  public Integer nullIndex() {
    return null;
  }
  public SafeCallErrorMethodParameters nullObject(int i1, int... dots) {
    return null;
  }
  public SafeCallErrorMethodParameters nullObject2(int i1, int i2) {
    return null;
  }
}