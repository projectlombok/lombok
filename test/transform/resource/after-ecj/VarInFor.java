import lombok.var;
public class VarInFor {
  public VarInFor() {
    super();
  }
  public void enhancedFor() {
    int[] list = new int[]{1, 2};
    for (@var int shouldBeInt : list)
      {
        System.out.println(shouldBeInt);
        @var int shouldBeInt2 = shouldBeInt;
      }
  }
}