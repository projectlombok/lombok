//version 8:
import lombok.NonNull;

class NonNullTypeUse {
	void test1(@NonNull String[][][] args) {
	}
	void test2(String @NonNull [][][] args) {
	}
	void test3(String [] @NonNull [][] args) {
	}
	void test4(String [][] @NonNull [] args) {
	}
	void test5(@NonNull String simple) {
	}
	void test6(java.lang.@NonNull String weird) {
	}
	void test7(java.lang.String @NonNull [][] weird) {
	}
	void test8(@NonNull String... args) {
	}
	void test9(String @NonNull ... args) {
	}
	void test10(java.lang.@NonNull String[] elements) {
	}
	void test11(String[] @NonNull ... elements) {
	}
	void test12(java.util.List<@NonNull String> elements) {
	}
}
