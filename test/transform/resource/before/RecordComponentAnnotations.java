// version 16:

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import lombok.NonNull;

public record RecordComponentAnnotations(@Label("x") @NonNull String x, @Label("y") String y) {
	@Target(ElementType.RECORD_COMPONENT)
	@interface Label {
		String value();
	}
}
