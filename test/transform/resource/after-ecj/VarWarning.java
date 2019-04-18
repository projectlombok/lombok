import lombok.var;
public class VarWarning {
  public VarWarning() {
    super();
  }
  public void isOkay() {
    @var java.lang.String x = "Warning";
    x.toLowerCase();
  }
}