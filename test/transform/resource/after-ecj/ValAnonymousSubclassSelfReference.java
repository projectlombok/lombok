import java.util.Map;
import java.util.HashMap;
import lombok.val;
public class ValAnonymousSubclassSelfReference {
  public ValAnonymousSubclassSelfReference() {
    super();
  }
  public <T>void test(T arg) {
    T d = arg;
    Integer[] e = new Integer[1];
    int[] f = new int[0];
    java.util.Map<java.lang.String, Integer> g = new HashMap<String, Integer>();
    Integer h = 0;
    int i = 0;
    final @val int j = 1;
    final @val int k = 2;
    new ValAnonymousSubclassSelfReference() {
      x() {
        super();
      }
    };
  }
}