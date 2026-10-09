//version 8:
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Setter
@RequiredArgsConstructor
class LombokNonNull {
	@NonNull Double[] noNullCheck;
	List<@NonNull Double> noNullCheckList;

	String @NonNull [] nullCheck;
	@NonNull List<String> nullCheckList;

	Map<@NonNull String, @NonNull List<@Nullable Object @NonNull []>> test;

	public void noNullCheckExpected1(@NonNull Double[] d) {
	}
	public void noNullCheckExpected2(@NonNull Double... d) {
	}
	public void nullCheckExpected1(Double @NonNull [] d) {
	}
	public void nullCheckExpected2(Double @NonNull ... d) {
	}
}
