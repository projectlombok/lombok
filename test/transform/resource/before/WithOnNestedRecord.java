// version 14:
import lombok.With;

public class WithOnNestedRecord<T> {
	@With
	public record Nested(String a, String b) {
		
	}
}