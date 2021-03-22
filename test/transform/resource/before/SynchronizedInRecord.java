// version 14:

import lombok.Synchronized;

record SynchronizedInRecord(String a, String b) {
	@Synchronized
	public void foo() {
		String foo = "bar";
	}
}