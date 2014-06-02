import lombok.AccessLevel;
import lombok.experimental.Delegate;
import lombok.Getter;
@Getter class DelegateOnGetterNone {
  private interface Bar {
    void setList(java.util.ArrayList<java.lang.String> list);
    int getInt();
  }
  private final @Delegate @Getter(AccessLevel.NONE) Bar bar = null;
  DelegateOnGetterNone() {
    super();
  }
  public @java.lang.SuppressWarnings("all") int getInt() {
    return this.bar.getInt();
  }
  public @java.lang.SuppressWarnings("all") void setList(final java.util.ArrayList<java.lang.String> list) {
    this.bar.setList(list);
  }
}
