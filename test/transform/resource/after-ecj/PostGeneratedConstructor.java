import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.PostGeneratedConstructor;
@RequiredArgsConstructor @AllArgsConstructor class PostGeneratedConstructorTest {
  private String name;
  private final Integer start;
  private final Integer end;
  private @PostGeneratedConstructor void validate() {
    if ((((start == null) || (end == null)) || (end < start)))
        {
          throw new IllegalArgumentException();
        }
  }
  public @java.lang.SuppressWarnings("all") PostGeneratedConstructorTest(final Integer start, final Integer end) {
    super();
    this.start = start;
    this.end = end;
    validate();
  }
  public @java.lang.SuppressWarnings("all") PostGeneratedConstructorTest(final String name, final Integer start, final Integer end) {
    super();
    this.name = name;
    this.start = start;
    this.end = end;
    validate();
  }
}