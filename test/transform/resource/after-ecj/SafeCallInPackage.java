package first.second;
import lombok.experimental.SafeCall;
class SafeCallInPackage {
  public SafeCallInPackage() {
    super();
    @SafeCall String s;
    {
      first.second.SafeCallInPackage s3 = new first.second.SafeCallInPackage();
      java.lang.String s2 = ((s3 != null) ? s3.getNullString() : null);
      java.lang.String s1 = ((s2 != null) ? s2.trim() : null);
      s = s1;
    }
  }
  public String getNullString() {
    return null;
  }
}