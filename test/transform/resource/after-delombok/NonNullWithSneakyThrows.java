class NonNullWithSneakyThrows {
	void test(@lombok.NonNull String in) {
		try {
			if (in == null) {
				throw new java.lang.NullPointerException("in is marked non-null but is null");
			}
			System.out.println(in);
		} catch (final java.lang.Throwable $ex) {
			throw lombok.Lombok.sneakyThrow($ex);
		}
	}
}
