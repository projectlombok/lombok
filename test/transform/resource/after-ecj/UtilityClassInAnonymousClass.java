import lombok.experimental.UtilityClass;
public class UtilityClassInAnonymousClass {
  Object annonymous = new Object() {
    @UtilityClass class Inner {
      private String string;
      Inner() {
        super();
      }
    }
    x() {
      super();
    }
  };
  public UtilityClassInAnonymousClass() {
    super();
  }
}
