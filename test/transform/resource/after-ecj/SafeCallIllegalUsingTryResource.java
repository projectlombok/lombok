import lombok.experimental.SafeCall;
class SafeCallIllegalUsingTryResource {
  public SafeCallIllegalUsingTryResource() {
    super();
    try (@SafeCall java.io.BufferedReader bufferedReader = nullObject().bufferedReader())
      {
      }
    catch (java.io.IOException e)
      {
      }
  }
  public SafeCallIllegalUsingTryResource nullObject() {
    return null;
  }
  public java.io.BufferedReader bufferedReader() {
    return null;
  }
}