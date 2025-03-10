import java.math.BigDecimal;

import javafx.beans.property.*;
import lombok.extern.javafx.FxProperty;

class FxPropertySimple {
	@FxProperty private IntegerProperty integer1;
	@FxProperty private LongProperty long1;
	@FxProperty private FloatProperty float1;
	@FxProperty private DoubleProperty double1;
	@FxProperty private BooleanProperty boolean1;
	@FxProperty private StringProperty string1;
	@FxProperty private ObjectProperty<BigDecimal> object1;
	@FxProperty private ListProperty<String> list1;
	@FxProperty private SetProperty<String> set1;
	@FxProperty private MapProperty<String, String> map1;
	
	@FxProperty private ReadOnlyIntegerProperty integer2;
	@FxProperty private ReadOnlyLongProperty long2;
	@FxProperty private ReadOnlyFloatProperty float2;
	@FxProperty private ReadOnlyDoubleProperty double2;
	@FxProperty private ReadOnlyBooleanProperty boolean2;
	@FxProperty private ReadOnlyStringProperty string2;
	@FxProperty private ReadOnlyObjectProperty<BigDecimal> object2;
	@FxProperty private ReadOnlyListProperty<String> list2;
	@FxProperty private ReadOnlySetProperty<String> set2;
	@FxProperty private ReadOnlyMapProperty<String, String> map2;
}