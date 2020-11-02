import java.math.BigDecimal;
import javafx.beans.property.*;

class FxPropertyReadOnly {
	private IntegerProperty integer1;
	private ReadOnlyIntegerProperty integer2;

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.ReadOnlyIntegerProperty integer1Property() {
		return this.integer1;
	}

	@java.lang.SuppressWarnings("all")
	public final int getInteger1() {
		return this.integer1.get();
	}

	@java.lang.SuppressWarnings("all")
	private final void setInteger1(final int integer1) {
		this.integer1.set(integer1);
	}

	@java.lang.SuppressWarnings("all")
	public javafx.beans.property.ReadOnlyIntegerProperty integer2Property() {
		return this.integer2;
	}

	@java.lang.SuppressWarnings("all")
	public final int getInteger2() {
		return this.integer2.get();
	}
}
