package lombok;

public interface ToStringFormatter {
	<T> String format(T field);
}
