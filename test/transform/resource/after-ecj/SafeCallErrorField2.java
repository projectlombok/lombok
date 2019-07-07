import lombok.experimental.SafeCall;
class SafeCallErrorField2 {
  @SafeCall String s = unexistedField.getNullString().trim();
  {
  }
  SafeCallErrorField2() {
    super();
  }
}