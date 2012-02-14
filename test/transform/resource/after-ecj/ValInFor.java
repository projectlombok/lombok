import lombok.val;
public class ValInFor {
  public ValInFor() {
    super();
  }
  public void enhancedFor() {
    java.util.List<String> list = java.util.Arrays.asList("Hello, World!");
    for (final @val java.lang.String shouldBeString : list) 
      {
        System.out.println(shouldBeString.toLowerCase());
        final @val java.lang.String shouldBeString2 = shouldBeString;
      }
  }
}