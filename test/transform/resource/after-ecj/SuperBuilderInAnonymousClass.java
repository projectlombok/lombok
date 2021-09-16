import lombok.experimental.SuperBuilder;
public class SuperBuilderInAnonymousClass {
  Object annonymous = new Object() {
    @SuperBuilder class InnerParent {
      private String string;
      InnerParent() {
        super();
      }
    }
    @SuperBuilder class InnerChild {
      private String string;
      InnerChild() {
        super();
      }
    }
    x() {
      super();
    }
  };
  public SuperBuilderInAnonymousClass() {
    super();
  }
}
