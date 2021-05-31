// version 14:

import lombok.experimental.Locked;

public record LockedInRecord(String a, String b) {
	@Locked
	public void foo() {
		String foo = "bar";
	}
}
