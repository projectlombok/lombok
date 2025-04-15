import java.math.BigDecimal;

import javafx.beans.property.*;
import lombok.extern.javafx.FxProperty;

class FxPropertyExisting {
	@FxProperty private IntegerProperty integer;

	public ReadOnlyIntegerProperty integerProperty() {
		return this.integer;
	}

	public int getInteger() {
		return this.integer.get();
	}
	
	public void setInteger(int integer) {
		this.integer.set(integer);
	}
}