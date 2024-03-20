import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.PostConstructor;
@NoArgsConstructor class PostConstructorGeneratedNoArgs {
  private String a;
  private String b;
  private @PostConstructor void postConstructor() {
  }
  public @java.lang.SuppressWarnings("all") PostConstructorGeneratedNoArgs() {
    super();
    postConstructor();
  }
}
@RequiredArgsConstructor class PostConstructorGeneratedRequiredArgs {
  private final String a;
  private final String b;
  private @PostConstructor void postConstructor() {
  }
  public @java.lang.SuppressWarnings("all") PostConstructorGeneratedRequiredArgs(final String a, final String b) {
    super();
    this.a = a;
    this.b = b;
    postConstructor();
  }
}
@AllArgsConstructor class PostConstructorGeneratedAllArgs {
  private String a;
  private String b;
  private @PostConstructor void postConstructor() {
  }
  public @java.lang.SuppressWarnings("all") PostConstructorGeneratedAllArgs(final String a, final String b) {
    super();
    this.a = a;
    this.b = b;
    postConstructor();
  }
}
