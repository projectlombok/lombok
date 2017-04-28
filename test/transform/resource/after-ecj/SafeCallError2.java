import lombok.experimental.SafeCall;
class SafeCallError2 {
  public SafeCallError2() {
    super();
    @SafeCall String s = unexistedField.getNullString().trim();
    {
    }
  }
}