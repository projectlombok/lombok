//version 8:
import lombok.NonNull;
class NonNullTypeUse {
	void test1(@NonNull String[][][] args) {
	}
	void test2(String @NonNull [][][] args) {
		if (args == null) {
			throw new java.lang.NullPointerException("args is marked non-null but is null");
		}
	}
	void test3(String[] @NonNull [][] args) {
	}
	void test4(String[][] @NonNull [] args) {
	}
	void test5(@NonNull String simple) {
		if (simple == null) {
			throw new java.lang.NullPointerException("simple is marked non-null but is null");
		}
	}
	void test6(java.lang.@NonNull String weird) {
		if (weird == null) {
			throw new java.lang.NullPointerException("weird is marked non-null but is null");
		}
	}
	void test7(java.lang.String @NonNull [][] weird) {
		if (weird == null) {
			throw new java.lang.NullPointerException("weird is marked non-null but is null");
		}
	}
	void test8(@NonNull String... args) {
	}
	void test9(String @NonNull ... args) {
		if (args == null) {
			throw new java.lang.NullPointerException("args is marked non-null but is null");
		}
	}
	void test10(java.lang.@NonNull String[] elements) {
	}
	// @NonNull applies to the String[] vararg elements, not the outer varargs array.
	void test11(String[] @NonNull ... elements) {
	}
	void test12(java.util.List<@NonNull String> elements) {
	}
	void test13(@NonNull String @NonNull [] args) {
		if (args == null) {
			throw new java.lang.NullPointerException("args is marked non-null but is null");
		}
	}
	// The annotations apply to the String elements and the inner String[] dimension, not the outer array.
	void test14(@NonNull String[] @NonNull [] elements) {
	}
	// The annotation is on the inner array dimension; the outer array reference remains nullable.
	void test15(String[] @NonNull [] elements) {
	}
	void test16(java.util.@NonNull List<String> args) {
		if (args == null) {
			throw new java.lang.NullPointerException("args is marked non-null but is null");
		}
	}
	// Here the annotation is on the outer dimension of the varargs array and must be checked.
	void test17(String @NonNull []... elements) {
		if (elements == null) {
			throw new java.lang.NullPointerException("elements is marked non-null but is null");
		}
	}
}
