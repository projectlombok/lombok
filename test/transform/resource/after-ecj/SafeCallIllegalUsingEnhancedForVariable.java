import lombok.experimental.SafeCall;
class SafeCallIllegalUsingEnhancedForVariable {
  public SafeCallIllegalUsingEnhancedForVariable() {
    super();
    for (@SafeCall int intVal : getIntegerArray())
      {
      }
  }
  private Integer[] getIntegerArray() {
    return new Integer[1];
  }
}