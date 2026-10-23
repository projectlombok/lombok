import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import java.util.List;
import java.util.Map;
class LombokNonNull {
	@NonNull
	Double[] noNullCheck;
	List<@NonNull Double> noNullCheckList;
	String @NonNull [] nullCheck;
	@NonNull
	List<String> nullCheckList;
	Map<@NonNull String, @NonNull List<@Nullable Object @NonNull []>> test;
	public void noNullCheckExpected1(@NonNull Double[] d) {
	}
	public void noNullCheckExpected2(@NonNull Double... d) {
	}
	public void nullCheckExpected1(Double @NonNull [] d) {
		if (d == null) {
			throw new java.lang.NullPointerException("d is marked non-null but is null");
		}
	}
	public void nullCheckExpected2(Double @NonNull ... d) {
		if (d == null) {
			throw new java.lang.NullPointerException("d is marked non-null but is null");
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setNoNullCheck(@NonNull final Double[] noNullCheck) {
		this.noNullCheck = noNullCheck;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setNoNullCheckList(final List<@NonNull Double> noNullCheckList) {
		this.noNullCheckList = noNullCheckList;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setNullCheck(final String @NonNull [] nullCheck) {
		if (nullCheck == null) {
			throw new java.lang.NullPointerException("nullCheck is marked non-null but is null");
		}
		this.nullCheck = nullCheck;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setNullCheckList(@NonNull final List<String> nullCheckList) {
		if (nullCheckList == null) {
			throw new java.lang.NullPointerException("nullCheckList is marked non-null but is null");
		}
		this.nullCheckList = nullCheckList;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setTest(final Map<@NonNull String, @NonNull List<@Nullable Object @NonNull []>> test) {
		this.test = test;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public LombokNonNull(final String @NonNull [] nullCheck, @NonNull final List<String> nullCheckList) {
		if (nullCheck == null) {
			throw new java.lang.NullPointerException("nullCheck is marked non-null but is null");
		}
		if (nullCheckList == null) {
			throw new java.lang.NullPointerException("nullCheckList is marked non-null but is null");
		}
		this.nullCheck = nullCheck;
		this.nullCheckList = nullCheckList;
	}
}
