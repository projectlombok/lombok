//version 8:
import java.util.function.Function;

public class ValDelegateMethodReference {
	public void config() {
		final Column<Entity, java.lang.String> column = createColumn(Entity::getValue);
	}

	private <V> Column<Entity, V> createColumn(Function<Entity, V> func) {
		return new Column<>(func);
	}
}

class Column<T, V> {
	public Column(Function<T, V> vp) {
	}
}

class Entity {
	private MyDelegate innerDelegate;

	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public java.lang.String getValue() {
		return this.innerDelegate.getValue();
	}

	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public java.lang.Boolean getABoolean() {
		return this.innerDelegate.getABoolean();
	}

	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setValue(final java.lang.String value) {
		this.innerDelegate.setValue(value);
	}

	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setABoolean(final java.lang.Boolean aBoolean) {
		this.innerDelegate.setABoolean(aBoolean);
	}
}

class MyDelegate {
	private String value;
	private Boolean aBoolean;

	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public String getValue() {
		return this.value;
	}

	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public Boolean getABoolean() {
		return this.aBoolean;
	}

	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setValue(final String value) {
		this.value = value;
	}

	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setABoolean(final Boolean aBoolean) {
		this.aBoolean = aBoolean;
	}
}