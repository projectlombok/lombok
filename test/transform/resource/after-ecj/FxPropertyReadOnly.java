import java.math.BigDecimal;
import javafx.beans.property.*;
import lombok.extern.javafx.FxProperty;
@FxProperty(readOnly = true) class FxPropertyReadOnly {
  private IntegerProperty integer1;
  private ReadOnlyIntegerProperty integer2;
  FxPropertyReadOnly() {
    super();
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.ReadOnlyIntegerProperty integer1Property() {
    return this.integer1;
  }
  public final @java.lang.SuppressWarnings("all") int getInteger1() {
    return this.integer1.get();
  }
  private final @java.lang.SuppressWarnings("all") void setInteger1(final int integer1) {
    this.integer1.set(integer1);
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.ReadOnlyIntegerProperty integer2Property() {
    return this.integer2;
  }
  public final @java.lang.SuppressWarnings("all") int getInteger2() {
    return this.integer2.get();
  }
}
