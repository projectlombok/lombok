interface DefaultMethod {
	int size();
	default boolean isEmpty() {
		return size() == 0;
	}
	strictfp default void run() {
	}
}