import lombok.var;
public class VarInForOld {
  public VarInForOld() {
    super();
  }
  public void oldFor() {
    for (@var int i = 0;; (i < 100); ++ i)
      {
        System.out.println(i);
      }
  }
}