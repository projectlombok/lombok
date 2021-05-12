import lombok.val;
public class ValAnonymousSubclassSelfReference {
  public ValAnonymousSubclassSelfReference() {
    super();
  }
  public void test() {
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