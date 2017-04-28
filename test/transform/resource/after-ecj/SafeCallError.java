import lombok.experimental.SafeCall;
class SafeCallError {
  public SafeCallError() {
    super();
    @SafeCall String s = new UnexistedClass().getNullString().trim();
    {
    }
  }
}