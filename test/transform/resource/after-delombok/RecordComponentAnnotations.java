// version 16:
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import lombok.NonNull;
public record RecordComponentAnnotations(@Label("x") @NonNull String x, @Label("y") String y) {
	@Target(ElementType.RECORD_COMPONENT)
	@interface Label {
		String value();
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public RecordComponentAnnotations {
		if (x == null) {
			throw new java.lang.NullPointerException("x is marked non-null but is null");
		}
	}
}
