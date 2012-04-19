import java.util.List;
import lombok.val;
public class ValRawType {
  static class Element {
    Element() {
      super();
    }
    public List attributes() {
      return null;
    }
  }
  static class Attribute {
    Attribute() {
      super();
    }
  }
  public ValRawType() {
    super();
  }
  public void test() {
    Element propElement = new Element();
    for (final @val java.lang.Object attribute : propElement.attributes()) 
      {
        final @val ValRawType.Attribute attr = (Attribute) attribute;
      }
  }
}