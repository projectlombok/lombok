import lombok.experimental.PostConstructor;
class PostConstructorOrder {
  private @PostConstructor void first() {
  }
  private @PostConstructor void second() {
  }
  private @PostConstructor void third() {
  }
  private @PostConstructor void fourth() {
  }
  public @java.lang.SuppressWarnings("all") PostConstructorOrder() {
    super();
    first();
    second();
    third();
    fourth();
  }
}
