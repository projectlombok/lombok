interface NonNullOnParameterOfDefaultMethod {
	void test(@lombok.NonNull String arg);
	default void test2(@lombok.NonNull String arg) {
		if (arg == null) {
			throw new java.lang.NullPointerException("arg");
		}
		System.out.println(arg);
	}
}
