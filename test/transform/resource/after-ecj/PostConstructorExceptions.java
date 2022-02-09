import lombok.AllArgsConstructor;
import lombok.experimental.PostConstructor;
@AllArgsConstructor class PostConstructorExceptions {
  private String a;
  PostConstructorExceptions() {
    super();
  }
  private @PostConstructor void post() throws IllegalArgumentException {
  }
  private @lombok.experimental.PostConstructor void post2() throws InterruptedException, java.lang.IllegalArgumentException, IllegalArgumentException {
  }
  public @java.lang.SuppressWarnings("all") PostConstructorExceptions(final String a) throws IllegalArgumentException, InterruptedException, java.lang.IllegalArgumentException, IllegalArgumentException {
    super();
    this.a = a;
    post();
    post2();
  }
}