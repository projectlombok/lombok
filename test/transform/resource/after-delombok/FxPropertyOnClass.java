import java.math.BigDecimal;
import javafx.beans.property.*;

class FxPropertyOnClass {
	private IntegerProperty integer1;
	private LongProperty long1;
	private FloatProperty float1;
	private DoubleProperty double1;
	private BooleanProperty boolean1;
	private StringProperty string1;
	private ObjectProperty<BigDecimal> object1;
	private ListProperty<String> list1;
	private SetProperty<String> set1;
	private MapProperty<String, String> map1;
	private ReadOnlyIntegerProperty integer2;
	private ReadOnlyLongProperty long2;
	private ReadOnlyFloatProperty float2;
	private ReadOnlyDoubleProperty double2;
	private ReadOnlyBooleanProperty boolean2;
	private ReadOnlyStringProperty string2;
	private ReadOnlyObjectProperty<BigDecimal> object2;
	private ReadOnlyListProperty<String> list2;
	private ReadOnlySetProperty<String> set2;
	private ReadOnlyMapProperty<String, String> map2;

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.IntegerProperty integer1Property() {
		return this.integer1;
	}

	@java.lang.SuppressWarnings("all")
	public final int getInteger1() {
		return this.integer1.get();
	}

	@java.lang.SuppressWarnings("all")
	public final void setInteger1(final int integer1) {
		this.integer1.set(integer1);
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.LongProperty long1Property() {
		return this.long1;
	}

	@java.lang.SuppressWarnings("all")
	public final long getLong1() {
		return this.long1.get();
	}

	@java.lang.SuppressWarnings("all")
	public final void setLong1(final long long1) {
		this.long1.set(long1);
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.FloatProperty float1Property() {
		return this.float1;
	}

	@java.lang.SuppressWarnings("all")
	public final float getFloat1() {
		return this.float1.get();
	}

	@java.lang.SuppressWarnings("all")
	public final void setFloat1(final float float1) {
		this.float1.set(float1);
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.DoubleProperty double1Property() {
		return this.double1;
	}

	@java.lang.SuppressWarnings("all")
	public final double getDouble1() {
		return this.double1.get();
	}

	@java.lang.SuppressWarnings("all")
	public final void setDouble1(final double double1) {
		this.double1.set(double1);
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.BooleanProperty boolean1Property() {
		return this.boolean1;
	}

	@java.lang.SuppressWarnings("all")
	public final boolean getBoolean1() {
		return this.boolean1.get();
	}

	@java.lang.SuppressWarnings("all")
	public final void setBoolean1(final boolean boolean1) {
		this.boolean1.set(boolean1);
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.StringProperty string1Property() {
		return this.string1;
	}

	@java.lang.SuppressWarnings("all")
	public final java.lang.String getString1() {
		return this.string1.get();
	}

	@java.lang.SuppressWarnings("all")
	public final void setString1(final java.lang.String string1) {
		this.string1.set(string1);
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.ObjectProperty object1Property() {
		return this.object1;
	}

	@java.lang.SuppressWarnings("all")
	public final BigDecimal getObject1() {
		return this.object1.get();
	}

	@java.lang.SuppressWarnings("all")
	public final void setObject1(final BigDecimal object1) {
		this.object1.set(object1);
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.ListProperty list1Property() {
		return this.list1;
	}

	@java.lang.SuppressWarnings("all")
	public final javafx.collections.ObservableList<String> getList1() {
		return this.list1.get();
	}

	@java.lang.SuppressWarnings("all")
	public final void setList1(final javafx.collections.ObservableList<String> list1) {
		this.list1.set(list1);
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.SetProperty set1Property() {
		return this.set1;
	}

	@java.lang.SuppressWarnings("all")
	public final javafx.collections.ObservableSet<String> getSet1() {
		return this.set1.get();
	}

	@java.lang.SuppressWarnings("all")
	public final void setSet1(final javafx.collections.ObservableSet<String> set1) {
		this.set1.set(set1);
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.MapProperty map1Property() {
		return this.map1;
	}

	@java.lang.SuppressWarnings("all")
	public final javafx.collections.ObservableMap<String, String> getMap1() {
		return this.map1.get();
	}

	@java.lang.SuppressWarnings("all")
	public final void setMap1(final javafx.collections.ObservableMap<String, String> map1) {
		this.map1.set(map1);
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.ReadOnlyIntegerProperty integer2Property() {
		return this.integer2;
	}

	@java.lang.SuppressWarnings("all")
	public final int getInteger2() {
		return this.integer2.get();
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.ReadOnlyLongProperty long2Property() {
		return this.long2;
	}

	@java.lang.SuppressWarnings("all")
	public final long getLong2() {
		return this.long2.get();
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.ReadOnlyFloatProperty float2Property() {
		return this.float2;
	}

	@java.lang.SuppressWarnings("all")
	public final float getFloat2() {
		return this.float2.get();
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.ReadOnlyDoubleProperty double2Property() {
		return this.double2;
	}

	@java.lang.SuppressWarnings("all")
	public final double getDouble2() {
		return this.double2.get();
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.ReadOnlyBooleanProperty boolean2Property() {
		return this.boolean2;
	}

	@java.lang.SuppressWarnings("all")
	public final boolean getBoolean2() {
		return this.boolean2.get();
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.ReadOnlyStringProperty string2Property() {
		return this.string2;
	}

	@java.lang.SuppressWarnings("all")
	public final java.lang.String getString2() {
		return this.string2.get();
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.ReadOnlyObjectProperty object2Property() {
		return this.object2;
	}

	@java.lang.SuppressWarnings("all")
	public final BigDecimal getObject2() {
		return this.object2.get();
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.ReadOnlyListProperty list2Property() {
		return this.list2;
	}

	@java.lang.SuppressWarnings("all")
	public final javafx.collections.ObservableList<String> getList2() {
		return this.list2.get();
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.ReadOnlySetProperty set2Property() {
		return this.set2;
	}

	@java.lang.SuppressWarnings("all")
	public final javafx.collections.ObservableSet<String> getSet2() {
		return this.set2.get();
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.ReadOnlyMapProperty map2Property() {
		return this.map2;
	}

	@java.lang.SuppressWarnings("all")
	public final javafx.collections.ObservableMap<String, String> getMap2() {
		return this.map2.get();
	}
}
