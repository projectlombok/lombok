import lombok.val;
public class ValWithLabel {
  {
    LABEL: for (final @val java.lang.String x : new String[0]) 
  {
    if ((x.toLowerCase() == null))
        {
          continue LABEL;
        }
  }
  }
  public ValWithLabel() {
    super();
  }
}