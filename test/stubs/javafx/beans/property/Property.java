package javafx.beans.property;

public interface Property<T> extends ReadOnlyProperty<T> {
	public void set(T value);
}
