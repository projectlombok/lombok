import lombok.val;
public class ValInMixedFor {
  public ValInMixedFor() {
    super();
  }
  public void basicFor() {
    java.util.List<String> list = java.util.Arrays.asList("Hello, World!");
    for (final @val int shouldBe = 1;, final @val int marked = "";, final @val int error = 1.0;; ; ) 
      {
        System.out.println("");
      }
  }
}