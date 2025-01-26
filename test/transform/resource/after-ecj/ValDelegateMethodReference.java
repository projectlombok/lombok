import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import lombok.val;
import java.util.function.Function;
public class ValDelegateMethodReference {
  public ValDelegateMethodReference() {
    super();
  }
  public void config() {
    final @val Column<Entity, java.lang.String> column = createColumn(Entity::getValue);
  }
  private <V>Column<Entity, V> createColumn(Function<Entity, V> func) {
    return new Column<>(func);
  }
}
class Column<T, V> {
  public Column(Function<T, V> vp) {
    super();
  }
}
class Entity {
  private @Delegate MyDelegate innerDelegate;
  Entity() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.Boolean getABoolean() {
    return this.innerDelegate.getABoolean();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String getValue() {
    return this.innerDelegate.getValue();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setABoolean(final java.lang.Boolean aBoolean) {
    this.innerDelegate.setABoolean(aBoolean);
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setValue(final java.lang.String value) {
    this.innerDelegate.setValue(value);
  }
}
@Getter @Setter class MyDelegate {
  private String value;
  private Boolean aBoolean;
  MyDelegate() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated String getValue() {
    return this.value;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated Boolean getABoolean() {
    return this.aBoolean;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setValue(final String value) {
    this.value = value;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setABoolean(final Boolean aBoolean) {
    this.aBoolean = aBoolean;
  }
}
