import lombok.AllArgsConstructor;
import lombok.experimental.PostGeneratedConstructor;
@AllArgsConstructor class PostGeneratedConstructorExceptions {
  private String a;
  PostGeneratedConstructorExceptions() {
    super();
  }
  private @PostGeneratedConstructor void post() throws IllegalArgumentException {
  }
  private @lombok.experimental.PostGeneratedConstructor void post2() throws InterruptedException, java.lang.IllegalArgumentException, IllegalArgumentException {
  }
  public @java.lang.SuppressWarnings("all") PostGeneratedConstructorExceptions(final String a) throws IllegalArgumentException, InterruptedException, java.lang.IllegalArgumentException, IllegalArgumentException {
    super();
    this.a = a;
    post();
    post2();
  }
}