import java.math.BigDecimal;

import javafx.beans.property.*;
import lombok.extern.javafx.FxProperty;

@FxProperty(readOnly = true)
class FxPropertyReadOnly {
	private IntegerProperty integer1;
	private ReadOnlyIntegerProperty integer2;
}