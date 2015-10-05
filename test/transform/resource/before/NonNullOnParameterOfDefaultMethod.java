// version 8:
interface NonNullOnParameterOfDefaultMethod {
	void test(@lombok.NonNull String arg);
	default void test2(@lombok.NonNull String arg) {
		System.out.println(arg);
	}
}
