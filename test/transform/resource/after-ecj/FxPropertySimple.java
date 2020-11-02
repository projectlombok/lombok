import java.math.BigDecimal;
import javafx.beans.property.*;
import lombok.extern.javafx.FxProperty;
class FxPropertySimple {
  private @FxProperty IntegerProperty integer1;
  private @FxProperty LongProperty long1;
  private @FxProperty FloatProperty float1;
  private @FxProperty DoubleProperty double1;
  private @FxProperty BooleanProperty boolean1;
  private @FxProperty StringProperty string1;
  private @FxProperty ObjectProperty<BigDecimal> object1;
  private @FxProperty ListProperty<String> list1;
  private @FxProperty SetProperty<String> set1;
  private @FxProperty MapProperty<String, String> map1;
  private @FxProperty ReadOnlyIntegerProperty integer2;
  private @FxProperty ReadOnlyLongProperty long2;
  private @FxProperty ReadOnlyFloatProperty float2;
  private @FxProperty ReadOnlyDoubleProperty double2;
  private @FxProperty ReadOnlyBooleanProperty boolean2;
  private @FxProperty ReadOnlyStringProperty string2;
  private @FxProperty ReadOnlyObjectProperty<BigDecimal> object2;
  private @FxProperty ReadOnlyListProperty<String> list2;
  private @FxProperty ReadOnlySetProperty<String> set2;
  private @FxProperty ReadOnlyMapProperty<String, String> map2;
  FxPropertySimple() {
    super();
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.IntegerProperty integer1Property() {
    return this.integer1;
  }
  public final @java.lang.SuppressWarnings("all") int getInteger1() {
    return this.integer1.get();
  }
  public final @java.lang.SuppressWarnings("all") void setInteger1(final int integer1) {
    this.integer1.set(integer1);
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.LongProperty long1Property() {
    return this.long1;
  }
  public final @java.lang.SuppressWarnings("all") long getLong1() {
    return this.long1.get();
  }
  public final @java.lang.SuppressWarnings("all") void setLong1(final long long1) {
    this.long1.set(long1);
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.FloatProperty float1Property() {
    return this.float1;
  }
  public final @java.lang.SuppressWarnings("all") float getFloat1() {
    return this.float1.get();
  }
  public final @java.lang.SuppressWarnings("all") void setFloat1(final float float1) {
    this.float1.set(float1);
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.DoubleProperty double1Property() {
    return this.double1;
  }
  public final @java.lang.SuppressWarnings("all") double getDouble1() {
    return this.double1.get();
  }
  public final @java.lang.SuppressWarnings("all") void setDouble1(final double double1) {
    this.double1.set(double1);
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.BooleanProperty boolean1Property() {
    return this.boolean1;
  }
  public final @java.lang.SuppressWarnings("all") boolean getBoolean1() {
    return this.boolean1.get();
  }
  public final @java.lang.SuppressWarnings("all") void setBoolean1(final boolean boolean1) {
    this.boolean1.set(boolean1);
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.StringProperty string1Property() {
    return this.string1;
  }
  public final @java.lang.SuppressWarnings("all") java.lang.String getString1() {
    return this.string1.get();
  }
  public final @java.lang.SuppressWarnings("all") void setString1(final java.lang.String string1) {
    this.string1.set(string1);
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.ObjectProperty<BigDecimal> object1Property() {
    return this.object1;
  }
  public final @java.lang.SuppressWarnings("all") BigDecimal getObject1() {
    return this.object1.get();
  }
  public final @java.lang.SuppressWarnings("all") void setObject1(final BigDecimal object1) {
    this.object1.set(object1);
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.ListProperty<String> list1Property() {
    return this.list1;
  }
  public final @java.lang.SuppressWarnings("all") javafx.collections.ObservableList<String> getList1() {
    return this.list1.get();
  }
  public final @java.lang.SuppressWarnings("all") void setList1(final javafx.collections.ObservableList<String> list1) {
    this.list1.set(list1);
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.SetProperty<String> set1Property() {
    return this.set1;
  }
  public final @java.lang.SuppressWarnings("all") javafx.collections.ObservableSet<String> getSet1() {
    return this.set1.get();
  }
  public final @java.lang.SuppressWarnings("all") void setSet1(final javafx.collections.ObservableSet<String> set1) {
    this.set1.set(set1);
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.MapProperty<String, String> map1Property() {
    return this.map1;
  }
  public final @java.lang.SuppressWarnings("all") javafx.collections.ObservableMap<String, String> getMap1() {
    return this.map1.get();
  }
  public final @java.lang.SuppressWarnings("all") void setMap1(final javafx.collections.ObservableMap<String, String> map1) {
    this.map1.set(map1);
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.ReadOnlyIntegerProperty integer2Property() {
    return this.integer2;
  }
  public final @java.lang.SuppressWarnings("all") int getInteger2() {
    return this.integer2.get();
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.ReadOnlyLongProperty long2Property() {
    return this.long2;
  }
  public final @java.lang.SuppressWarnings("all") long getLong2() {
    return this.long2.get();
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.ReadOnlyFloatProperty float2Property() {
    return this.float2;
  }
  public final @java.lang.SuppressWarnings("all") float getFloat2() {
    return this.float2.get();
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.ReadOnlyDoubleProperty double2Property() {
    return this.double2;
  }
  public final @java.lang.SuppressWarnings("all") double getDouble2() {
    return this.double2.get();
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.ReadOnlyBooleanProperty boolean2Property() {
    return this.boolean2;
  }
  public final @java.lang.SuppressWarnings("all") boolean getBoolean2() {
    return this.boolean2.get();
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.ReadOnlyStringProperty string2Property() {
    return this.string2;
  }
  public final @java.lang.SuppressWarnings("all") java.lang.String getString2() {
    return this.string2.get();
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.ReadOnlyObjectProperty<BigDecimal> object2Property() {
    return this.object2;
  }
  public final @java.lang.SuppressWarnings("all") BigDecimal getObject2() {
    return this.object2.get();
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.ReadOnlyListProperty<String> list2Property() {
    return this.list2;
  }
  public final @java.lang.SuppressWarnings("all") javafx.collections.ObservableList<String> getList2() {
    return this.list2.get();
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.ReadOnlySetProperty<String> set2Property() {
    return this.set2;
  }
  public final @java.lang.SuppressWarnings("all") javafx.collections.ObservableSet<String> getSet2() {
    return this.set2.get();
  }
  public @java.lang.SuppressWarnings("all") javafx.beans.property.ReadOnlyMapProperty<String, String> map2Property() {
    return this.map2;
  }
  public final @java.lang.SuppressWarnings("all") javafx.collections.ObservableMap<String, String> getMap2() {
    return this.map2.get();
  }
}
