import lombok.Getter;
import lombok.AccessLevel;
class MultiFieldGetter {
  @Getter(AccessLevel.PROTECTED) int x;
  @Getter(AccessLevel.PROTECTED) int y;
  MultiFieldGetter() {
    super();
  }
  protected @java.lang.SuppressWarnings("all") int getX() {
    return this.x;
  }
  protected @java.lang.SuppressWarnings("all") int getY() {
    return this.y;
  }
}
@Getter class MultiFieldGetter2 {
  @Getter(AccessLevel.PACKAGE) int x;
  @Getter(AccessLevel.PACKAGE) int y;
  MultiFieldGetter2() {
    super();
  }
  @java.lang.SuppressWarnings("all") int getX() {
    return this.x;
  }
  @java.lang.SuppressWarnings("all") int getY() {
    return this.y;
  }
}