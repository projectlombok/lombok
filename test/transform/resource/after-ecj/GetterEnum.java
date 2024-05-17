import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor(access = AccessLevel.PRIVATE) enum GetterEnum {
  ONE(1, "One"),
  private final @Getter int id;
  private final @Getter String name;
  <clinit>() {
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated int getId() {
    return this.id;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated String getName() {
    return this.name;
  }
  private @java.lang.SuppressWarnings("all") @lombok.Generated GetterEnum(final int id, final String name) {
    super();
    this.id = id;
    this.name = name;
  }
}
