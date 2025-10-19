import lombok.RequiredArgsConstructor;
import lombok.experimental.PostConstructor;
@RequiredArgsConstructor class PostConstructorTest {
  private String name;
  private final Integer start;
  private final Integer end;
  private Integer range;
  private @PostConstructor void validate() {
    if ((((start == null) || (end == null)) || (end < start)))
        {
          throw new IllegalArgumentException();
        }
  }
  private @PostConstructor void calculateRange() {
    range = (end - start);
  }
  public @java.lang.SuppressWarnings("all") PostConstructorTest(final Integer start, final Integer end) {
    super();
    this.start = start;
    this.end = end;
    validate();
    calculateRange();
  }
}
