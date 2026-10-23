//version 8:
import lombok.Data;
import org.jspecify.annotations.NonNull;

@Data
class DataWithJspecifyTypeUseNonNull {
	private @NonNull String prop1;
	private java.lang.@NonNull String prop2;
}
