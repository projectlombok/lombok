class NonNullWithSneakyThrows {
	@lombok.SneakyThrows void test(@lombok.NonNull String in) {
		System.out.println(in);
	}
}
