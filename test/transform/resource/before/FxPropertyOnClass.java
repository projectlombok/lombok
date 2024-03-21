import java.math.BigDecimal;

import javafx.beans.property.*;
import lombok.extern.javafx.FxProperty;

@FxProperty
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
}